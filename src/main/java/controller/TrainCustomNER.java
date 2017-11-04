package controller;

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
import model.DataSourceType;
import model.RoleListProvider;
import model.TrainTestData;
import util.FileUtil;

/**
 * This class convert dataset files to a structure which Stanford NER can read for custom NER training
 * @author fbm
 *
 */
public class TrainCustomNER {

	private static final String POSITIVE_DATA = "./result/23.10.2017/positive.log";
	private static final String NEGATIVE_DATA = "./result/23.10.2017/negativeDifficult.log";
	private static final float TEST_PERCENTAGE = 0.1f;
	private static final boolean ADD_DICTIONARY_TO_DATA = false;
	/**
	 * We have to types of train/test data.<a>XXXX<r>YYY</r>ZZZ</a>
	 * - only considering roles as ROLE
	 * - considering anchor text as ROLE
	 */
	private static final boolean ONLY_ROLE = true;

	public static void main(String[] args) throws IOException {
		final List<String> positiveLines = Files.readAllLines(Paths.get(POSITIVE_DATA), StandardCharsets.UTF_8);
		final List<String> negativeLines = Files.readAllLines(Paths.get(NEGATIVE_DATA), StandardCharsets.UTF_8);

		final TrainTestData positiveTTD = sampleData(positiveLines,TEST_PERCENTAGE);

		//Adding dictionary elements which have more than one words to train data
		if(ADD_DICTIONARY_TO_DATA) {
			addDictionaryToPositiveDataset(positiveTTD);
		}

		final TrainTestData negativeTTD = sampleData(negativeLines,TEST_PERCENTAGE);

		if(ONLY_ROLE) {
			writeDataToFileOnlyRole(positiveTTD.getTrainSet(), negativeTTD.getTrainSet(),"NER/trainOnlyRole.tsv");
			writeDataToFileOnlyRole(positiveTTD.getTestSet(), negativeTTD.getTestSet(),"NER/testOnlyRole.tsv");
		}else {
			writeDataToFileAnchorText(positiveTTD.getTrainSet(), negativeTTD.getTrainSet(),"NER/train.tsv");
			writeDataToFileAnchorText(positiveTTD.getTestSet(), negativeTTD.getTestSet(),"NER/test.tsv");
		}
	}

	private static void addDictionaryToPositiveDataset(TrainTestData positiveTTD) {
		final RoleListProvider dictionaries = new RoleListProviderFileBased();
		dictionaries.loadRoles(DataSourceType.WIKIPEDIA_LIST_OF_TILTES);
		dictionaries.loadRoles(DataSourceType.WIKIDATA_LABEL);
		final List<String> dicList = new ArrayList<>();
		for (final String role: dictionaries.getData().keySet()) {
			if(role.split(" ").length>1) {
				dicList.add("X\tX\tX\t"+role);
			}
		}
		positiveTTD.getTrainSet().addAll(dicList);
	}

	private static void writeDataToFileOnlyRole(List<String> positiveTrainSet, List<String> negativeTrainSet, String fileName) {
		final TokenizerFactory<Word> tf = PTBTokenizer.factory();
		final List<Tuple> positiveResult = new ArrayList<>();
		for (int i = 5; i < positiveTrainSet.size(); i++) {
			final String line = positiveTrainSet.get(i);
			final String[] split = line.split("\t");
			String data = split[3];

			if(data.contains("<a>")) {
				data = data.replaceAll("<.?a>", "");
			}
			final List<Word> tokens_words = tf.getTokenizer(new StringReader(data)).tokenize();

			boolean inside = false;
			for (Word w : tokens_words) {
				if (!inside) {
					if (w.value().equals("<r>")) {
						inside = true;
						continue;
					} else if (w.value().equals("</r>")) {
						continue;
					} else {
						positiveResult.add(new Tuple(w.value(), "O"));
					}
				} else {
					if (w.value().equals("</r>")) {
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
		FileUtil.writeToFile(positiveResult, fileName);
	}

	private static void writeDataToFileAnchorText(List<String> positiveTrainSet, List<String> negativeTrainSet, String fileName) {
		final TokenizerFactory<Word> tf = PTBTokenizer.factory();
		final List<Tuple> positiveResult = new ArrayList<>();
		for (int i = 5; i < positiveTrainSet.size(); i++) {
			final String line = positiveTrainSet.get(i);
			final String[] split = line.split("\t");
			String data = split[3];

			if(data.contains("<a>")) {
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
			}else {
				final List<Word> tokens_words = tf.getTokenizer(new StringReader(data)).tokenize();

				boolean inside = false;
				for (Word w : tokens_words) {
					if (!inside) {
						if (w.value().equals("<r>")) {
							inside = true;
							continue;
						} else if (w.value().equals("</r>")) {
							continue;
						} else {
							positiveResult.add(new Tuple(w.value(), "O"));
						}
					} else {
						if (w.value().equals("</r>")) {
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
		FileUtil.writeToFile(positiveResult,fileName);
	}

	private static TrainTestData sampleData(List<String> lines, double threshold) {
		int total = lines.size();
		int trainSize = (int) (total*(1-threshold));
		final Set<Integer> indexes = new HashSet<>();
		while(indexes.size()<trainSize) {
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
