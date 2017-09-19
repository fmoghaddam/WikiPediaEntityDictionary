package util;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import model.Category;

public class POSTagger {

	private final static Properties props = new Properties();
	private final static StanfordCoreNLP pipeline;
	static {
		props.setProperty("annotators", "tokenize, ssplit, pos");
		pipeline = new StanfordCoreNLP(props);
	}
	enum POS_TAG {
		CC("Coordinating conjunction"), CD("Cardinal number"), DT("Determiner"), EX("Existential there"), FW(
				"Foreign word"), IN("Preposition or subordinating conjunction"), JJ("Adjective"), JJR(
						"Adjective, comparative"), JJS("Adjective, superlative"), LS("List item marker"), MD(
								"Modal"), NN("Noun, singular or mass"), NNS(
										"Noun, plural"), NNP("Proper noun, singular"), NNPS("Proper noun, plural"), PDT(
												"Predeterminer"), POS("Possessive ending"), PRP("Personal pronoun"),
		PRP$("Possessive pronoun"),
		RB("Adverb"), RBR("Adverb, comparative"), RBS("Adverb, superlative"), RP("Particle"), SYM("Symbol"), TO(
				"to"), UH("Interjection"), VB(
						"Verb, base form"), VBD("Verb, past tense"), VBG("Verb, gerund or present participle"), VBN(
								"Verb, past participle"), VBP("Verb, non-3rd person singular present"), VBZ(
										"Verb, 3rd person singular present"), WDT("Wh-determiner"), WP("Wh-pronoun"),
		WP$("Possessive wh-pronoun"),
		WRB("Wh-adverb");

		public String text;

		POS_TAG(String text) {
			this.text = text;
		}
	}

	public static Map<String, Set<Category>> generatePOSDictionary(Map<String, Set<Category>> originalDictionary) {
		final Map<String, Set<Category>> posDictinary = new TreeMap<>(String.CASE_INSENSITIVE_ORDER); 

		for(Entry<String, Set<Category>> entry:originalDictionary.entrySet()){
			final String text = entry.getKey();
			final Set<Category> categories = entry.getValue();
			try {			
				final String posTaggedResult = runPOSTagger(text);
				final String replaceWordsWithTags = replaceWordsWithTags(posTaggedResult, text);
				final Set<Category> set = posDictinary.get(replaceWordsWithTags);
				if(set==null){
					posDictinary.put(replaceWordsWithTags,new HashSet<>(categories));
				}else{
					Set<Category> newSet = new HashSet<>(set);
					newSet.addAll(categories);
					posDictinary.put(replaceWordsWithTags,newSet);
				}
			}
			catch (ClassCastException  e) {
				e.printStackTrace();
			}
		}
		return posDictinary;
	}
	
	public static Map<String, Set<Category>> generatePOSAndNERDictionary(Map<String, Set<Category>> originalDictionary) {
		final Map<String, Set<Category>> posDictinary = new TreeMap<>(String.CASE_INSENSITIVE_ORDER); 

		for(Entry<String, Set<Category>> entry:originalDictionary.entrySet()){
			final String text = entry.getKey();
			final Set<Category> categories = entry.getValue();
			try {			
				final String posTaggedResult = runPOSTaggerWithNoNER(text);
				final String replaceWordsWithTags = replaceWordsWithTagsButNotNER(posTaggedResult, text);
				final Set<Category> set = posDictinary.get(replaceWordsWithTags);
				if(set==null){
					posDictinary.put(replaceWordsWithTags,new HashSet<>(categories));
				}else{
					Set<Category> newSet = new HashSet<>(set);
					newSet.addAll(categories);
					posDictinary.put(replaceWordsWithTags,newSet);
				}
			}
			catch (ClassCastException  e) {
				e.printStackTrace();
			}
		}
		return posDictinary;
	}

//	public static String runPOSTagger2(String text) {
//		MaxentTagger tagger = new MaxentTagger("posmodel/english-left3words/english-left3words-distsim.tagger");
//		final String tagged = tagger.tagString(text);
//
//		return tagged;
//	}

	public static String runPOSTagger(String text) {
		final StringBuilder result = new StringBuilder();
		final Annotation annotation = new Annotation(text);
		pipeline.annotate(annotation);
		List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
		for (CoreMap sentence : sentences) {
			for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
				final String word = token.get(CoreAnnotations.TextAnnotation.class);
				final String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
				if (!pos.equals(word)) {
					result.append("<").append(pos).append(">").append(word).append("</").append(pos).append(">")
							.append(" ");
				} else {
					result.append(word).append(" ");
				}
			}
		}
		return result.toString();
	}

	public static String runPOSTaggerWithNoNER(String text) {

		StringBuilder result = new StringBuilder();
		Annotation annotation = new Annotation(text);
		pipeline.annotate(annotation);
		List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
		for (CoreMap sentence : sentences) {
			for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
				final String word = token.get(CoreAnnotations.TextAnnotation.class);
				final String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
				if (!isNERTag(word) && !pos.equals(word)) {
					result.append("<").append(pos).append(">").append(word).append("</").append(pos).append(">")
							.append(" ");
				} else {
					result.append(word).append(" ");
				}
			}
		}
		return result.toString();
	}

	private static boolean isNERTag(String word) {
		for (NER_TAG tag : NER_TAG.values()) {
			if (("<" + tag.name() + ">").equalsIgnoreCase(word)) {
				return true;
			}
		}
		return false;
	}

	public static String replaceWordsWithTags(String posTaggedResult, String originalText) {
		StringBuilder result = new StringBuilder();
		final Document doc = Jsoup.parse(posTaggedResult);

		for (Element element : doc.getAllElements()) {
			final POS_TAG tag = getPOSTag(element.nodeName());
			if (tag == null) {
				continue;
			} else {
				result.append("<" + tag + ">").append(" ");
			}
		}
		return result.toString();
	}

	public static String replaceWordsWithTagsButNotNER(String posTaggedResult, String originalText) {
		StringBuilder result = new StringBuilder();
		final Document doc = Jsoup.parse(posTaggedResult);

		for (Element element : doc.getAllElements()) {
			final POS_TAG tag = getPOSTag(element.nodeName());
			if (tag == null) {
				if (!isNERTag("<" + element.nodeName() + ">")) {
					continue;
				} else {
					result.append("<" + element.nodeName().toUpperCase() + ">").append(" ");
				}
			}
			if (tag != null && !isNERTag(element.html())) {
				// result = result.replace(element.html(),
				// "<"+element.nodeName().toUpperCase()+">");
				result.append("<" + tag + ">").append(" ");
			}
		}
		return result.toString();
	}

	private static POS_TAG getPOSTag(String nodeName) {
		for (final POS_TAG tag : POS_TAG.values()) {
			if (tag.name().equalsIgnoreCase(nodeName)) {
				return tag;
			}
		}
		return null;
	}
}
