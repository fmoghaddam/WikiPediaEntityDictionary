package model;

public class HtmlLink {

	String link;
	String linkText;
	String fullSentence;

	public HtmlLink(){};

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