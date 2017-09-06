package model;

public enum Category {
	HEAD_OF_STATE_TAG("president"),
	POPE_TAG("pope"), 
	MONARCH_TAG("king"), 
	CHAIR_PERSON_TAG("ceo");

	private String text;

	Category(String text) {
		this.text = text;
	}

	public String text() {
		return text;
	}
	
	public static Category resolve(String text){
		for(Category cat: Category.values()){
			if(cat.text().equals(text.toLowerCase())){
				return cat;
			}
		}
		return null;
	}
}