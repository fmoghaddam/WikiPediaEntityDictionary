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
	private static final Logger negativeDifficultLog = Logger.getLogger("reportsLogger");
	private static final Logger negativeEasyLog = Logger.getLogger("ExternalAppLogger");
	
	private final CopyOnWriteArrayList<String> positiveDataset = new CopyOnWriteArrayList<>();
	private final CopyOnWriteArrayList<String> negativeDatasetDifficult = new CopyOnWriteArrayList<>();
	private final CopyOnWriteArrayList<String> negativeDatasetEasy = new CopyOnWriteArrayList<>();
	
	private final Map<Category,Set<String>> positiveDatasetStatistic = new HashMap<>();
	private final Map<Category,Set<String>> negativeDatasetStatistic = new HashMap<>();
	
	public void addPositiveData(final Dataset localDataset) {
		if(localDataset.getPositiveDataset()==null) {
			throw new IllegalArgumentException("Positive dataset list can not be null");
		}
		this.positiveDataset.addAll(localDataset.getPositiveDataset());
		
		for(Entry<Category, Set<String>> entity:localDataset.positiveDatasetStatistic.entrySet()) {
			final Set<String> set = positiveDatasetStatistic.get(entity.getKey());
			if(set==null) {
				positiveDatasetStatistic.putAll(localDataset.positiveDatasetStatistic);
			}else {
				set.addAll(entity.getValue());
				positiveDatasetStatistic.put(entity.getKey(), set);
			}
		}
	}
	
	public void addNegativeDatasetDifficult(final Dataset localDataset) {
		if(localDataset==null) {
			throw new IllegalArgumentException("Negative difficult dataset list can not be null");
		}
		this.negativeDatasetDifficult.addAll(localDataset.getNegativeDatasetDifficult());
		
		for(Entry<Category, Set<String>> entity:localDataset.negativeDatasetStatistic.entrySet()) {
			final Set<String> set = negativeDatasetStatistic.get(entity.getKey());
			if(set==null) {
				negativeDatasetStatistic.putAll(localDataset.negativeDatasetStatistic);
			}else {
				set.addAll(entity.getValue());
				negativeDatasetStatistic.put(entity.getKey(), set);
			}
		}
	}
	
	public void addPositiveData(final Category category,final String positiveDataFull,final String positiveDataSentence){
		if(positiveDataFull==null || positiveDataFull.isEmpty()){
			throw new IllegalArgumentException("Postive data can not be null or empty");
		}
		positiveDataset.add(positiveDataFull);
		final Set<String> set = positiveDatasetStatistic.get(category);
		if(set==null) {
			final Set<String> newSet= new HashSet<>();
			newSet.add(positiveDataSentence);
			positiveDatasetStatistic.put(category, newSet);
		}else {
			set.add(positiveDataSentence);
			positiveDatasetStatistic.put(category, set);
		}
	}
	
	public void addNegativeDifficultData(final Category category,final String negativeDataFull,final String negativeDataSentence){
		if(negativeDataFull==null || negativeDataFull.isEmpty()){
			throw new IllegalArgumentException("Negative data can not be null or empty");
		}
		negativeDatasetDifficult.add(negativeDataFull);
		final Set<String> set = negativeDatasetStatistic.get(category);
		if(set==null) {
			final Set<String> newSet= new HashSet<>();
			newSet.add(negativeDataSentence);
			negativeDatasetStatistic.put(category, newSet);
		}else {
			set.add(negativeDataSentence);
			negativeDatasetStatistic.put(category, set);
		}
	}

	public void addNegativeEasyData(final String negativeDataFull){
		if(negativeDataFull==null || negativeDataFull.isEmpty()){
			throw new IllegalArgumentException("Negative data can not be null or empty");
		}
		negativeDatasetEasy.add(negativeDataFull);
	}
	
	public CopyOnWriteArrayList<String> getPositiveDataset() {
		return positiveDataset;
	}

	public CopyOnWriteArrayList<String> getNegativeDatasetDifficult() {
		return negativeDatasetDifficult;
	}
	
	public CopyOnWriteArrayList<String> getNegativeDatasetEasy() {
		return negativeDatasetEasy;
	}
	
	public void printPositiveDataset() {
		positiveLog.info("Number of positive samples = "+positiveDataset.size());
		for(String s:positiveDataset){
			positiveLog.info(s);
		}
	}
	
	public void printNegativeDatasetDifficult() {
		negativeDifficultLog.info("Number of negative samples = "+negativeDatasetDifficult.size());
		for(String s:negativeDatasetDifficult){
			negativeDifficultLog.info(s);
		}
	}
	
	public void printNegativeDatasetEasy() {
		negativeEasyLog.info("Number of negative samples = "+negativeDatasetDifficult.size());
		for(String s:negativeDatasetEasy){
			negativeEasyLog.info(s);
		}
	}
	
	public void printPositiveDatasetStatistic() {
		for(Entry<Category, Set<String>> s:positiveDatasetStatistic.entrySet()){
			positiveLog.info(s.getKey()+" == "+s.getValue().size());
		}
	}
	
	public void printNegativeDatasetStatistic() {
		for(Entry<Category, Set<String>> s:negativeDatasetStatistic.entrySet()){
			negativeDifficultLog.info(s.getKey()+" == "+s.getValue().size());
		}
	}
	
	public void printNegativeDatasetDifficultUnique() {		
		for(Entry<Category, Set<String>> s:negativeDatasetStatistic.entrySet()){
			for(String sentence:s.getValue()) {
				negativeDifficultLog.info(s.getKey()+"\t"+sentence);
			}
		}
	}
	
	public void printPositiveDatasetUnique() {		
		for(Entry<Category, Set<String>> s:positiveDatasetStatistic.entrySet()){
			for(String sentence:s.getValue()) {
				positiveLog.info(s.getKey()+"\t"+sentence);
			}
		}
	}
}
