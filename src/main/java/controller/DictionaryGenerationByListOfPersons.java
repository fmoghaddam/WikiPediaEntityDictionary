package controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import model.AnchorText;
import model.DataSourceType;
import model.Dictionary;
import model.Entity;
import util.HTMLLinkExtractor;
import util.HTMLLinkExtractor.HtmlLink;
import util.NERTagger;
import util.NER_TAG;
import util.POSTagger;

/**
 * This class is responsible for generating Role dictionary by considering all
 * the anchor text from wikipedia which are refereing to persons and normalizing
 * them and aggregation.
 * 
 * @author fbm
 *
 */
public class DictionaryGenerationByListOfPersons {

	/**
	 * Dictionary generation configuration Which datasource? Which category?
	 * Here be careful, this class only work with the list of persons not list of titles 
	 */
	private static final DataSourceType ENTITY_DATA_SOURCE = DataSourceType.WIKIDATA;
	private static final Dictionary DICTIONARY = new Dictionary();
	private static final Logger LOG = Logger.getLogger(DictionaryGenerationByListOfPersons.class.getCanonicalName());
	private static String WIKI_FILES_FOLDER = "wikipediafiles";
	private static int NUMBER_OF_THREADS = 1;

	private static Map<String, Entity> entityMap;
	private static ExecutorService executor;

	public static void main(String[] args) {
		NUMBER_OF_THREADS = Integer.parseInt(args[0]);
		WIKI_FILES_FOLDER = args[1];
		executor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
		entityMap = EntityFileLoader.loadData(ENTITY_DATA_SOURCE, null);
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
			DICTIONARY.printResultByCategory();
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
						if (!line.contains("<a href=")) {
							continue;
						}
						final HTMLLinkExtractor htmlLinkExtractor = new HTMLLinkExtractor();
						final Vector<HtmlLink> links = htmlLinkExtractor.grabHTMLLinks(line);
						for (Iterator<?> iterator = links.iterator(); iterator.hasNext();) {
							final HtmlLink htmlLink = (HtmlLink) iterator.next();
							final Entity entity = entityMap.get(htmlLink.getLink());
							if (entity != null) {
								String anchor = htmlLink.getLinkText().trim();
								anchor = refactor(anchor, entity);

								if (anchor.isEmpty()) {
									continue;
								}
								DICTIONARY.merge(new AnchorText(anchor), entity);
							}
						}
					}
					br.close();
					System.out.println("File " + pathToSubFolder + " has been processed.");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			private String refactor(String anchor, Entity entity) {
				anchor = spellCheck(anchor);
				anchor = removeQutationFromStartAndEnd(anchor);
				//anchor = convertUmlaut(anchor);
				anchor = removeNERPerson(anchor);
				anchor = removeFullNameAndEntityName(anchor, entity);
				anchor = removeFullNameAndEntityNameWordByWord(anchor, entity);
				anchor = removeAnchorTextWithoutNonePhrase(anchor);
				anchor = removeS(anchor);
				anchor = doseNotContainAlphabet(anchor);
				anchor = removeAnchorTextWithoutPossesiveProNone(anchor);
				anchor = removeCommaFromStartAndEnd(anchor);
				anchor = removeFullNameAndEntityNameWordByWord(anchor, entity);
				anchor = removeHumanTitle(anchor);
				anchor = removeParanteze(anchor);
				anchor = removeLastPreposition(anchor);				
				anchor = removeDotsIfTheSizeOfTextIs2(anchor);
				anchor = removeIfTheSizeOfTextIsLessThan2(anchor);
				anchor = removeFamilyTitle(anchor);
				anchor = removeHtmlTags(anchor);
				return anchor.trim();
			}

			private String removeHtmlTags(String anchor) {
				if(anchor.contains("<")) {
					return "";
				}
				return anchor;
			}

			private String removeIfTheSizeOfTextIsLessThan2(String anchor) {
				if (anchor.trim().length() <= 2) {
					return "";
				} else {
					return anchor;
				}
			}

			private String removeLastPreposition(String anchor) {
				String text = new String(anchor);
				text = NERTagger.runTaggerString(text);
				String runPOSTagger = POSTagger.runPOSTaggerWithNoNER(text);

				String nerpostext = POSTagger.replaceWordsWithTagsButNotNER(runPOSTagger, text);
				String[] split = nerpostext.split(" ");
				if (split[split.length - 1].equals("<IN>")) {
					StringBuilder result = new StringBuilder();
					for (int i = 0; i < anchor.split(" ").length - 1; i++) {
						result.append(anchor.split(" ")[i]).append(" ");
					}
					return result.toString().trim();
				}
				return anchor;
			}

			private String removeParanteze(String anchor) {
				anchor = anchor.trim();
				anchor = anchor.replaceAll("\\)", "");
				anchor = anchor.replaceAll("\\(", "");
				anchor = anchor.replaceAll("\\]", "");
				anchor = anchor.replaceAll("\\[", "");
				return anchor.trim();
			}

			private String removeQutationFromStartAndEnd(String anchor) {
				anchor = anchor.trim();
				anchor = anchor.replaceAll("\"", "");
				anchor = anchor.replaceAll("\\”", "");
				anchor = anchor.replaceAll("\\“", "");
				return anchor;
			}

			private String removeHumanTitle(String anchor) {
				final List<String> titles = Arrays.asList("mr", "mr.", "mrs", "mrs.", "ms", "ms.");
				if (titles.contains(anchor.toLowerCase())) {
					return "";
				} else {
					return anchor;
				}
			}

			private String removeFamilyTitle(String anchor) {
				final List<String> titles = Arrays.asList("son", "father", "husbend", "wife", "dauther", "mother",
						"family", "lady", "brother", "grandfather","grandson","grandmother","infant","child");
				if (titles.contains(anchor.toLowerCase().trim())) {
					return "";
				} else {
					return anchor;
				}
			}

			private String convertUmlaut(String text) {
				final String[][] UMLAUT_REPLACEMENTS = { { new String("Ä"), "Ae" }, { new String("Ü"), "Ue" },
						{ new String("Ö"), "Oe" }, { new String("ä"), "ae" }, { new String("ü"), "ue" },
						{ new String("ö"), "oe" }, { new String("ß"), "ss" } };
				String result = text;
				for (int i = 0; i < UMLAUT_REPLACEMENTS.length; i++) {
					result = result.replace(UMLAUT_REPLACEMENTS[i][0], UMLAUT_REPLACEMENTS[i][1]);
				}
				return result;
			}

			protected String removeDotsIfTheSizeOfTextIs2(String anchorText) {
				String result = new String(anchorText);
				if (anchorText.length() <= 2) {
					result = result.replaceAll("\\.", "");
				}
				return result;
			}

			private String removeCommaFromStartAndEnd(String anchor) {
				anchor = anchor.trim();
				anchor = anchor.replaceAll(", ", "");
				anchor = anchor.replaceAll(" ,", "");
				return anchor;
			}

			private String removeAnchorTextWithoutPossesiveProNone(String anchor) {
				String text = new String(anchor);
				text = NERTagger.runTaggerString(text);
				String runPOSTagger = POSTagger.runPOSTaggerWithNoNER(text);

				String nerpostext = POSTagger.replaceWordsWithTagsButNotNER(runPOSTagger, text);
				if (nerpostext.contains("PRP$")) {
					return "";
				} else {
					return anchor;
				}
			}

			protected String doseNotContainAlphabet(String anchorText) {
				for (int i = 0; i < anchorText.length(); i++) {
					if (Character.isLetter(anchorText.charAt(i))) {
						return anchorText;
					}
				}
				return "";
			}

			private String spellCheck(String anchor) {
				// TODO
				return anchor;
			}

			protected String removeS(final String anchorText) {
				String result = new String(anchorText);
				result = result.replaceAll("'s ", " ");
				result = result.replaceAll("'s", "");
				result = result.replaceAll("´s", "");
				result = result.replaceAll("`s", "");
				result = result.replaceAll("’s", "");
				return result;
			}

			private String removeAnchorTextWithoutNonePhrase(String anchor) {
				String text = new String(anchor);
				text = NERTagger.runTaggerString(text);
				String runPOSTagger = POSTagger.runPOSTaggerWithNoNER(text);

				String nerpostext = POSTagger.replaceWordsWithTagsButNotNER(runPOSTagger, text);
				if (nerpostext.contains("<NN>") || nerpostext.contains("<NNP>")) {
					return anchor;
				} else {
					return "";
				}
			}

			protected String removeFullNameAndEntityNameWordByWord(String anchorText, Entity entity) {
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

			protected String removeFullNameAndEntityName(String anchorText, Entity entity) {
				String result = new String(anchorText);
				result = result.replaceAll(entity.getName(), "");
				result = result.replaceAll(entity.getEntityName(), "");
				result = result.replaceAll(entity.getEntityName().replaceAll("_", " "), "");
				return result;
			}

			private String removeNERPerson(String anchor) {
				anchor = NERTagger.runTaggerString(anchor, NER_TAG.PERSON);
				anchor = anchor.replaceAll("<PERSON>", "").trim();
				return anchor;
			}
		};
		return r;
	}

}
