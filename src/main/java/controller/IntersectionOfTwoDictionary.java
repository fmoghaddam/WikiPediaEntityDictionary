package controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import model.DataSourceType;
import model.RoleListProvider;

public class IntersectionOfTwoDictionary {

	private static final RoleListProvider dic1 = new RoleListProviderFileBased();
	private static final RoleListProvider dic2 = new RoleListProviderFileBased();
	
	public static void main(String[] args) {
		dic1.loadRoles(DataSourceType.WIKIDATA_LABEL);
		dic2.loadRoles(DataSourceType.WIKIPEDIA_LIST_OF_TILTES);

		final Set<String> dic1Set = dic1.getData().keySet();
		final Set<String> dic2Set = dic2.getData().keySet();
		
		final List<String> dic1List = new ArrayList<>(dic1Set);
		final List<String> dic2List = new ArrayList<>(dic2Set);
		int wikipediaSizeBefore = dic2List.size();
		System.err.println("Wikipedia dic size = "+wikipediaSizeBefore);
		System.err.println("Wikidata dic size = "+dic1List.size());
		
		for(int i = wikipediaSizeBefore - 1; i > -1; --i){
		    String str = dic2List.get(i);
		    if(!dic1List.remove(str))
		    	dic2List.remove(str);
		}
		
		System.err.println("Intesection size by considering case sensitivity = "+dic2List.size());
		
		
		final List<String> dic1List2 = dic1.getData().keySet().stream().map(p->p.toLowerCase()).collect(Collectors.toList());
		final List<String> dic2List2 = dic2.getData().keySet().stream().map(p->p.toLowerCase()).collect(Collectors.toList());
		
		for(int i = dic2List2.size() - 1; i > -1; --i){
		    String str = dic2List2.get(i);
		    if(!dic1List2.remove(str))
		    	dic2List2.remove(str);
		}
		
		System.err.println("Intesection size by considering case INsensitivity = "+dic2List2.size());
		
		
	}

}
