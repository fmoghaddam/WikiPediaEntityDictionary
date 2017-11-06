package controller;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import util.CustomNERTagger;

/**
 * Run our external test data (currently from new york times) 
 * and report accuracy
 * @author fbm
 *
 */
public class TestStnfordCustomNER {
	private static final String POSITIVE_DATA = "/home/fbm/eclipse-workspace/General Data/RoleTaggerGroundTruth-master/Roles/test/test30/positive";
	private static final String NEGATIVE_DATA = "/home/fbm/eclipse-workspace/General Data/RoleTaggerGroundTruth-master/Roles/test/test30/negative";

	public static void main(String[] args) throws IOException {
		File[] listOfFiles = new File(POSITIVE_DATA).listFiles();

		float tp = 0;
		float tn = 0;
		float fp = 0;
		float fn = 0;

		for(File f:listOfFiles) {
			final List<String> positiveLines = Files.readAllLines(f.toPath(), StandardCharsets.UTF_8);
			for(String posLine:positiveLines) {
				String result = CustomNERTagger.runTaggerString(posLine);
				if(result.contains("<ROLE>")) {
					tp++;
				}else {
					fn++;
				}
			}
		}

		listOfFiles = new File(NEGATIVE_DATA).listFiles();
		for(File f:listOfFiles) {
			final List<String> negativeLines = Files.readAllLines(f.toPath(), StandardCharsets.UTF_8);
			for(String negLine:negativeLines) {
				String result = CustomNERTagger.runTaggerString(negLine);
				if(result.contains("<ROLE>")) {
					fp++;
				}else {
					tn++;
				}
			}
		}

		System.err.println("accuracy = "+(tp+tn)/(tp+tn+fp+fn));
		System.err.println("precision = "+(tp)/(tp+fp));
		System.err.println("recall = "+(tp)/(tp+fn));
	}
}
