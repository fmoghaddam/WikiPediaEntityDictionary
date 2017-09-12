package controler;

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
import model.RoleListProviderFileBased;
import util.CharactersUtils;
import util.HTMLLinkExtractor;
import util.HTMLLinkExtractor.HtmlLink;

public class AnchorTextToEntityDatasetGeneratorCategory {

	private static final Dataset DATASET = new Dataset();
	/**
	 * This folder contains all the wikipedia pages which are already cleaned by a
	 * python code from https://github.com/attardi/wikiextractor and contains the
	 * links and anchor text
	 */
	private static String WIKI_FILES_FOLDER = "data";
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
	private static String CATEGORY_TREE_FOLDER = "categoryTree";
	/**
	 * Number of thread for parallelization
	 */
	private static int NUMBER_OF_THREADS = 1;

	private static Map<String, Entity> entityMap;

	private static ExecutorService executor;

	private static EntityToListOfCategories entityToCategoryList;

	private static final CategoryTrees categoryTrees = new CategoryTrees();

	private static final StringBuilder regexPattern = new StringBuilder();
	private static Pattern pattern;
	private static final TreeMap<String, Set<Category>> regexTextToCategories = new TreeMap<>(
			String.CASE_INSENSITIVE_ORDER);

	/**
	 * Role provide which reads already calculated dictioanry of roles from folder
	 * "dictionary"
	 */
	private static final RoleListProvider roleProvider = new RoleListProviderFileBased();

	public static void main(String[] args) {

		NUMBER_OF_THREADS = Integer.parseInt(args[0]);
		WIKI_FILES_FOLDER = args[1];
		ENTITY_CATEGORY_FILE = args[2];

		System.out.println("Loading skos category trees....");
		categoryTrees.load(CATEGORY_TREE_FOLDER);

		System.out.println("Extracting mapping between entites and categories....");
		entityToCategoryList = new EntityToListOfCategories(ENTITY_CATEGORY_FILE);
		entityToCategoryList.parse();

		executor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

		System.out.println("Loading seeds(list of persons, wikidata)....");
		entityMap = EntityFileLoader.loadData();

		System.out.println("Loading extracted roles (dictionaries)....");
		roleProvider.loadRoles(DataSourceType.WIKIPEDIA);

		regexPattern.append("(?im)");

		boolean first = true;
		for (final Entry<String, Set<Category>> roleEntity : roleProvider.getData().entrySet()) {
			final Set<Category> categories = roleEntity.getValue();
			final String role = roleEntity.getKey().replaceAll("\\.", "\\\\.");
			if (role.charAt(0) == '<' && role.charAt(role.length() - 1) == '>') {
				continue;
			}

			if (first) {
				first = false;
				regexPattern.append("(\\b").append(role).append("\\b)");
			} else {
				regexPattern.append("|").append("(\\b").append(role).append("\\b)");
			}

			regexTextToCategories.put(role, categories);
		}
		pattern = Pattern.compile(regexPattern.toString());
		System.out.println("Start....");
		checkWikiPages(entityMap);
	}

	private static void checkWikiPages(final Map<String, Entity> entityMap) {
		try {
			final File[] listOfFolders = new File(WIKI_FILES_FOLDER).listFiles();
			Arrays.sort(listOfFolders);
			final long now = System.currentTimeMillis();
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
			System.err.println(TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - now));
			DATASET.printPositiveDataset();
			DATASET.printNegativeDataset();
		} catch (final Exception exception) {
			exception.printStackTrace();
		}
	}

	private static Runnable handle(String pathToSubFolder) {
		final Runnable r = new Runnable() {
			@Override
			public void run() {
				try {
					// String line = new String(Files.readAllBytes(Paths.get(pathToSubFolder)),
					// StandardCharsets.UTF_8);
					//
					// final DocumentBuilderFactory docBuilderFactory =
					// DocumentBuilderFactory.newInstance();
					// final DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
					// final org.w3c.dom.Document document = docBuilder
					// .parse(new ByteArrayInputStream(line.getBytes(StandardCharsets.UTF_8)));
					//
					// final NodeList nodeList = document.getElementsByTagName("*");
					// for (int i = 0; i < nodeList.getLength(); i++) {
					// final Node node = nodeList.item(i);
					// if (node.getNodeType() == Node.ELEMENT_NODE) {
					// if (node.getNodeName().equals("doc")) {
					// System.err.println(node);
					// }
					// }
					// }

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
							final Entity entity = entityMap.get(link);
							if (entity != null) {
								final String linkText = refactor(htmlLink.getLinkText().trim(), entity);
								if (linkText != null && !linkText.isEmpty()) {
									DATASET.addPositiveData(
											entity.getCategoryFolder() + ";" + htmlLink.getFullSentence());
								}
							} else {
								final String anchorText = htmlLink.getLinkText();
								final Matcher matcher = pattern.matcher(anchorText);
								if (matcher.find()) {
									link = java.net.URLDecoder.decode(link);
									link = link.replaceAll(" ", "_");

									final Set<String> categoriesOfEntity = entityToCategoryList.getEntity2categories()
											.get(link);
									if (categoriesOfEntity == null) {
										continue;
									}
									boolean negativeFlag = true;
									String existInAnyTree = "";
									for (String cat : categoriesOfEntity) {
										existInAnyTree = categoryTrees.existInAnyTree(cat);
										if (existInAnyTree != null) {
											negativeFlag = false;
											break;
										}
									}
									if (negativeFlag) {
										final Set<Category> categorySet = regexTextToCategories.get(matcher.group());
										DATASET.addNegativeData(
												categorySet + ";" + matcher.group() + ";" + htmlLink.getFullSentence());
									}
								}
							}
						}
					}
					System.out.println("File " + pathToSubFolder + " has been processed.");
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
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
