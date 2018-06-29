package ru.miit.cache.metadatastore;

import java.util.List;
import java.util.Map;

import org.bson.Document;

public interface MetadataStore {

		void put(final String id, final Map<String, Object> parameters);
		void update(final Document document, final String id); 
		boolean exists(final String id); 
		Object getValue(final String id, final String field); 
		Object getLastUpdated(); 
		Object getLastUpdated(final String nodeName); 
		List<Object> getOverdueList();
		List<Object> getfullList();
		void updateTime(final String id);
		Map<String, Object> getParameters(final String id); 
		void delete(final String id);
		void reduceSize();
		void close();
		boolean connectionIsUp();
		void applyDowntime(final long downtime);
		void allowAccess(String idInCache);
		void increaseHits();
		void increaseMisses();
		Map<String, Object> getStatistics();
		void clearStatistics();
}
