package model;

import java.util.HashSet;
import java.util.Set;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import org.apache.log4j.Logger;

public class Dictionary {

	private static final Logger LOG = Logger.getLogger(Dictionary.class.getCanonicalName());
	private final ConcurrentHashMap<AnchorText, Set<MapEntity>> dic = new ConcurrentHashMap<>();
	private static final BiFunction<Set<MapEntity>, Set<MapEntity>, Set<MapEntity>> biFunction = (oldSet,
			entity) -> {
		try {
			final Entity newEntity = entity.stream().findFirst().get().getEntity();
			Stream<MapEntity> filter = oldSet.stream()
					.filter(p -> p.getEntity().getUrl().equals(newEntity.getUrl()));
			if (filter.count() != 0) {
				Optional<MapEntity> findFirst = oldSet.stream().filter(
						p -> p.getEntity().getUrl().equals(newEntity.getUrl()))
						.findFirst();
				findFirst.get().increment();
			}else{
				oldSet.add(new MapEntity(newEntity));
			}
//			Stream<MapEntity> filter = oldSet.stream()
//					.filter(p -> p.getEntity().getUrl().equals(newEntity));
//			if (filter.count() != 0) {
//				Optional<MapEntity> findFirst = oldSet.stream().filter(
//						p -> p.getEntity().getUrl().equals(newEntity))
//						.findFirst();
//				findFirst.get().increment();
//			} else {
//				oldSet.add(entity.stream().findFirst().get());
//			}
		} catch (Exception exception) {
			LOG.error(exception);
		}
		return oldSet;
		
	};

	public void merge(final AnchorText anchorText, final Entity value) {
		if (anchorText == null || value == null) {
			throw new IllegalArgumentException();
		}
		final Set<MapEntity> set = new HashSet<>();
		final MapEntity mapEntity = new MapEntity(value);	
		set.add(mapEntity);
		dic.merge(anchorText, set, biFunction);
		dic.keySet().stream().filter(p -> p.equals(anchorText)).findFirst().get().increment();

	}

	public void printResult() {
		for (final Entry<AnchorText, Set<MapEntity>> entry : dic.entrySet()) {
			StringBuilder result = new StringBuilder();
			result.append(entry.getKey().getAnchorText()).append(";").append(entry.getKey().getFrequency()).append(";");
			for (MapEntity mapEntity : entry.getValue()) {
				result.append(mapEntity.getEntity().getEntityName()).append(";").append(mapEntity.getFrequency()).append(";");
			}
			LOG.info(result.toString());
		}
		
	}

	@Override
	public String toString() {
		return "Dictionary [dic=" + dic + "]";
	}
}
