package controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import model.AnchorText;
import model.Category;
import model.DataSourceType;
import model.Dictionary;
import model.Entity;
import util.CharactersUtils;
import util.HTMLLinkExtractor;
import util.HTMLLinkExtractor.HtmlLink;

/**
 * This class is responsible for generating Role dictionary by considering all
 * the anchor text from wikipedia, and normalizing them and aggregation.
 * 
 * @author fbm
 *
 */
public class DictionaryGenerator {

	/**
	 * Dictionary generation configuration
	 * Which datasource?
	 * Which category?
	 */
	private static final DataSourceType ENTITY_DATA_SOURCE = DataSourceType.ALL;
	private static final Category ENTITY_DATA_SOURCE_CATEGORY = Category.HEAD_OF_STATE_TAG;
	
	private static final Logger LOG = Logger.getLogger(DictionaryGenerator.class.getCanonicalName());
	private static final Dictionary DICTIONARY = new Dictionary();
	private static String WIKI_FILES_FOLDER = "wikipediafiles";
	private static int NUMBER_OF_THREADS = 1;

	private static Map<String, Entity> entityMap;
	private static ExecutorService executor;

	public static void main(String[] args) {

		NUMBER_OF_THREADS = Integer.parseInt(args[0]);
		WIKI_FILES_FOLDER = args[1];
		executor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
		entityMap = EntityFileLoader.loadData(ENTITY_DATA_SOURCE,ENTITY_DATA_SOURCE_CATEGORY);
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
			DICTIONARY.printResult();
		} catch (final Exception exception) {
			exception.printStackTrace();
		}
	}

	private static Runnable handle(String pathToSubFolder) {
		final Runnable r = new Runnable() {
			@Override
			public void run() {
				try {
					BufferedReader br = new BufferedReader(new FileReader(pathToSubFolder));
					String line;
					while ((line = br.readLine()) != null) {
						if(!line.contains("<")) {
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
								}
							}
						}
					}
					br.close();
					System.out.println("File " + pathToSubFolder + " has been processed.");
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
				continue;
			}
			if (entity.getName().contains(word)) {
				continue;
			} else {
				linkTextRefactored.append(word).append(" ");
			}
		}
		result = linkTextRefactored.toString();
		result = result.replaceAll("\\s+", " ");
		result = result.trim();
		return result;
	}

	protected static String removeFullNameAndEntityName(String anchorText, Entity entity) {
		String result = new String(anchorText);
		result = result.replaceAll(entity.getName(), "");
		result = result.replaceAll(entity.getEntityName(), "");
		result = result.replaceAll(entity.getEntityName().replaceAll("_", " "), "");
		return result;
	}

	public static String refactor(String anchorText, Entity entity) {
		String linkText = anchorText.trim();
		//TODO: Fix this part
		switch (ENTITY_DATA_SOURCE) {
		case WIKIDATA_LIST_OF_PRESON:
			linkText = removeS(anchorText.trim());
			linkText = removeFullNameAndEntityName(linkText.trim(), entity);
			linkText = removeFullNameAndEntityNameWordByWord(linkText.trim(), entity);
			linkText = convertUmlaut(linkText.trim());
			linkText = removeSpeicalCharacters(linkText.trim());
			linkText = removeDotsIfTheSizeOfTextIs2(linkText.trim());
			linkText = removeNoneAlphabeticSingleChar(linkText.trim());
			linkText = removeAlphabeticSingleChar(linkText.trim());
			linkText = ignoreAnchorTextWithSpeicalAlphabeticCharacter(linkText.trim());
			break;
		case WIKIPEDIA_LIST_OF_PERSON_MANUAL:
			linkText = removeS(anchorText.trim());
			linkText = removeFullNameAndEntityName(linkText.trim(), entity);
			linkText = removeFullNameAndEntityNameWordByWord(linkText.trim(), entity);
			linkText = convertUmlaut(linkText.trim());
			linkText = removeSpeicalCharacters(linkText.trim());
			linkText = removeDotsIfTheSizeOfTextIs2(linkText.trim());
			linkText = removeNoneAlphabeticSingleChar(linkText.trim());
			linkText = removeAlphabeticSingleChar(linkText.trim());
			linkText = ignoreAnchorTextWithSpeicalAlphabeticCharacter(linkText.trim());
			break;
		case ALL:
			linkText = removeS(anchorText.trim());
			linkText = removeFullNameAndEntityName(linkText.trim(), entity);
			linkText = removeFullNameAndEntityNameWordByWord(linkText.trim(), entity);
			linkText = convertUmlaut(linkText.trim());
			linkText = removeSpeicalCharacters(linkText.trim());
			linkText = removeDotsIfTheSizeOfTextIs2(linkText.trim());
			linkText = removeNoneAlphabeticSingleChar(linkText.trim());
			linkText = removeAlphabeticSingleChar(linkText.trim());
			linkText = ignoreAnchorTextWithSpeicalAlphabeticCharacter(linkText.trim());
			break;
		default:
			LOG.error("DATA SOURCE SHOULDBE SELECTED");
			break;
		}		
		return linkText;
	}

	private static String ignoreAnchorTextWithSpeicalAlphabeticCharacter(String text) {
		if (Charset.forName("US-ASCII").newEncoder().canEncode(text)) {
			return text;
		} else {
			return "";
		}

	}

	protected static String removeS(final String anchorText) {
		String result = new String(anchorText);
		result = result.replaceAll("'s ", " ");
		return result;
	}

	private static String convertUmlaut(String text) {
		final String[][] UMLAUT_REPLACEMENTS = { { new String("Ä"), "Ae" }, { new String("Ü"), "Ue" },
				{ new String("Ö"), "Oe" }, { new String("ä"), "ae" }, { new String("ü"), "ue" },
				{ new String("ö"), "oe" }, { new String("ß"), "ss" } };
		String result = text;
		for (int i = 0; i < UMLAUT_REPLACEMENTS.length; i++) {
			result = result.replace(UMLAUT_REPLACEMENTS[i][0], UMLAUT_REPLACEMENTS[i][1]);
		}
		return result;
	}

	protected static String removeSpeicalCharacters(String anchorText) {
		String result = new String(anchorText);
		for (String character : CharactersUtils.CHARS) {
			result = result.replaceAll(character, "");
		}
		return result;
	}

	protected static String removeDotsIfTheSizeOfTextIs2(String anchorText) {
		String result = new String(anchorText);
		if (anchorText.length() <= 2) {
			result = result.replaceAll(".", "");
		}
		return result;
	}

	protected static String removeNoneAlphabeticSingleChar(String anchorText) {
		if (anchorText.length() == 1) {
			if (!Character.isLetter(anchorText.charAt(0))) {
				return "";
			}
		}
		return anchorText;
	}

	protected static String removeAlphabeticSingleChar(String anchorText) {
		if (anchorText.length() == 1) {
			return "";
		}
		return anchorText;
	}
}
