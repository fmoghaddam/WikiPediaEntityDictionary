package controller;

import java.util.ArrayList;
import java.util.List;

import model.DataSourceType;
import model.RoleListProvider;
import util.FileUtil;
/**
 * This class convert dictionary to an standard gazetter for Stanford NER
 * @author fbm
 *
 */
public class DictionaryToStanfordGazettes {

	public static void main(String[] args) {
		final RoleListProvider dictionaries = new RoleListProviderFileBased();
		dictionaries.loadRoles(DataSourceType.WIKIPEDIA_LIST_OF_TILTES);
		dictionaries.loadRoles(DataSourceType.WIKIDATA_LABEL);
		
		final List<String> data = new ArrayList<>();
		for(String role:dictionaries.getData().keySet()) {
			data.add("ROLE"+"\t"+role);
		}
		
		FileUtil.writeDataToFile(data, "gazettes.txt");
	}

}
