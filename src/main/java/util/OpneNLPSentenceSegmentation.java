package util;

import java.io.FileInputStream;
import java.io.InputStream;

import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.util.Span;

/**
 * This was just a test class to see how OpneNLP sentence tokenization works
 * @author fbm
 *
 */
public class OpneNLPSentenceSegmentation {

	public static void main(String[] args) {
		try (InputStream modelIn = new FileInputStream("en-sent.bin")) {
			SentenceModel model = new SentenceModel(modelIn);
			SentenceDetectorME sentenceDetector = new SentenceDetectorME(model);
			String sentences1[] = sentenceDetector.sentDetect("Pope Martin V (Constit. 3 \"In Apostolicae\", ii and v) fixed the manner for their examination and approbation and also the tax they should demand for their labour and the punishment for overcharge.");
			Span sentences2[] = sentenceDetector.sentPosDetect("Pope Martin V (Constit. 3 \"In Apostolicae\", ii and v) fixed the manner for their examination and approbation and also the tax they should demand for their labour and the punishment for overcharge.");
			
			for(String s:sentences1) {
				System.err.println(s);
			}
			
			for(Span s:sentences2) {
				System.err.println(s);
			}
		}catch(Exception e) {
			e.printStackTrace();
		}

	}

}
