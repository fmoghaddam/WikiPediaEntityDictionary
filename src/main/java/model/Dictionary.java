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
	private final ConcurrentHashMap<String, Set<MapEntity>> dic = new ConcurrentHashMap<>();
	private static final BiFunction<Set<MapEntity>, Set<MapEntity>, Set<MapEntity>> biFunction = (beforeSet, entity) -> {
		try{
			Stream<MapEntity> filter = beforeSet.stream().filter(p -> p.getEntity().getUrl().equals(entity.stream().findFirst().get().getEntity().getUrl()));
			if(filter.count()!=0){
				Optional<MapEntity> findFirst = beforeSet.stream().filter(p -> p.getEntity().getUrl().equals(entity.stream().findFirst().get().getEntity().getUrl())).findFirst();
				findFirst.get().increment();
			}
		}catch(Exception exception){
			LOG.error(exception);
			System.err.println(entity);
			System.err.println(beforeSet);
		}
		return beforeSet;
	};

	public void merge(final String key, final Entity value) {
		if (key == null || value == null) {
			throw new IllegalArgumentException();
		}
		final Set<MapEntity> set = new HashSet<>();
		final MapEntity mapEntity = new MapEntity(value);
		mapEntity.increment();
		set.add(mapEntity);
		dic.merge(key, set, biFunction);
	}

	public void printResult() {
		for (final Entry<String, Set<MapEntity>> entry : dic.entrySet()) {
			for(MapEntity mapEntity: entry.getValue()){
				LOG.info(entry.getKey() + " ; " + mapEntity.getEntity().getEntityName() + " ; "+ mapEntity.getFrequency());
			}
		}
	}

	@Override
	public String toString() {
		return "Dictionary [dic=" + dic + "]";
	}
}
