package controler;
import model.Entity;

public class Test {

	public static void main(String[] args) {
//		Dictionary dic = new Dictionary();
//		dic.merge(new AnchorText("Salam"), new Entity("www.google.com"));
//		dic.merge(new AnchorText("Salam"), new Entity("www.yahoo.com"));
//		dic.merge(new AnchorText("Bye"), new Entity("www.facebook.com"));
//		dic.printToXLS();
		System.out.println("lorem     ipsum     dolor \n sit.".replaceAll("\\s+", " "));
		
		String a = "my name is's farshad ! and I am good#";
		System.err.println(AnchorTextToEntity.refactor(a, new Entity("farshad","  ","FARSHAD","TEST")));
	}

}
