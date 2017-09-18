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

	public static Map<String, Entity> loadData(DataSourceType dataSourceType, Category entityDataSourceCategory) {
		String dataSubFolder;
		switch (dataSourceType) {
		case WIKIPEDIA:
			dataSubFolder = ENTITY_FOLDER_NAME + File.separator + "wikipedia";
			break;
		case WIKIDATA:
			dataSubFolder = ENTITY_FOLDER_NAME + File.separator + "wikidataFull";
			break;
		case ALL:
			dataSubFolder = ENTITY_FOLDER_NAME + File.separator + "all";
			break;
		default:
			dataSubFolder = ENTITY_FOLDER_NAME + File.separator + "wikipedia";
			break;
		}

		dataSubFolder = dataSubFolder + File.separator + entityDataSourceCategory.text();
		final String fileName = dataSubFolder;
		Category resolve = Category.resolve(entityDataSourceCategory.text());
		final Map<String, Entity> map = new LinkedHashMap<>();

		try {
			final BufferedReader br = new BufferedReader(new FileReader(fileName));
			String entityName;

			while ((entityName = br.readLine()) != null) {
				if (entityName == null || entityName.isEmpty()) {
					continue;
				}
				final String[] data = entityName.split(";");
				map.put(URLUTF8Encoder.encode(data[2]),
						new Entity(data[0], data[1], data[2],resolve ));
			}
			br.close();
		} catch (final IOException exception) {
			LOG.error(exception.getMessage());
		}
		return map;
	}
}
