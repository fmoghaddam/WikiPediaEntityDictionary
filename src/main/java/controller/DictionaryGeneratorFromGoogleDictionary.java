package controller;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Map;

import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;

import model.DataSourceType;
import model.Entity;

/**
 * This class is responsible for extracting important entries from google dictionary
 * We will add extracted entries to our dictionary
 * @author fbm
 *
 */
public class DictionaryGeneratorFromGoogleDictionary {
	private static Map<String, Entity> entityMap;
	
	public static void main(String[] args) {
		
		System.out.println("Loading seeds(list of persons, wikidata)....");
		entityMap = EntityFileLoader.loadData(DataSourceType.WIKIPEDIA_LIST_OF_TILTES, null);
		//entityMap.putAll(EntityFileLoader.loadData(DataSourceType.WIKIPEDIA_LIST_OF_PERSON_MANUAL, null));
		
		try {
			FileInputStream fin = new FileInputStream("/home/fbm/eclipse-workspace/General Data/dictionary.bz2");
			BufferedInputStream bis = new BufferedInputStream(fin);
			CompressorInputStream input = new CompressorStreamFactory().createCompressorInputStream(bis);
			BufferedReader br2 = new BufferedReader(new InputStreamReader(input));
			String sCurrentLine;
			while ((sCurrentLine = br2.readLine()) != null)
			{
				final String[] split = sCurrentLine.split("\t");
				final String anchorText = split[0];
				float probability = Float.parseFloat(split[1].split(" ")[0]);
				String url = split[1].split(" ")[1];
				url = java.net.URLEncoder.encode(url);
				//if(entityMap.get(url)!=null) {
				if(url.equalsIgnoreCase("President_of_the_United_States")) {
					if(probability>0.2)
						System.out.println(anchorText + "\t\t" + url + "\t\t"+probability +"\t\t"+sCurrentLine);
				}
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

}
