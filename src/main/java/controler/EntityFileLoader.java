package controler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import model.Entity;
import util.URLUTF8Encoder;

public class EntityFileLoader {
	private static final Logger LOG = Logger.getLogger(EntityFileLoader.class.getCanonicalName());
	private static final String ENTITY_FOLDER_NAME = "entities";

	public static Map<String, Entity> loadData() {
		final Map<String, Entity> map = new LinkedHashMap<>();
		final File[] listOfFiles = new File(ENTITY_FOLDER_NAME).listFiles();

		try {
			for (int i = 0; i < listOfFiles.length; i++) {
				final String fineName = listOfFiles[i].getName();
				final BufferedReader br = new BufferedReader(
						new FileReader(ENTITY_FOLDER_NAME + File.separator + fineName));
				String entityName;
				while ((entityName = br.readLine()) != null) {
					final String[] data = entityName.split(";");
					map.put(URLUTF8Encoder.encode(data[2].toLowerCase()),
							new Entity(data[0].toLowerCase(), data[1].toLowerCase(), data[2].toLowerCase(),fineName));
				}
				br.close();
			}
		} catch (final IOException exception) {
			LOG.error(exception.getMessage());
		}
		return map;
	}
}
