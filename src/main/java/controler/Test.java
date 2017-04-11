package controler;

import model.AnchorText;
import model.Dictionary;
import model.Entity;

public class Test {

	public static void main(String[] args) {
		Dictionary dic = new Dictionary();
		dic.merge(new AnchorText("Salam"), new Entity("www.google.com"));
		dic.merge(new AnchorText("Salam"), new Entity("www.yahoo.com"));
		dic.merge(new AnchorText("Bye"), new Entity("www.facebook.com"));
		dic.printToXLS();
	}

}
