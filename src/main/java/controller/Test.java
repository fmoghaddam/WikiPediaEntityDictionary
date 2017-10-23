package controller;
import java.awt.image.TileObserver;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import model.Category;
import model.CategoryTrees;
import model.Document;

public class Test {

	public static void main(String[] args) throws IOException {
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
//		String text = "Lincoln's portrait appears on $5 bill two denominations of United States currency, the penny and the $5 bill.";
		
//		System.err.println(text.replace("$5 bill", "<a>$5 bill</a>"));
		//System.err.println(Pattern.quote("(he*.)"));
		
//		try {
//			List<String> lines = Files.readAllLines(Paths.get("wikipediafiles/AA/wiki_00"), StandardCharsets.UTF_8);			
//			System.err.println(lines.size());
////			for(String s: lines) {
////				System.out.println(s);
////			}
//			
			//String text = new String(Files.readAllBytes(Paths.get("wikipediafiles/AA/wiki_00")), StandardCharsets.UTF_8);
			List<Document>  documents = DatasetGeneratorWithCategoryTrees3rdVersion.getDocuments("wikipediafiles/AA/wiki_00");
			for(Document d:documents) {
				System.err.println(d.getTitle());
			}
			
//			
//			final Pattern titlePattern = Pattern.compile("<doc.* url=\".*\" title=\".*\">");
//			final Matcher titleMatcher = titlePattern.matcher(text);
//			while(titleMatcher.find()) {
//				System.err.println(titleMatcher.group(0));
//			}											
//			
//			String[] title = StringUtils.substringsBetween(text, "<doc", "</doc>");
//			System.err.println(title.length);
////			for(int i=0;i<title.length;i++) {
////				System.err.println(title[i]);
////			}
//
//			String pattern1 = "<doc.* url=\\\".*\\\" title=\\\".*\\\">";
//			String pattern2 = "</doc>";
//			
//			Pattern p = Pattern.compile(pattern1 + "(.*?)" + Pattern.quote(pattern2));
//			Matcher m = p.matcher(text);
//			while (m.find()) {
//			  System.out.println(m.group(1));
//			}
//			
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		
	}

	
}
