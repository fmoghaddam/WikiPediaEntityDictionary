package controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import model.Category;
import model.DataSourceType;
import model.Entity;
import util.URLUTF8Encoder;

public class EntityFileLoader {
	private static final Logger LOG = Logger.getLogger(EntityFileLoader.class.getCanonicalName());
	private static final String ENTITY_FOLDER_NAME = "data/entities";

	public static Map<String, Entity> loadData(DataSourceType dataSourceType) {
		String dataSubFolder;
		switch (dataSourceType) {
		case WIKIPEDIA:
			dataSubFolder = ENTITY_FOLDER_NAME+File.separator + "wikipedia";
			break;
		case WIKIDATA:
			dataSubFolder = ENTITY_FOLDER_NAME+File.separator + "wikidata";
			break;
		case ALL:
			dataSubFolder = ENTITY_FOLDER_NAME+File.separator + "all";
			break;
		default:
			dataSubFolder = ENTITY_FOLDER_NAME+File.separator + "wikipedia";
			break;
		}
		
		final Map<String, Entity> map = new LinkedHashMap<>();
		final File[] listOfFiles = new File(dataSubFolder).listFiles();

		try {
			for (int i = 0; i < listOfFiles.length; i++) {
				final String fileName = listOfFiles[i].getName();
				final BufferedReader br = new BufferedReader(
						new FileReader(dataSubFolder + File.separator + fileName));
				String entityName;
				
				while ((entityName = br.readLine()) != null) {
					if(entityName==null || entityName.isEmpty()){
						continue;
					}
					final String[] data = entityName.split(";");
					map.put(URLUTF8Encoder.encode(data[2]),
							new Entity(data[0],data[1], data[2],Category.resolve(fileName)));
				}
				br.close();
			}
		} catch (final IOException exception) {
			LOG.error(exception.getMessage());
		}
		return map;
	}
}
