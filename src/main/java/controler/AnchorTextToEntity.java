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

import model.Dictionary;
import model.Entity;
import util.HTMLLinkExtractor;
import util.HTMLLinkExtractor.HtmlLink;

public class AnchorTextToEntity {

	private static final int NUMBER_OF_THREADS = 45;
	private static final Logger LOG = Logger.getLogger(AnchorTextToEntity.class.getCanonicalName());
	private static final Dictionary DICTIONARY = new Dictionary();
	private static final String WIKI_FILES_FOLDER = "data";
	
	private static Map<String, Entity> entityMap;
	private static final ExecutorService executor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

	public static void main(String[] args) {
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
									// Remove exact names
									// if(htmlLink.getLinkText().equals(entity.getName())){
									// continue;
									// }

									StringBuilder linkText = new StringBuilder(htmlLink.getLinkText().trim());
									// linkText = refactor(linkText);

									// Remove any word which exist in the entity
									// name
									// String[] split =
									// linkText.toString().split(" ");
									// StringBuilder linkTextRefactored = new
									// StringBuilder();
									// for(final String word: split){
									// if(!entity.getName().contains(word)){
									// linkTextRefactored.append(word).append("
									// ");
									// }
									// }
									// if(linkTextRefactored.toString().isEmpty()
									// || linkTextRefactored.toString() == ""){
									// continue;
									// }

									// linkTextRefactored = linkText;

									// linkTextRefactored =
									// refactor(linkTextRefactored);
									
									DICTIONARY.merge(linkText.toString(), entity);
									// DICTIONARY.merge(linkTextRefactored.toString(),set,
									// biFunction);
								}
							}
						}
						br.close();
					}
					System.out.println("Folder " + pathToSubFolder + " has been processed.");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};

		return r;
	}

	@SuppressWarnings("unused")
	private static StringBuilder refactor(StringBuilder linkText) {
		StringBuilder result = new StringBuilder(linkText.toString().trim());
		int index = result.lastIndexOf("'");
		while (index == result.length() - 2 && index >= 0) {
			result = new StringBuilder(result.substring(0, index).trim());
			index = result.lastIndexOf("'");
			// if(index == linkText.length()-2 && index>=0){
			// linkText = new StringBuilder(linkText.substring(0, index));
		}
		index = result.lastIndexOf("'");
		while (index == result.length() - 1 && index >= 0) {
			result = new StringBuilder(result.substring(0, index).trim());
			index = result.lastIndexOf("'");
			// if(index == linkText.length()-1 && index>=0){
			// linkText = new StringBuilder(linkText.substring(0, index));
		}
		index = result.lastIndexOf("’");
		while (index == result.length() - 2 && index >= 0) {
			result = new StringBuilder(result.substring(0, index).trim());
			index = result.lastIndexOf("’");
			// if(index == linkText.length()-2 && index>=0){
			// linkText = new StringBuilder(linkText.substring(0, index));
		}
		index = result.lastIndexOf("’");
		while (index == result.length() - 1 && index >= 0) {
			// if(index == linkText.length()-1 && index>=0){
			result = new StringBuilder(result.substring(0, index).trim());
			index = result.lastIndexOf("’");
		}
		index = result.lastIndexOf(",");
		while (index == result.length() - 1 && index >= 0) {
			result = new StringBuilder(result.substring(0, index).trim());
			index = result.lastIndexOf(",");
			// if(index == linkText.length()-1 && index>=0){
			// linkText = new StringBuilder(linkText.substring(0, index));
		}
		return result;
	}

	
}
