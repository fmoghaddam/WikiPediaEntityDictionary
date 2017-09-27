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

	/**
	 * 
	 * @param dataSourceType
	 * @param entityDataSourceCategory
	 *            should be null if we want to read all the files
	 * @return
	 */
	public static Map<String, Entity> loadData(DataSourceType dataSourceType, Category entityDataSourceCategory) {
		String dataSubFolder;
		final Map<String, Entity> map = new LinkedHashMap<>();
		switch (dataSourceType) {
		case WIKIDATA_LIST_OF_PRESON:
			dataSubFolder = ENTITY_FOLDER_NAME + File.separator + DataSourceType.WIKIDATA_LIST_OF_PRESON.getText();
			break;
		case WIKIDATA_LABEL:
			dataSubFolder = ENTITY_FOLDER_NAME + File.separator + DataSourceType.WIKIDATA_LABEL.getText();
			;
			break;
		case WIKIPEDIA_LIST_OF_PERSON_MANUAL:
			dataSubFolder = ENTITY_FOLDER_NAME + File.separator
					+ DataSourceType.WIKIPEDIA_LIST_OF_PERSON_MANUAL.getText();
			break;
		case WIKIPEDIA_LIST_OF_TILTES:
			dataSubFolder = ENTITY_FOLDER_NAME + File.separator + DataSourceType.WIKIPEDIA_LIST_OF_TILTES.getText();
			break;
		case ALL:
			dataSubFolder = ENTITY_FOLDER_NAME + File.separator + "wikidataListOfMonarchs";
			break;
		default:
			dataSubFolder = ENTITY_FOLDER_NAME + File.separator + "wikipedia";
			break;
		}

		if (entityDataSourceCategory == null) {
			final File[] listOfFiles = new File(dataSubFolder).listFiles();

			try {
				for (int i = 0; i < listOfFiles.length; i++) {
					final String fileName = listOfFiles[i].getName();
					final BufferedReader br = new BufferedReader(
							new FileReader(dataSubFolder + File.separator + fileName));
					String entityName;

					while ((entityName = br.readLine()) != null) {
						if (entityName == null || entityName.isEmpty()) {
							continue;
						}
						final String[] data = entityName.split(";");
						map.put(URLUTF8Encoder.encode(data[2]),
								new Entity(data[0], data[1], data[2], Category.resolve(fileName)));
					}
					br.close();
				}
			} catch (final IOException exception) {
				LOG.error(exception.getMessage());
			}
			return map;
		} else {

			dataSubFolder = dataSubFolder + File.separator + entityDataSourceCategory.text();
			final String fileName = dataSubFolder;
			Category resolve = Category.resolve(entityDataSourceCategory.text());
			try {
				final BufferedReader br = new BufferedReader(new FileReader(fileName));
				String entityName;

				while ((entityName = br.readLine()) != null) {
					if (entityName == null || entityName.isEmpty()) {
						continue;
					}
					final String[] data = entityName.split(";");
					map.put(URLUTF8Encoder.encode(data[2]), new Entity(data[0], data[1], data[2], resolve));
				}
				br.close();
			} catch (final IOException exception) {
				LOG.error(exception.getMessage());
			}
			return map;
		}
	}
}
