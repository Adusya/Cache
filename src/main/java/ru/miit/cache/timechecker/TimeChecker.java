package ru.miit.cache.timechecker;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import ru.miit.cache.Cache;
import ru.miit.cache.CacheProperties;
import ru.miit.cache.circiutbreaker.CircuitBreaker;

public class TimeChecker {

	TimeCheckerProperties timeCheckerProperties;
	
	public ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();
	
	public TimeChecker (TimeCheckerProperties timeCheckerProperties) {
		
		this.timeCheckerProperties = timeCheckerProperties;
		
	}

	public void start(Cache cache, CacheProperties cacheProperties) {

		Runnable pinger = new Runnable() {
			public void run() {
					List<Object> overdueList = cache.getOverdueList();
					for (Object ItemToDelete : overdueList) {
						if (cache.isUp) {
							cache.deleteItem(ItemToDelete.toString());
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
