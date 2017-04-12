package controler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import model.AnchorText;
import model.Dictionary;
import model.Entity;
import util.HTMLLinkExtractor;
import util.HTMLLinkExtractor.HtmlLink;
import util.SpecialCharacters;

public class AnchorTextToEntity {

	@SuppressWarnings("unused")
	private static final Logger LOG = Logger.getLogger(AnchorTextToEntity.class.getCanonicalName());
	private static final Dictionary DICTIONARY = new Dictionary();
	private static String WIKI_FILES_FOLDER = "data";
	private static int NUMBER_OF_THREADS = 1;

	private static Map<String, Entity> entityMap;
	private static ExecutorService executor;

	public static void main(String[] args) {

		NUMBER_OF_THREADS = Integer.parseInt(args[0]);
		WIKI_FILES_FOLDER = args[1];
		executor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

		entityMap = EntityFileLoader.loadData();
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
					final File[] listOfFiles = new File(pathToSubFolder).listFiles();
					Arrays.sort(listOfFiles);
					for (int j = 0; j < listOfFiles.length; j++) {
						final String file = listOfFiles[j].getName();
						BufferedReader br = new BufferedReader(new FileReader(pathToSubFolder + File.separator + file));
						String line;
						while ((line = br.readLine()) != null) {
							line = line.toLowerCase();
							final HTMLLinkExtractor htmlLinkExtractor = new HTMLLinkExtractor();
							final Vector<HtmlLink> links = htmlLinkExtractor.grabHTMLLinks(line);
							for (Iterator<?> iterator = links.iterator(); iterator.hasNext();) {
								final HtmlLink htmlLink = (HtmlLink) iterator.next();
								final Entity entity = entityMap.get(htmlLink.getLink());
								if (entity != null) {
									String linkText = refactor(htmlLink.getLinkText().trim(),entity);

									// Remove any word which exist in the entity name
									//									 String[] split = linkText.toString().split(" ");
									//									 StringBuilder linkTextRefactored = new StringBuilder();
									//									 for(final String word: split){
									//										 if(!entity.getCategoryFolder().contains("WorldCupWinner")){
									//											 if(entity.getEntityName().contains(word)){
									//												 continue;
									//											 }
									//										 }
									//										 if(entity.getName().contains(word)){
									//											 continue;
									//										 }
									//										 //if(!entity.getName().contains(word)){
									//										 else{
									//											 linkTextRefactored.append(word).append(" ");
									//										 }
									//									 }
									//									 if(linkTextRefactored.toString().isEmpty() || linkTextRefactored.toString() == ""){
									//										 continue;
									//									 }

									// linkTextRefactored = linkText;

									// linkTextRefactored =
									// refactor(linkTextRefactored);

									DICTIONARY.merge(new AnchorText(linkText), entity);
									//									DICTIONARY.merge(new AnchorText(linkTextRefactored.toString()), entity);
									// DICTIONARY.merge(linkTextRefactored.toString(),set,
									// biFunction);
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

	public static String refactor(String anchorText, Entity entity) {
		String result = new String(anchorText);

		result = result.replaceAll("'s ", " ");
		for(String character:SpecialCharacters.CHARS){
			result = result.replaceAll(character, "");
		}
		result = result.replaceAll(entity.getName(), "");
		result = result.replaceAll(entity.getEntityName(), "");
		result = result.replaceAll(entity.getEntityName().replaceAll("_"," "), "");

		String[] split = result.toString().split(" ");
		StringBuilder linkTextRefactored = new StringBuilder();
		for(final String word: split){
			if(entity.getEntityName().contains(word)){
				continue;
			}
			if(entity.getName().contains(word)){
				continue;
			}
			else{
				linkTextRefactored.append(word).append(" ");
			}
		}
		
		result = linkTextRefactored.toString();

		result = result.replaceAll("\\s+", " ");
		result = result.trim();

		return result.toString();
	}

}
