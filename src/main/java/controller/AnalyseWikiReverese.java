package controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import model.DataSourceType;
import model.Entity;

/**
 * This class read all the wikireverse files and try to find all the urls which they have a link to English Wikipedia
 * At the end it writes its result to a file
 * @author fbm
 *
 */
public class AnalyseWikiReverese {

	/**
	 * Contains mapping between links and enitites It will be loaded based on
	 * "entitieswikidata" folder These are the seed as input list (persons,
	 * titles,...)
	 */
	private static Map<String, Entity> entityMap;
	private static final Set<String> urlList = new HashSet<>();
	
	public static void main(String[] args) {
		

		entityMap = EntityFileLoader.loadData(DataSourceType.WIKIDATA_LIST_OF_PRESON, null);
		entityMap.putAll(EntityFileLoader.loadData(DataSourceType.WIKIPEDIA_LIST_OF_PERSON_MANUAL, null));

		final File[] listOfFolders = new File("/home/fbm/eclipse-workspace/General Data/WikiReverse").listFiles();
		Arrays.sort(listOfFolders);
		for (int i = 0; i < listOfFolders.length; i++) {
			final String fileName = listOfFolders[i].getPath();
			try(BufferedReader br = new BufferedReader(new FileReader(fileName))) {
				String sCurrentLine;
				while ((sCurrentLine = br.readLine()) != null) {
					final String[] split = sCurrentLine.split("\t");
					
					if(!split[0].equals("en")) {
						continue;
					}
					final String wikiPediaLink = split[1];
					final String url = split[2];
					if(linkIsImportant(wikiPediaLink)) {
						urlList.add(url);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		//FileUtil.writeDataToFile(new ArrayList<>(urlList), "urls.txt");
		printStatistics();
		
	}

	private static void printStatistics() {
		int total = urlList.size();
		long contaiinWikipedia = urlList.stream().filter(p->p.contains("wikipedia.org")).count();
		System.err.println("wikipedia: "+contaiinWikipedia);
		
		int count = 0;
		for(String s: urlList) {
			if(s.contains("wikipedia.org")) {
				count++;
			}
		}
		
		System.err.println(count);
		System.err.println("Total Number of URLS: "+total);
		System.err.println("Total Number of URLS (No Wikipedia): "+(total-contaiinWikipedia));
		
	}

	private static boolean linkIsImportant(String link) {
		final String changedLink = link.replace("_", "%20");
		if(entityMap.containsKey(changedLink)){
			return true;
		}else {
			return false;
		}
	}
}
