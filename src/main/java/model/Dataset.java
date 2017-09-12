package model;

import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Logger;

public class Dataset {
	//private static final Logger LOG = Logger.getLogger(Dataset.class.getCanonicalName());
	private static final Logger positiveLog = Logger.getLogger("debugLogger");
	private static final Logger negativeLog = Logger.getLogger("reportsLogger");
	private final CopyOnWriteArrayList<String> positiveDataset = new CopyOnWriteArrayList<>();
	private final CopyOnWriteArrayList<String> negativeDataset = new CopyOnWriteArrayList<>();
	
	public void addPositiveData(final String positiveData){
		if(positiveData==null || positiveData.isEmpty()){
			throw new IllegalArgumentException("Postive data can not be null or empty");
		}
		positiveDataset.add(positiveData);
	}
	
	public void addNegativeData(final String negativeData){
		if(negativeData==null || negativeData.isEmpty()){
			throw new IllegalArgumentException("Negative data can not be null or empty");
		}
		negativeDataset.add(negativeData);
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
}
