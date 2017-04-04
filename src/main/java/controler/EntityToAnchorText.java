package controler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;

import org.apache.log4j.Logger;

import model.Entity;
import util.HTMLLinkExtractor;
import util.HTMLLinkExtractor.HtmlLink;

public class EntityToAnchorText {

	private static final Logger LOG = Logger.getLogger(EntityToAnchorText.class.getCanonicalName());
	private static final ConcurrentHashMap<Entity, Set<String>> DICTIONARY = new ConcurrentHashMap<>();
	private static final String WIKI_FILES_FOLDER = "data";
	private static final BiFunction<Set<String>, Set<String>, Set<String>> biFunction = (set, string) -> {
		set.addAll(string);
		return set;
	};
	private static Map<String, Entity> entityMap;
	private static final ExecutorService executor = Executors.newFixedThreadPool(45);

	public static void main(String[] args) {
		entityMap = EntityFileLoader.loadData();
		checkWikiPages(entityMap);
	}

	private static void checkWikiPages(final Map<String, Entity> entityMap) {
		try {
			final File[] listOfFolders = new File(WIKI_FILES_FOLDER).listFiles();
			Arrays.sort(listOfFolders);
			for (int i = 0; i < listOfFolders.length; i++) {
				final String subFolder = listOfFolders[i].getName();
				executor.execute(handle(WIKI_FILES_FOLDER + File.separator + subFolder + File.separator));
			}
			executor.shutdown();
			executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
			printResult();
		} catch (final Exception exception) {
			exception.printStackTrace();
		}

	}

	private static Runnable handle(String pathToSubFolder) {
		final Runnable r = new Runnable() {
			@Override
			public void run() {
				try {
					final File[] listOfFiles = new File(pathToSubFolder).listFiles();
					Arrays.sort(listOfFiles);
					for (int j = 0; j < listOfFiles.length; j++) {
						final String file = listOfFiles[j].getName();
						BufferedReader br = new BufferedReader(new FileReader(pathToSubFolder + File.separator + file));
						String line;
						while ((line = br.readLine()) != null) {
							line = line.toLowerCase();
							final HTMLLinkExtractor htmlLinkExtractor = new HTMLLinkExtractor();
							final Vector<HtmlLink> links = htmlLinkExtractor.grabHTMLLinks(line);
							for (Iterator<?> iterator = links.iterator(); iterator.hasNext();) {
								final HtmlLink htmlLink = (HtmlLink) iterator.next();
								final Entity entity = entityMap.get(htmlLink.getLink());
								if (entity != null) {
									final Set<String> set = new HashSet<>();
									set.add(htmlLink.getLinkText().trim());
									DICTIONARY.merge(entity, set, biFunction);
								}
							}
						}
						br.close();
					}
					System.out.println("Folder " + pathToSubFolder + " has been processed.");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};

		return r;
	}

	private static void printResult() {
		System.out.println("Size of map= " + DICTIONARY.size());
		for (final Entry<Entity, Set<String>> entry : DICTIONARY.entrySet()) {
			LOG.info(entry.getKey().getEntityName() + " ; " + entry.getValue() + " ; " + entry.getKey().getCategoryFolder());
		}
	}
}
