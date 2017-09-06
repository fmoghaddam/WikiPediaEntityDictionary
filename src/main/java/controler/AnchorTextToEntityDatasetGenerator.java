package controler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import model.AnchorText;
import model.Category;
import model.DataSourceType;
import model.Dataset;
import model.Dictionary;
import model.Entity;
import model.RoleListProvider;
import model.RoleListProviderFileBased;
import util.CharactersUtils;
import util.HTMLLinkExtractor;
import util.HTMLLinkExtractor.HtmlLink;

public class AnchorTextToEntityDatasetGenerator {

	private static final Logger LOG = Logger.getLogger(AnchorTextToEntityDatasetGenerator.class.getCanonicalName());
	private static final Dictionary DICTIONARY = new Dictionary();
	private static final RoleListProvider roleProvider = new RoleListProviderFileBased();
	private static final Dataset DATASET = new Dataset();
	private static String WIKI_FILES_FOLDER = "data";
	private static int NUMBER_OF_THREADS = 1;

	private static Map<String, Entity> entityMap;
	private static ExecutorService executor;

	private static final Properties properties = new Properties();

	static final StanfordCoreNLP pipeline;
	static {
		properties.setProperty("annotators", "tokenize, ssplit, parse");
		pipeline = new StanfordCoreNLP(properties);
	}

	public static void main(String[] args) {
		NUMBER_OF_THREADS = Integer.parseInt(args[0]);
		WIKI_FILES_FOLDER = args[1];
		executor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

		entityMap = EntityFileLoader.loadData();
		roleProvider.loadRoles(DataSourceType.ALL);
		checkWikiPages(entityMap);
	}

	private static void checkWikiPages(final Map<String, Entity> entityMap) {
		try {
			final File[] listOfFolders = new File(WIKI_FILES_FOLDER).listFiles();
			Arrays.sort(listOfFolders);
			for (int i = 0; i < listOfFolders.length; i++) {
				final String subFolder = listOfFolders[i].getName();
				executor.execute(handle(WIKI_FILES_FOLDER + File.separator + subFolder + File.separator));
			}
			executor.shutdown();
			executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
			DATASET.printPositiveDataset();
			LOG.info("*****************************************************");
			DATASET.printNegativeDataset();
			// DICTIONARY.printResultWithoutEntitesWithClustringCoefficient();
			// DICTIONARY.printResult();
		} catch (final Exception exception) {
			exception.printStackTrace();
		}
	}

	private static Runnable handle(String pathToSubFolder) {
		final Runnable r = new Runnable() {
			@Override
			public void run() {
				try {
					final File[] listOfFiles = new File(pathToSubFolder).listFiles();
					Arrays.sort(listOfFiles);
					for (int j = 0; j < listOfFiles.length; j++) {
						final String file = listOfFiles[j].getName();
						// String line = new
						// String(Files.readAllBytes(Paths.get(pathToSubFolder +
						// File.separator + file)));

						BufferedReader br = new BufferedReader(new FileReader(pathToSubFolder + File.separator + file));
						String line;
						int lineCounter = 0;
						while ((line = br.readLine()) != null) {
							// Ignore first 3 lines as they are just titles
							if (lineCounter++ < 3) {
								continue;
							}

							if (!line.contains("<")) {
								continue;
							}

							final HTMLLinkExtractor htmlLinkExtractor = new HTMLLinkExtractor();
							final Vector<HtmlLink> links = htmlLinkExtractor.grabHTMLLinks(line);
							for (Iterator<?> iterator = links.iterator(); iterator.hasNext();) {
								final HtmlLink htmlLink = (HtmlLink) iterator.next();
								final Entity entity = entityMap.get(htmlLink.getLink());
								if (entity != null) {
									final String linkText = refactor(htmlLink.getLinkText().trim(), entity);
									if (linkText != null && !linkText.isEmpty()) {
										DICTIONARY.merge(new AnchorText(linkText), entity);
										DATASET.addPositiveData(
												entity.getCategoryFolder() + ";" + htmlLink.getFullSentence());
									}
								} else {
									String anchorText = htmlLink.getLinkText();
									for (final Entry<String, Set<Category>> roleEntity : roleProvider.getData().entrySet()) {
										final List<Category> roleCategory = new ArrayList<>(roleEntity.getValue());
										final String role = roleEntity.getKey().replaceAll("\\.", "\\\\.");
										if (role.charAt(0) == '<' && role.charAt(role.length() - 1) == '>') {
											continue;
										}
										String regexPattern = "(?im)";
										regexPattern += "\\b";
										regexPattern += role;
										regexPattern += "\\b";

										final Pattern pattern = Pattern.compile("(?im)" + regexPattern);
										final Matcher matcher = pattern.matcher(anchorText);
										if (matcher.find()) {
											DATASET.addNegativeData(
													roleCategory + ";" + htmlLink.getFullSentence());
											break;
										}
									}
								}
							}
						}
						br.close();
					}
					System.out.println("Folder " + pathToSubFolder + " has been processed.");
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
