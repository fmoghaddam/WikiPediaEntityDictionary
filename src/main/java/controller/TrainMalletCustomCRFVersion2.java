package controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import model.TrainTestData;
import model.Tuple;
import util.FileUtil;
import util.NERTagger;

/**
 * This class convert dataset files to a structure which MALLET CRF can read for
 * training
 * 
 * This version works with output of {@link DatasetGeneratorWithCategoryTrees4thVersion}}
 * @author fbm
 *
 */
public class TrainMalletCustomCRFVersion2 {

	private static final int CHUNKING_SIZE = 2000;
	private static int NUMBER_OF_THREADS = 2;
	private static final String READ_SPLITTER = "\t";
	private static final String WRITE_SPLITTER = " ";
	private static final String POSITIVE_DATA = "./result/06.11.2017-2/positive.log";
	private static final String NEGATIVE_DATA = "./result/06.11.2017-2/negativeDifficult.log";
	private static final float TEST_PERCENTAGE = 0.0f;

	public static void main(String[] args) throws IOException {
		
		NUMBER_OF_THREADS = Integer.parseInt(args[0]);
		
		final List<String> positiveLines = Files.readAllLines(Paths.get(POSITIVE_DATA), StandardCharsets.UTF_8);
		final List<String> negativeLines = Files.readAllLines(Paths.get(NEGATIVE_DATA), StandardCharsets.UTF_8);

		final TrainTestData positiveTTD = sampleData(positiveLines, TEST_PERCENTAGE);
		final TrainTestData negativeTTD = sampleData(negativeLines, TEST_PERCENTAGE);

		//writeDataToFileAnchorText(positiveTTD.getTrainSet(), negativeTTD.getTrainSet(), "MALLETCRF/train.tsv");
		//writeDataToFileAnchorText(positiveTTD.getTestSet(), negativeTTD.getTestSet(), "MALLETCRF/test.tsv");

		generateFullDataset(positiveTTD, negativeTTD);
	}

	@SuppressWarnings("unused")
	private static void writeDataToFileOnlyRole(List<String> positiveTrainSet, List<String> negativeTrainSet,
			String fileName) {
		final TokenizerFactory<Word> tf = PTBTokenizer.factory();
		final List<Tuple> positiveResult = new ArrayList<>();
		for (int i = 2; i < positiveTrainSet.size(); i++) {
			final String line = positiveTrainSet.get(i);
			String data = line;

			if (data.contains("<a>")) {
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
		for (int i = 2; i < negativeTrainSet.size(); i++) {
			final String line = negativeTrainSet.get(i);
			String data = line;

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
		FileUtil.writeToFile(positiveResult, fileName, WRITE_SPLITTER);
	}

	@SuppressWarnings("unused")
	private static void writeDataToFileAnchorText(List<String> positiveTrainSet, List<String> negativeTrainSet,
			String fileName) {
		final TokenizerFactory<Word> tf = PTBTokenizer.factory();
		final List<Tuple> positiveResult = new ArrayList<>();
		for (int i = 5; i < positiveTrainSet.size(); i++) {
			final String line = positiveTrainSet.get(i);
			final String[] split = line.split(READ_SPLITTER);
			String data = split[3];

			if (data.contains("<a>")) {
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
			} else {
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
			}
			final Tuple tuple = new Tuple("", "");
			positiveResult.add(tuple);
		}

		final List<Tuple> negativeResult = new ArrayList<>();
		for (int i = 5; i < negativeTrainSet.size(); i++) {
			final String line = negativeTrainSet.get(i);
			final String[] split = line.split(READ_SPLITTER);
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
		FileUtil.writeToFile(positiveResult, fileName, WRITE_SPLITTER);
	}

	private static void generateFullDataset(TrainTestData positiveTTD, TrainTestData negativeTTD) {
		try {
			final ExecutorService executor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
			for (int i = 2; i < positiveTTD.getTrainSet().size(); i=Math.min(i+CHUNKING_SIZE,positiveTTD.getTrainSet().size())) {
				executor.execute(handle(positiveTTD.getTrainSet(),i,Math.min(i+CHUNKING_SIZE, positiveTTD.getTrainSet().size()),true));
				System.err.println("Thread started. POSITIVE");
			}			
			for (int i = 2; i < negativeTTD.getTrainSet().size(); i=Math.min(i+CHUNKING_SIZE,positiveTTD.getTrainSet().size())) {
				executor.execute(handle(negativeTTD.getTrainSet(),i,Math.min(i+CHUNKING_SIZE, negativeTTD.getTrainSet().size()),false));
				System.err.println("Thread started. NEGATIVE");
			}
			executor.shutdown();
			executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		}catch(Exception e) {
			e.printStackTrace();
		}

//		for (int i = 2; i < positiveTTD.getTrainSet().size(); i++) {
//			final String line = positiveTTD.getTrainSet().get(i);
//			final String taggedLine = line;
//			final String noTaggedLine = taggedLine.replaceAll("<.?r>", "").replaceAll("<.?a>","");
//			final Map<Integer, Map<String, String>> result = nerXmlParser(NERTagger.runTaggerXML(noTaggedLine));
//
//			addContextFeatures(result, 2);
//
//			try{
//				addPositivLabels(result, taggedLine, noTaggedLine);
//			}catch(Exception e) {
//				continue;
//			}
//
//			List<String> finalResult = new ArrayList<>();
//			finalResult.add(taggedLine);
//			finalResult.add(noTaggedLine);
//
//			for (Map<String, String> entity : result.values()) {
//				final StringBuilder l = new StringBuilder();
//				for(Entry<String, String> a:entity.entrySet()) {
//					l.append(a.getKey()).append(" ");
//				}
//				finalResult.add(l.toString());
//				break;
//			}
//
//			for (Entry<Integer, Map<String, String>> entity : result.entrySet()) {
//				final Map<String, String> value = entity.getValue();
//				final StringBuilder l = new StringBuilder();
//				for (String s : value.values()) {
//					l.append(s).append(" ");
//				}
//				finalResult.add(l.toString());
//			}
//			FileUtil.writeDataToFile(finalResult, "CRF/"+(i-1)+"Positive.txt");
//		}

//		for (int i = 2; i < negativeTTD.getTrainSet().size(); i++) {
//			final String line = negativeTTD.getTrainSet().get(i);
//			final String taggedLine = line;
//			final String noTaggedLine = taggedLine.replaceAll("<.?r>", "").replaceAll("<.?a>","");
//			final Map<Integer, Map<String, String>> result = nerXmlParser(NERTagger.runTaggerXML(noTaggedLine));
//
//			addContextFeatures(result, 2);
//
//			addNegativeLabels(result);
//
//			List<String> finalResult = new ArrayList<>();
//			finalResult.add(taggedLine);
//			finalResult.add(noTaggedLine);
//
//			for (Map<String, String> entity : result.values()) {
//				final StringBuilder l = new StringBuilder();
//				for(Entry<String, String> a:entity.entrySet()) {
//					l.append(a.getKey()).append(" ");
//				}
//				finalResult.add(l.toString());
//				break;
//			}
//
//			for (Entry<Integer, Map<String, String>> entity : result.entrySet()) {
//				final Map<String, String> value = entity.getValue();
//				final StringBuilder l = new StringBuilder();
//				for (String s : value.values()) {
//					l.append(s).append(" ");
//				}
//				finalResult.add(l.toString());
//			}
//			FileUtil.writeDataToFile(finalResult, "CRF/"+(i-1)+"Negative.txt");
//		}
	}

	private static Runnable handle(List<String> data, int start, int end,boolean isPositive) {
		System.err.println(start +" ---- " + end);
		final Runnable r = () -> {
			for (int i = start; i < end; i++) {
				final String line = data.get(i);
				final String taggedLine = line;
				final String noTaggedLine = taggedLine.replaceAll("<.?r>", "").replaceAll("<.?a>","");
				final Map<Integer, Map<String, String>> result = nerXmlParser(NERTagger.runTaggerXML(noTaggedLine));

				addContextFeatures(result, 2);

				try{
					if(isPositive) {
						addPositivLabels(result, taggedLine, noTaggedLine);
					}else {
						addNegativeLabels(result);
					}
				}catch(Exception e) {
					continue;
				}

				List<String> finalResult = new ArrayList<>();
				finalResult.add(taggedLine);
				finalResult.add(noTaggedLine);

				for (Map<String, String> entity1 : result.values()) {
					final StringBuilder l1 = new StringBuilder();
					for(Entry<String, String> a:entity1.entrySet()) {
						l1.append(a.getKey()).append(" ");
					}
					finalResult.add(l1.toString());
					break;
				}

				for (Entry<Integer, Map<String, String>> entity2 : result.entrySet()) {
					final Map<String, String> value = entity2.getValue();
					final StringBuilder l2 = new StringBuilder();
					for (String s : value.values()) {
						l2.append(s).append(" ");
					}
					finalResult.add(l2.toString());
				}
				FileUtil.writeDataToFile(finalResult, "CRF/"+(i-1)+(isPositive?"Positive.txt":"Negative.txt"));
			}
		};
		return r;
	}

	private static void addNegativeLabels(Map<Integer, Map<String, String>> result) {
		for (int i = 0; i < result.size(); i++) {
			result.get(i).put("TAG", "O");
		}
	}

	private static void addPositivLabels(Map<Integer, Map<String, String>> result, String taggedLine,
			String noTaggedLine) {
		final TokenizerFactory<Word> tf = PTBTokenizer.factory();
		int wordCount = 0;
		try {
			if (taggedLine.contains("<a>")) {
				taggedLine = taggedLine.replaceAll("<.?r>", "");
				final List<Word> taggedSentence_tokens_words = tf.getTokenizer(new StringReader(taggedLine)).tokenize();

				boolean inside = false;

				for (int i = 0; i < taggedSentence_tokens_words.size(); i++) {
					final Word taggedWord = taggedSentence_tokens_words.get(i);
					if (!inside) {
						if (taggedWord.value().equals("<a>")) {
							inside = true;
							continue;
						} else if (taggedWord.value().equals("</a>")) {
							continue;
						} else {
							result.get(wordCount++).put("TAG", "O");
						}
					} else {
						if (taggedWord.value().equals("</a>")) {
							inside = false;
							continue;
						} else {
							result.get(wordCount++).put("TAG", "ROLE");
						}
					}
				}
			} else {
				final List<Word> tokens_words = tf.getTokenizer(new StringReader(taggedLine)).tokenize();

				boolean inside = false;
				for (Word w : tokens_words) {
					if (!inside) {
						if (w.value().equals("<r>")) {
							inside = true;
							continue;
						} else if (w.value().equals("</r>")) {
							continue;
						} else {
							result.get(wordCount++).put("TAG", "O");
						}
					} else {
						if (w.value().equals("</r>")) {
							inside = false;
							continue;
						} else {
							result.get(wordCount++).put("TAG", "ROLE");
						}
					}
				}
			}
		} catch (NullPointerException e) {
			throw e;
		}
	}

	/**
	 * 
	 * @param result
	 * @param windowSize how many words consider before and after main word
	 */
	private static void addContextFeatures(Map<Integer, Map<String, String>> result, int windowSize) {
		for (final Entry<Integer, Map<String, String>> entity : result.entrySet()) {
			final Integer wordPosition = entity.getKey();

			for (int i = 1; i <= windowSize; i++) {
				Map<String, String> previousWord = getFeaturesOfNeighborWord(wordPosition, wordPosition - i, result, i);
				result.put(wordPosition, previousWord);
			}

			for (int i = 1; i <= windowSize; i++) {
				Map<String, String> nextWord = getFeaturesOfNeighborWord(wordPosition, wordPosition + i, result, i);
				result.put(wordPosition, nextWord);
			}
		}
	}

	private static Map<String, String> getFeaturesOfNeighborWord(int wordPosition, int neighborPosition,
			Map<Integer, Map<String, String>> result, int i) {
		String letter = "P";
		if (neighborPosition > wordPosition) {
			letter = "N";
		}
		if (!result.containsKey(neighborPosition)) {
			final Map<String, String> wordFeature = result.get(wordPosition);
			wordFeature.put(letter + i, "NIL");
			wordFeature.put(letter + i + "Pos", "NIL");
			wordFeature.put(letter + i + "Ner", "NIL");
			return wordFeature;
		} else {
			final Map<String, String> list = result.get(neighborPosition);
			final Map<String, String> wordFeature = result.get(wordPosition);
			wordFeature.put(letter + i, list.get("word"));
			wordFeature.put(letter + i + "Pos", list.get("POS"));
			wordFeature.put(letter + i + "Ner", list.get("NER"));
			return wordFeature;
		}

	}

	public static Map<Integer, Map<String, String>> nerXmlParser(final String xml) {
		try {
			Map<Integer, Map<String, String>> result = new LinkedHashMap<>();
			final DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
			final DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
			final org.w3c.dom.Document document = docBuilder
					.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
			final Map<String, String> features = new LinkedHashMap<>();
			final NodeList nodeList = document.getElementsByTagName("*");
			int wordPosition = 0;
			int wordCounter = 0;
			for (int i = 0; i < nodeList.getLength(); i++) {
				final Node node = nodeList.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().equals("token")) {
					if (node.hasChildNodes()) {
						for (int j = 0; j < node.getChildNodes().getLength(); j++) {
							final Node childNode = node.getChildNodes().item(j);

							if (childNode.getNodeType() == Node.ELEMENT_NODE) {
								if (childNode.getNodeName().equals("word")) {
									features.put("ID", String.valueOf(wordCounter++));
									features.put("word", childNode.getTextContent());
									if (childNode.getTextContent().charAt(0) >= 65
											&& childNode.getTextContent().charAt(0) <= 90) {
										features.put("STARTCAP", "true");
									} else {
										features.put("STARTCAP", "false");
									}
									if(StringUtils.isAllUpperCase(childNode.getTextContent())) {
										features.put("ALLCAP", "true");
									}else {
										features.put("ALLCAP", "false");
									}
								} else if (childNode.getNodeName().equals("lemma")) {
									features.put("lemma", childNode.getTextContent());
								} else if (childNode.getNodeName().equals("POS")) {
									features.put("POS", childNode.getTextContent());
								} else if (childNode.getNodeName().equals("NER")) {
									features.put("NER", childNode.getTextContent());
								}
							}
						}
					}
					final Map<String, String> map = new LinkedHashMap<>();
					map.putAll(features);
					result.put(wordPosition++, map);
					features.clear();
				}
			}
			return result;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private static TrainTestData sampleData(List<String> lines, double threshold) {
		int total = lines.size();
		int trainSize = (int) (total * (1 - threshold));
		final Set<Integer> indexes = new HashSet<>();
		while (indexes.size() < trainSize) {
			indexes.add((int) (Math.random() * total));
		}
		final List<String> train = new ArrayList<>();
		final List<String> test = new ArrayList<>();

		for (int i : indexes) {
			train.add(lines.get(i));
		}

		for (int i = 0; i < total; i++) {
			if (!indexes.contains(i)) {
				test.add(lines.get(i));
			}
		}

		return new TrainTestData(train, test);
	}

}
