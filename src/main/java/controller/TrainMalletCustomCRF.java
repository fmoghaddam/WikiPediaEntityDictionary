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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

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
 * @author fbm
 *
 */
public class TrainMalletCustomCRF {

	private static final String READ_SPLITTER = "\t";
	private static final String WRITE_SPLITTER = " ";
	private static final String POSITIVE_DATA = "./result/06.11.2017/positive.log";
	private static final String NEGATIVE_DATA = "./result/06.11.2017/negativeDifficult.log";
	private static final float TEST_PERCENTAGE = 0.0f;

	public static void main(String[] args) throws IOException {
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
		for (int i = 5; i < positiveTrainSet.size(); i++) {
			final String line = positiveTrainSet.get(i);
			final String[] split = line.split(READ_SPLITTER);
			String data = split[3];

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
		for (int i = 5; i < positiveTTD.getTrainSet().size(); i++) {
			final String line = positiveTTD.getTrainSet().get(i);
			final String[] split = line.split("\t");
			final String tag = split[0];
			final String taggedLine = split[3];
			final String noTaggedLine = split[5];
			final Map<Integer, Map<String, String>> result = nerXmlParser(NERTagger.runTaggerXML(noTaggedLine));
			addContextFeatures(result, 2);

			try{
				addPositivLabels(result, taggedLine, noTaggedLine);
			}catch(Exception e) {
				continue;
			}

			List<String> finalResult = new ArrayList<>();
			finalResult.add(tag);
			finalResult.add(taggedLine);
			finalResult.add(noTaggedLine);

			int counter = 0 ;
			for (Entry<Integer, Map<String, String>> entity : result.entrySet()) {
				final Map<String, String> value = entity.getValue();
				final StringBuilder l = new StringBuilder();
				l.append(counter++).append(" ");
				for (String s : value.values()) {
					l.append(s).append(" ");
				}
				finalResult.add(l.toString());
			}
			FileUtil.writeDataToFile(finalResult, "CRF/"+(i-4)+"Positive.txt");
		}

		for (int i = 5; i < negativeTTD.getTrainSet().size(); i++) {
			final String line = negativeTTD.getTrainSet().get(i);
			final String[] split = line.split("\t");
			final String tag = split[0];
			final String taggedLine = split[3];
			final String noTaggedLine = split[5];
			final Map<Integer, Map<String, String>> result = nerXmlParser(NERTagger.runTaggerXML(noTaggedLine));

			addContextFeatures(result, 2);

			addNegativeLabels(result);

			List<String> finalResult = new ArrayList<>();
			finalResult.add(tag);
			finalResult.add(taggedLine);
			finalResult.add(noTaggedLine);

			int counter = 0;
			for (Entry<Integer, Map<String, String>> entity : result.entrySet()) {
				final Map<String, String> value = entity.getValue();
				final StringBuilder l = new StringBuilder();
				l.append(counter++).append(" ");
				for (String s : value.values()) {
					l.append(s).append(" ");
				}
				finalResult.add(l.toString());
			}
			FileUtil.writeDataToFile(finalResult, "CRF/"+(i-4)+"Negative.txt");
		}
	}

	private static void addNegativeLabels(Map<Integer, Map<String, String>> result) {
		for (int i = 0; i < result.size(); i++) {
			result.get(i).put("TAG", "O");
		}
	}

	private static void addPositivLabels(Map<Integer, Map<String, String>> result, String taggedLine,
			String noTaggedLine) {
		final TokenizerFactory<Word> tf = PTBTokenizer.factory();
		// tf.setOptions("splitHyphenated=true");
		int wordCount = 0;
		try {
			if (taggedLine.contains("<a>")) {
				taggedLine = taggedLine.replaceAll("<.?r>", "");
				final List<Word> taggedSentence_tokens_words = tf.getTokenizer(new StringReader(taggedLine)).tokenize();

				boolean inside = false;

				for (int i = 0; i < taggedSentence_tokens_words.size(); i++) {
					final Word taggedWord = taggedSentence_tokens_words.get(i);
//					if (taggedWord.toString().equals(".") && wordCount >= result.size()) {
//						continue;
//					}
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
//					if (w.toString().equals(".") && wordCount > result.size()) {
//						continue;
//					}
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
			for (int i = 0; i < nodeList.getLength(); i++) {
				final Node node = nodeList.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().equals("token")) {
					if (node.hasChildNodes()) {
						for (int j = 0; j < node.getChildNodes().getLength(); j++) {
							final Node childNode = node.getChildNodes().item(j);

							if (childNode.getNodeType() == Node.ELEMENT_NODE) {
								if (childNode.getNodeName().equals("word")) {
									features.put("word", childNode.getTextContent());
									if (childNode.getTextContent().charAt(0) >= 65
											&& childNode.getTextContent().charAt(0) <= 90) {
										features.put("CAPITAL", "true");
									} else {
										features.put("CAPITAL", "false");
									}
								} else if (childNode.getNodeName().equals("lemma")) {
									features.put("lemma", childNode.getTextContent());
								} else if (childNode.getNodeName().equals("POS")) {
									features.put("POS", childNode.getTextContent());
								} else if (childNode.getNodeName().equals("NER")) {
									features.put("NER", childNode.getTextContent());
								}
								// else if (childNode.getNodeName().equals("CharacterOffsetBegin")) {
								// startPosition = Integer.parseInt(childNode.getTextContent());
								// } else if (childNode.getNodeName().equals("CharacterOffsetEnd")) {
								// endPosition = Integer.parseInt(childNode.getTextContent());
								// }
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
