package controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import model.Category;
import model.CategoryTrees;
import model.DataSourceType;
import model.Dataset;
import model.Entity;
import model.RoleListProvider;
import util.CharactersUtils;
import util.HTMLLinkExtractor;
import util.HTMLLinkExtractor.HtmlLink;

public class DatasetGeneratorWithCategoryTrees {

	/**
	 * Used when we add a result to a positive/negative dataset
	 */
	private static final String RESULT_FILE_SEPARATOR = "\t";
	/**
	 * This contains the positive and negative samples
	 */
	private static final Dataset DATASET = new Dataset();
	/**
	 * This folder contains all the wikipedia pages which are already cleaned by a
	 * python code from https://github.com/attardi/wikiextractor and contains the
	 * links and anchor text
	 */
	private static String WIKI_FILES_FOLDER = "wikipediafiles";
	/**
	 * This file is a dump which contains relation between each entity and its
	 * category entity dbp:subject category
	 */
	private static String ENTITY_CATEGORY_FILE = "category/article_categories_en.ttl";
	/**
	 * This folder files related to category trees which are already calculated as a
	 * preprocess by my another project named "CategoryTreeGeneration"
	 * https://github.com/fmoghaddam/CategoryTreeGeneration
	 */
	private static String CATEGORY_TREE_FOLDER = "data/categoryTree";
	/**
	 * Number of thread for parallelization
	 */
	private static int NUMBER_OF_THREADS = 1;
	/**
	 * Contains mapping between links and enitites It will be loaded based on
	 * "entitieswikidata" folder These are the seed as input list (persons,
	 * titles,...)
	 */
	private static Map<String, Entity> entityMap;
	private static ExecutorService executor;
	/**
	 * Continas mapping between entities and their category It uses dbpedia dump. It
	 * uses dct:subject
	 */
	private static EntityToListOfCategories entityToCategoryList;
	/**
	 * Contains category treeS
	 */
	private static final CategoryTrees categoryTrees = new CategoryTrees();
	/**
	 * Contians all the roles in one regex The roles are coming for precalculated
	 * dictionaries
	 */
	private static final StringBuilder regexPattern = new StringBuilder();
	private static Pattern pattern;
	/**
	 * Contains mapping between each role and their categories
	 */
	private static final TreeMap<String, Set<Category>> regexTextToCategories = new TreeMap<>(
			String.CASE_INSENSITIVE_ORDER);
	/**
	 * Reads already calculated dictionary of roles from folder "dictionary/manually
	 * cleaned"
	 */
	private static final RoleListProvider dictionaries = new RoleListProviderFileBased();

	public static void main(String[] args) {

		NUMBER_OF_THREADS = Integer.parseInt(args[0]);
		WIKI_FILES_FOLDER = args[1];
		ENTITY_CATEGORY_FILE = args[2];

		System.out.println("Loading skos category trees....");
		categoryTrees.load(CATEGORY_TREE_FOLDER);

		executor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

		System.out.println("Loading seeds(list of persons, wikidata)....");
		entityMap = EntityFileLoader.loadData(DataSourceType.WIKIPEDIA_LIST_OF_TILTES, null);
		entityMap.putAll(EntityFileLoader.loadData(DataSourceType.WIKIDATA_LIST_OF_PRESON, null));
		entityMap.putAll(EntityFileLoader.loadData(DataSourceType.WIKIPEDIA_LIST_OF_PERSON_MANUAL, null));
		// For now ALL means wikidataListOfMonarchs
		entityMap.putAll(EntityFileLoader.loadData(DataSourceType.ALL, null));

		System.out.println("Loading extracted roles (dictionaries)....");
		dictionaries.loadRoles(DataSourceType.WIKIPEDIA_LIST_OF_TILTES);
		dictionaries.loadRoles(DataSourceType.WIKIDATA_LABEL);

		regexPattern.append("(?im)");

		boolean first = true;
		for (final Entry<String, Set<Category>> roleEntity : dictionaries.getData().entrySet()) {
			final Set<Category> categories = roleEntity.getValue();
			final String originalrole = roleEntity.getKey();
			final String role = originalrole.replaceAll("\\.", "\\\\.");
			if (role.charAt(0) == '<' && role.charAt(role.length() - 1) == '>') {
				continue;
			}

			if (first) {
				first = false;
				regexPattern.append("(\\b").append(role).append("\\b)");
			} else {
				regexPattern.append("|").append("(\\b").append(role).append("\\b)");
			}

			regexTextToCategories.put(originalrole, categories);
		}
		pattern = Pattern.compile(regexPattern.toString());

		System.out.println("Extracting mapping between entites and categories....");
		entityToCategoryList = new EntityToListOfCategories(ENTITY_CATEGORY_FILE);
		entityToCategoryList.parse();

		System.out.println("Start....");
		checkWikiPages();
	}

	private static void checkWikiPages() {
		try {
			final File[] listOfFolders = new File(WIKI_FILES_FOLDER).listFiles();
			Arrays.sort(listOfFolders);
			for (int i = 0; i < listOfFolders.length; i++) {
				final String subFolder = listOfFolders[i].getName();
				final File[] listOfFiles = new File(WIKI_FILES_FOLDER + File.separator + subFolder + File.separator)
						.listFiles();
				Arrays.sort(listOfFiles);
				for (int j = 0; j < listOfFiles.length; j++) {
					final String file = listOfFiles[j].getName();
					executor.execute(handle(
							WIKI_FILES_FOLDER + File.separator + subFolder + File.separator + File.separator + file));
				}
			}
			executor.shutdown();
			executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
			DATASET.printPositiveDatasetStatistic();
			DATASET.printPositiveDataset();

			DATASET.printNegativeDatasetStatistic();
			DATASET.printNegativeDatasetDifficult();

			DATASET.printNegativeDatasetEasy();
		} catch (final Exception exception) {
			exception.printStackTrace();
		}
	}

	private static Runnable handle(String pathToSubFolder) {
		final Runnable r = new Runnable() {
			@SuppressWarnings("deprecation")
			@Override
			public void run() {
				try {
					final BufferedReader br = new BufferedReader(new FileReader(pathToSubFolder));
					String line;
					while ((line = br.readLine()) != null) {
						if (!line.contains("<")) {
							continue;
						}

						final HTMLLinkExtractor htmlLinkExtractor = new HTMLLinkExtractor();
						final Vector<HtmlLink> links = htmlLinkExtractor.grabHTMLLinks(line);
						for (Iterator<?> iterator = links.iterator(); iterator.hasNext();) {
							final HtmlLink htmlLink = (HtmlLink) iterator.next();
							String link = htmlLink.getLink();
							final String anchorText = htmlLink.getLinkText();
							final Entity entity = entityMap.get(link);
							link = java.net.URLDecoder.decode(link);
							link = link.replaceAll(" ", "_");
							/**
							 * If anchor text refer to any link in the list and anchor-text contains any
							 * role, It is positive sample
							 */
							if (entity != null) {
								final Matcher matcher = pattern.matcher(anchorText);
								String fullSentence = htmlLink.getFullSentence();
								String linktext = htmlLink.getLinkText();

								if (matcher.find()) {
									linktext = linktext.replace(matcher.group(), "<r>" + matcher.group() + "</r>");
									fullSentence = fullSentence.replace(htmlLink.getLinkText(),
											"<a>" + linktext + "</a>");

									DATASET.addPositiveData(entity.getCategoryFolder(),
											entity.getCategoryFolder() + RESULT_FILE_SEPARATOR + anchorText
													+ RESULT_FILE_SEPARATOR + matcher.group() + RESULT_FILE_SEPARATOR
													+ fullSentence + RESULT_FILE_SEPARATOR + link
													+ RESULT_FILE_SEPARATOR + htmlLink.getFullSentence(),
											fullSentence);
								} else {
									// TODO:
								}
								// cat log/positive.log | awk -F "\t" '{print $1"\t"$4}' > log/pos.tsv

							}
							/**
							 * It is a negative sample if contains any role and it does not have any
							 * connection to category trees.
							 */
							else {
								final Matcher matcher = pattern.matcher(anchorText);
								if (matcher.find()) {
									String fullSentence = htmlLink.getFullSentence();
									String linktext = htmlLink.getLinkText();

									final Set<String> categoriesOfEntity = entityToCategoryList.getEntity2categories()
											.get(link);
									if (categoriesOfEntity == null) {
										continue;
									}

									boolean negativeFlag = true;
									Category existInAnyTree = null;
									for (String cat : categoriesOfEntity) {
										existInAnyTree = categoryTrees.existInAnyTree(cat);
										if (existInAnyTree != null) {
											negativeFlag = false;
											break;
										}
									}
									if (negativeFlag) {

										String foundText = matcher.group();
										linktext = linktext.replace(foundText, "<r>" + foundText + "</r>");
										fullSentence = fullSentence.replace(htmlLink.getLinkText(),
												"<a>" + linktext + "</a>");
										final Set<Category> categorySet = regexTextToCategories.get(foundText);
										for (final Category cat : categorySet) {
											DATASET.addNegativeDifficultData(cat,
													cat + RESULT_FILE_SEPARATOR + anchorText + RESULT_FILE_SEPARATOR
															+ foundText + RESULT_FILE_SEPARATOR + fullSentence
															+ RESULT_FILE_SEPARATOR + link + RESULT_FILE_SEPARATOR
															+ htmlLink.getFullSentence(),
													fullSentence);
										}
									}
								} else {
									// Negative sample which does not have a role and does not refer to our list
//									String linktext = htmlLink.getLinkText();
//									String fullSentence = htmlLink.getFullSentence();
//									try {
//										fullSentence = fullSentence.replace(linktext, "<a>" + linktext + "</a>");
//										DATASET.addNegativeEasyData(
//												fullSentence + RESULT_FILE_SEPARATOR + htmlLink.getFullSentence());
//									} catch (Exception e) {
//										e.printStackTrace();
//									}
								}
							}
						}
					}
					System.out.println("File " + pathToSubFolder + " has been processed.");
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
					System.exit(1);
				}
			}
		};
		return r;
	}

	protected static String removeFullNameAndEntityNameWordByWord(String anchorText, Entity entity) {
		String result = new String(anchorText);
		String[] split = result.toString().split(" ");
		StringBuilder linkTextRefactored = new StringBuilder();
		for (final String word : split) {
			if (entity.getEntityName().contains(word)) {
				// linkTextRefactored.append("X");
				continue;
			}
			if (entity.getName().contains(word)) {
				// linkTextRefactored.append("X");
				continue;
			} else {
				linkTextRefactored.append(word).append(" ");
			}
		}
		result = linkTextRefactored.toString();
		result = result.replaceAll("\\s+", " ");
		result = result.trim();

		// result = result.replaceAll("XXX", "X");
		// result = result.replaceAll("XX", "X");
		//
		// if(result.contains("X")){
		// result = result.replaceAll("X", " X ");
		// }
		return result;
	}

	protected static String removeFullNameAndEntityName(String anchorText, Entity entity) {
		String result = new String(anchorText);
		result = result.replaceAll(entity.getName(), "");
		result = result.replaceAll(entity.getEntityName(), "");
		result = result.replaceAll(entity.getEntityName().replaceAll("_", " "), "");
		return result;
	}

	protected static String removeSpeicalCharacters(String anchorText) {
		String result = new String(anchorText);
		for (String character : CharactersUtils.CHARS) {
			result = result.replaceAll(character, "");
		}
		return result;
	}

	protected static String removeS(final String anchorText) {
		String result = new String(anchorText);
		result = result.replaceAll("'s ", " ");
		return result;
	}

	public static String refactor(String anchorText, Entity entity) {
		String linkText = removeS(anchorText.trim());
		linkText = removeSpeicalCharacters(linkText.trim());
		linkText = removeFullNameAndEntityName(linkText.trim(), entity);
		linkText = removeFullNameAndEntityNameWordByWord(linkText.trim(), entity);
		linkText = removeStopWords(linkText.trim());
		linkText = removeDotsIfTheSizeOfTextIs2(linkText.trim());
		linkText = removeNoneAlphabeticSingleChar(linkText.trim());
		linkText = removeAlphabeticSingleChar(linkText.trim());

		return linkText;
	}

	protected static String removeDotsIfTheSizeOfTextIs2(String anchorText) {
		String result = new String(anchorText);
		if (anchorText.length() <= 2) {
			result = result.replaceAll(".", "");
		}
		return result;
	}

	protected static String removeAlphabeticSingleChar(String anchorText) {
		if (anchorText.length() == 1) {
			return "";
		}
		return anchorText;
	}

	protected static String removeNoneAlphabeticSingleChar(String anchorText) {
		if (anchorText.length() == 1) {
			if (!Character.isLetter(anchorText.charAt(0))) {
				return "";
			}
		}
		return anchorText;
	}

	protected static String removeStopWords(String anchorText) {
		String result = new String(anchorText);
		if (anchorText.split(" ").length == 1) {
			for (String stopWord : CharactersUtils.STOP_WORDS) {
				if (result.equalsIgnoreCase(stopWord)) {
					result = result.replaceAll(stopWord, "");
				}
			}
		}
		return result;
	}

}
