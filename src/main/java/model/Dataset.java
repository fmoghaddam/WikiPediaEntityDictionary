package model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Logger;

public class Dataset {
	//private static final Logger LOG = Logger.getLogger(Dataset.class.getCanonicalName());
	private static final Logger positiveLog = Logger.getLogger("debugLogger");
	private static final Logger negativeLog = Logger.getLogger("reportsLogger");
	
	private final CopyOnWriteArrayList<String> positiveDataset = new CopyOnWriteArrayList<>();
	private final CopyOnWriteArrayList<String> negativeDataset = new CopyOnWriteArrayList<>();
	
	private final Map<Category,Set<String>> positiveDatasetStatistic = new HashMap<>();
	private final Map<Category,Set<String>> negativeDatasetStatistic = new HashMap<>();
	
	public void addPositiveData(final Category category,final String positiveData){
		if(positiveData==null || positiveData.isEmpty()){
			throw new IllegalArgumentException("Postive data can not be null or empty");
		}
		positiveDataset.add(positiveData);
		final Set<String> set = positiveDatasetStatistic.get(category);
		if(set==null) {
			final Set<String> newSet= new HashSet<>();
			newSet.add(positiveData);
			positiveDatasetStatistic.put(category, newSet);
		}else {
			set.add(positiveData);
			positiveDatasetStatistic.put(category, set);
		}
	}
	
	public void addNegativeData(final Category category,final String negativeData){
		if(negativeData==null || negativeData.isEmpty()){
			throw new IllegalArgumentException("Negative data can not be null or empty");
		}
		negativeDataset.add(negativeData);
		final Set<String> set = negativeDatasetStatistic.get(category);
		if(set==null) {
			final Set<String> newSet= new HashSet<>();
			newSet.add(negativeData);
			negativeDatasetStatistic.put(category, newSet);
		}else {
			set.add(negativeData);
			negativeDatasetStatistic.put(category, set);
		}
	}

	public CopyOnWriteArrayList<String> getPositiveDataset() {
		return positiveDataset;
	}

	public CopyOnWriteArrayList<String> getNegativeDataset() {
		return negativeDataset;
	}
	
	public void printPositiveDataset() {
		positiveLog.info("Number of positive samples = "+positiveDataset.size());
		for(String s:positiveDataset){
			positiveLog.info(s);
		}
	}
	
	public void printNegativeDataset() {
		negativeLog.info("Number of negative samples = "+negativeDataset.size());
		for(String s:negativeDataset){
			negativeLog.info(s);
		}
	}
	
	public void printPositiveDatasetStatistic() {
		for(Entry<Category, Set<String>> s:positiveDatasetStatistic.entrySet()){
			positiveLog.info(s.getKey()+" == "+s.getValue().size());
		}
	}
	
	public void printNegativeDatasetStatistic() {
		for(Entry<Category, Set<String>> s:negativeDatasetStatistic.entrySet()){
			negativeLog.info(s.getKey()+" == "+s.getValue().size());
		}
	}
}
