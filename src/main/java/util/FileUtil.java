package util;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import model.Tuple;

public class FileUtil {
	public static void writeDataToFile(List<String> data,final String fileName) {
		final Path file = Paths.get(fileName);
		try {
			Files.write(file, data, Charset.forName("UTF-8"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void writeToFile(List<Tuple> result, String filename,String Splitter) {
		try {
			final FileWriter fw = new FileWriter(filename, true); // the true will append the new data
			for (Tuple t : result) {
				fw.write(t.a + Splitter + t.b+"\n");// appends the string to the file
			}
			fw.close();
		} catch (IOException ioe) {
			System.err.println("IOException: " + ioe.getMessage());
		}
	}
}
