package util;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.LexedTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.WordToSentenceProcessor;

public class HTMLLinkExtractor {

	private Pattern patternTag, patternLink;
	private Matcher matcherTag, matcherLink;

	private static final String HTML_A_TAG_PATTERN = "(?i)<a([^>]+)>(.+?)</a>";
	private static final String HTML_A_HREF_TAG_PATTERN =
			"\\s*(?i)href\\s*=\\s*(\"([^\"]*\")|'[^']*'|([^'\">\\s]+))";


	public HTMLLinkExtractor() {
		patternTag = Pattern.compile(HTML_A_TAG_PATTERN);
		patternLink = Pattern.compile(HTML_A_HREF_TAG_PATTERN);
	}

	/**
	 * Validate html with regular expression
	 *
	 * @param html
	 *            html content for validation
	 * @return Vector links and link text
	 */
	public Vector<HtmlLink> grabHTMLLinks(final String html) {

		Vector<HtmlLink> result = new Vector<HtmlLink>();


		List<CoreLabel> tokens = new ArrayList<CoreLabel>();
		
		final LexedTokenFactory<CoreLabel> tokenFactory = new CoreLabelTokenFactory();
		
		final PTBTokenizer<CoreLabel> tokenizer = new PTBTokenizer<CoreLabel>(new StringReader(html), tokenFactory, "untokenizable=noneDelete");
		
		while (tokenizer.hasNext()) {
			tokens.add(tokenizer.next());
		}
		
		final List<List<CoreLabel>> sentences = new WordToSentenceProcessor<CoreLabel>().process(tokens);
		int end;
		int start = 0;
		final ArrayList<String> sentenceList = new ArrayList<String>();
		for (List<CoreLabel> sentence: sentences) {
			end = sentence.get(sentence.size()-1).endPosition();
			sentenceList.add(html.substring(start, end).trim());
			start = end;
		}
		for(final String sentenceString :sentenceList){
			final String sentenceWithoutHtmlTag = sentenceString.replaceAll("<[^>]*>", "");
			matcherTag = patternTag.matcher(sentenceString);

			while (matcherTag.find()) {

				String href = matcherTag.group(1); // href
				String linkText = matcherTag.group(2); // link text

				matcherLink = patternLink.matcher(href);

				while (matcherLink.find()) {

					String link = matcherLink.group(1); // link
					HtmlLink obj = new HtmlLink();
					obj.setLink(link);
					obj.setLinkText(linkText);
					obj.setFullSentence(sentenceWithoutHtmlTag);
					result.add(obj);
				}
			}
		}
		return result;
	}

	public class HtmlLink {

		String link;
		String linkText;
		String fullSentence;

		HtmlLink(){};

		public void setFullSentence(String sentenceWithoutHtmlTag) {
			fullSentence = sentenceWithoutHtmlTag;
		}

		@Override
		public String toString() {
			return "HtmlLink [link=" + link + ", linkText=" + linkText + ", fullSentence=" + fullSentence + "]";
		}

		public String getLink() {
			return link;
		}

		public String getFullSentence(){
			return fullSentence;
		}

		public void setLink(String link) {
			this.link = replaceInvalidChar(link);
		}

		public String getLinkText() {
			return linkText;
		}

		public void setLinkText(String linkText) {
			this.linkText = linkText;
		}

		private String replaceInvalidChar(String link){
			link = link.replaceAll("'", "");
			link = link.replaceAll("\"", "");
			return link;
		}

	}
}
