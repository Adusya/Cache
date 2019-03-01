package ru.unisuite.cache.timechecker;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import ru.unisuite.cache.PersistentCache;
import ru.unisuite.cache.CacheProperties;
import ru.unisuite.cache.cacheexception.CacheMetadataStoreConnectionException;
import ru.unisuite.cache.circiutbreaker.CircuitBreaker;

public class TimeChecker {

	TimeCheckerProperties timeCheckerProperties;
	
	public ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();
	
	public TimeChecker (TimeCheckerProperties timeCheckerProperties) {
		
		this.timeCheckerProperties = timeCheckerProperties;
		
	}

	public void start(PersistentCache persistentCache, CacheProperties cacheProperties) {

		Runnable pinger = new Runnable() {
			public void run() {
					List<Object> overdueList = persistentCache.getOverdueList();
					for (Object ItemToDelete : overdueList) {
						if (persistentCache.isUp) {
							try {
								persistentCache.deleteItem(ItemToDelete.toString());
							} catch (CacheMetadataStoreConnectionException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}


			}
		};

		ses.scheduleAtFixedRate(pinger, 5000, timeCheckerProperties.getCheckPeriod(), TimeUnit.MILLISECONDS);
	}

	public void close() {

		ses.shutdown();

	}

}
