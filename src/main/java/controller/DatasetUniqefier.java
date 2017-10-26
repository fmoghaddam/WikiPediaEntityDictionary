package controller;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DatasetUniqefier {
	
	private static final String POSITIVE_DATA = "./result/23.10.2017/positive.log";
	private static final String NEGATIVE_DATA = "./result/23.10.2017/negativeDifficult.log";
	
	private static final Map<String,Set<String>> positiveMap = new HashMap<>();
	private static final Map<String,Set<String>> negativeMap = new HashMap<>();
	
	public static void main(String[] args) throws IOException {		
		final List<String> positiveLines = Files.readAllLines(Paths.get(POSITIVE_DATA), StandardCharsets.UTF_8);
		final List<String> negativeLines = Files.readAllLines(Paths.get(NEGATIVE_DATA), StandardCharsets.UTF_8);
		
		for (int i = 5; i < positiveLines.size(); i++) {
			final String posLine = positiveLines.get(i);			
			final String posSentence = posLine.split("\t")[5];
			if(posSentence.length()>1500) {
				continue;
			}
			final String punctuationRemoved = posSentence.replaceAll("[^\\w\\s]", "");
			Set<String> set = positiveMap.get(punctuationRemoved.toLowerCase());
			if(set==null || set.isEmpty()) {
				Set<String> newSet = new HashSet<>();
				newSet.add(posSentence);
				positiveMap.put(punctuationRemoved.toLowerCase(), newSet);
			}else {
				set.add(posSentence);
				positiveMap.put(punctuationRemoved.toLowerCase(), set);
			}
		}
		
		writeDataToFile(positiveMap,"positiveUniq.txt");
		
		for (int i = 5; i < negativeLines.size(); i++) {
			final String negLine = negativeLines.get(i);
			final String negSentence = negLine.split("\t")[5];
			if(negSentence.length()>1500) {
				continue;
			}
			final String punctuationRemoved = negSentence.replaceAll("[^\\w\\s]", "");
			Set<String> set = negativeMap.get(punctuationRemoved.toLowerCase());
			if(set==null || set.isEmpty()) {
				Set<String> newSet = new HashSet<>();
				newSet.add(negSentence);
				negativeMap.put(punctuationRemoved.toLowerCase(), newSet);
			}else {
				set.add(negSentence);
				negativeMap.put(punctuationRemoved.toLowerCase(), set);
			}
		}
		
		writeDataToFile(negativeMap,"negativeUniq.txt");
	}

	private static void writeDataToFile(Map<String, Set<String>> data,final String fileName) {
		final Path file = Paths.get(fileName);
		final List<String> lines = new ArrayList<>();
		for(Set<String> set:data.values()) {
			List<String> a = new ArrayList<>(set);
			lines.add(a.get(0));
		}		
		try {
			Files.write(file, lines, Charset.forName("UTF-8"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
