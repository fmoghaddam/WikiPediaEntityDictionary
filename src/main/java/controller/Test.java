package controller;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import model.Category;
import model.CategoryTrees;

public class Test {

	public static void main(String[] args) {
//		Dictionary dic = new Dictionary();
//		dic.merge(new AnchorText("Salam"), new Entity("www.google.com"));
//		dic.merge(new AnchorText("Salam"), new Entity("www.yahoo.com"));
//		dic.merge(new AnchorText("Bye"), new Entity("www.facebook.com"));
//		dic.printToXLS();
//		System.out.println("lorem     ipsum     dolor \n sit.".replaceAll("\\s+", " "));
		
//		String a = "my name is's the the the farshad ! and I am good#";
//		System.err.println(AnchorTextToEntity.refactor(a, new Entity("farshad","  ","FARSHAD","TEST")));
//		String b = " 1 ";
//		System.err.println(AnchorTextToEntity.refactor(b, new Entity("farshad","  ","FARSHAD",null)));
//		
//		String c = "XXXX";
//		System.err.println(AnchorTextToEntity.refactor(c, new Entity("farshad","  ","FARSHAD",null)));
	
//		String CATEGORY_TREE_FOLDER = "categoryTree";
//		final CategoryTrees categoryTrees = new CategoryTrees();
//		categoryTrees.load(CATEGORY_TREE_FOLDER);
//		
//		//for (String cat : categoriesOfEntity) {
//			Category existInAnyTree = categoryTrees.existInAnyTree("Chief_executive_officers");
//			if (existInAnyTree != null) {
//				System.err.println(existInAnyTree);
//			}
//		//}
		String text = "Lincoln's portrait appears on $5 bill two denominations of United States currency, the penny and the $5 bill.";
		
		System.err.println(text.replace("$5 bill", "<a>$5 bill</a>"));
		//System.err.println(Pattern.quote("(he*.)"));
	}

}
