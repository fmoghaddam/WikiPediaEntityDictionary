package controller;

import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.TokenizerFactory;

/**
 * This class convert dataset files to structure which Stanford NER can read for custom NER training
 * @author fbm
 *
 */
public class TrainCustomNER {

	private static final String POSITIVE_DATA = "./result/23.10.2017/positive.log";
	private static final String NEGATIVE_DATA = "./result/23.10.2017/negativeDifficult.log";
	private static final float TEST_TRAIN_PERCENTAGE = 0.9f;
	public static void main(String[] args) throws IOException {
		final List<String> positiveLines = Files.readAllLines(Paths.get(POSITIVE_DATA), StandardCharsets.UTF_8);
		final List<String> negativeLines = Files.readAllLines(Paths.get(NEGATIVE_DATA), StandardCharsets.UTF_8);

		final TrainTestData positiveTTD = sampleData(positiveLines,TEST_TRAIN_PERCENTAGE);
		final TrainTestData negativeTTD = sampleData(negativeLines,TEST_TRAIN_PERCENTAGE);
		
		writeDataToFile(positiveTTD.getTrainSet(), negativeTTD.getTrainSet(),"train.tsv");
		writeDataToFile(positiveTTD.getTestSet(), negativeTTD.getTestSet(),"test.tsv");
	}

	private static void writeDataToFile(List<String> positiveTrainSet, List<String> negativeTrainSet, String fileName) {
		final TokenizerFactory<Word> tf = PTBTokenizer.factory();
		final List<Tuple> positiveResult = new ArrayList<>();
		for (int i = 5; i < positiveTrainSet.size(); i++) {
			final String line = positiveTrainSet.get(i);
			final String[] split = line.split("\t");
			String data = split[3];

			data = data.replaceAll("<.?r>", "");
			final List<Word> tokens_words = tf.getTokenizer(new StringReader(data)).tokenize();

			boolean inside = false;
			for (Word w : tokens_words) {
				if (!inside) {
					if (w.value().equals("<a>")) {
						inside = true;
						continue;
					} else if (w.value().equals("</a>")) {
						continue;
					} else {
						positiveResult.add(new Tuple(w.value(), "O"));
					}
				} else {
					if (w.value().equals("</a>")) {
						inside = false;
						continue;
					} else {
						positiveResult.add(new Tuple(w.value(), "ROLE"));
					}
				}
			}
			final Tuple tuple = new Tuple("", "");
			positiveResult.add(tuple);			
		}
		
		final List<Tuple> negativeResult = new ArrayList<>();
		for (int i = 5; i < negativeTrainSet.size(); i++) {
			final String line = negativeTrainSet.get(i);
			final String[] split = line.split("\t");
			String data = split[3];

			data = data.replaceAll("<.?r>", "");
			final List<Word> tokens_words = tf.getTokenizer(new StringReader(data)).tokenize();
			boolean inside = false;
			for (Word w : tokens_words) {
				if (!inside) {
					if (w.value().equals("<a>")) {
						inside = true;
						continue;
					} else if (w.value().equals("</a>")) {
						continue;
					} else {
						negativeResult.add(new Tuple(w.value(), "O"));
					}
				} else {
					if (w.value().equals("</a>")) {
						inside = false;
						continue;
					} else {
						negativeResult.add(new Tuple(w.value(), "NO_ROLE"));
					}
				}
			}
			final Tuple tuple = new Tuple("", "");
			negativeResult.add(tuple);			
		}
		positiveResult.addAll(negativeResult);
		writeToFile(positiveResult, "NER/"+fileName);
	}

	private static TrainTestData sampleData(List<String> lines, double threshold) {
		int total = lines.size();
		int size = (int) (total*(threshold));
		final Set<Integer> indexes = new HashSet<>();
		while(indexes.size()<size) {
			indexes.add((int)(Math.random() * total));
		}
		final List<String> train = new ArrayList<>();
		final List<String> test = new ArrayList<>();
		
		for(int i:indexes) {
			train.add(lines.get(i));
		}
		
		for(int i=0;i<total;i++) {
			if(!indexes.contains(i)) {
				test.add(lines.get(i));
			}
		}
		
		return new TrainTestData(train, test);
	}

	private static void writeToFile(List<Tuple> result, String filename) {
		try {
			final FileWriter fw = new FileWriter(filename, true); // the true will append the new data
			for (Tuple t : result) {
				fw.write(t.a + "\t" + t.b+"\n");// appends the string to the file
			}
			fw.close();
		} catch (IOException ioe) {
			System.err.println("IOException: " + ioe.getMessage());
		}
	}

	public static class Tuple {
		public String a;
		public String b;

		public Tuple(String w, String b) {
			this.a = w;
			this.b = b;
		}

		@Override
		public String toString() {
			return "Tuple [a=" + a + ", b=" + b + "]";
		}

	}

}
