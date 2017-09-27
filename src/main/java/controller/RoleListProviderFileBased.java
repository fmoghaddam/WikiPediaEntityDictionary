package controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import model.Category;
import model.DataSourceType;
import model.Order;
import model.RoleListProvider;

public class RoleListProviderFileBased extends RoleListProvider {

	/**
	 * This is the dictionary which already exist.
	 * It is used for selecting negative examples for dataset in {@link DatasetGenerator}
	 * 
	 */
	private static final String DATA_FOLDER = "data/dictionary/manually cleaned";

	public RoleListProviderFileBased() {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see main.RoleListProvider#loadRoles()
	 */
	@Override
	public void loadRoles(DataSourceType dataSourceType) {
		//NO RESET FOR NOW
		//roleMap.clear();
		String dataSubFolder;
		switch (dataSourceType) {
		case WIKIDATA_LIST_OF_PRESON:
			dataSubFolder = DATA_FOLDER+File.separator + DataSourceType.WIKIDATA_LIST_OF_PRESON.getText();
			break;
		case WIKIDATA_LABEL:
			dataSubFolder = DATA_FOLDER+File.separator + DataSourceType.WIKIDATA_LABEL.getText();;
			break;
		case WIKIPEDIA_LIST_OF_PERSON_MANUAL:
			dataSubFolder = DATA_FOLDER+File.separator + DataSourceType.WIKIPEDIA_LIST_OF_PERSON_MANUAL.getText();;
			break;
		case WIKIPEDIA_LIST_OF_TILTES:
			dataSubFolder = DATA_FOLDER+File.separator + DataSourceType.WIKIPEDIA_LIST_OF_TILTES.getText();;
			break;
		case ALL:
			dataSubFolder = DATA_FOLDER+File.separator + DataSourceType.ALL.getText();
			break;
		default:
			dataSubFolder = DATA_FOLDER+File.separator + "wikidataListOfMonarchs";
			break;
		}
		try {
			final File[] listOfFiles = new File(dataSubFolder).listFiles();
			for (int j = 0; j < listOfFiles.length; j++) {
				final String file = listOfFiles[j].getName();
				BufferedReader br = new BufferedReader(new FileReader(dataSubFolder + File.separator + file));
				String line;
				while ((line = br.readLine()) != null) {
					final Set<Category> categorySet = roleMap.get(line);
					if (categorySet == null || categorySet.isEmpty()) {
						final Set<Category> catSet = new HashSet<>();
						catSet.add(Category.resolve(file));
						roleMap.put(line, catSet);
					} else {
						categorySet.add(Category.resolve(file));
						roleMap.put(line, categorySet);
					}
				}
				br.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		sortBasedOnLenghth(Order.DESC);
	}
}
