package ru.miit.timechecker;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import ru.miit.cache.Cache;
import ru.miit.cache.CacheProperties;
import ru.miit.cacheexception.CacheStartFailedException;
import ru.miit.circiutbreaker.CircuitBreaker;

public class TimeChecker {

	TimeCheckerProperties timeCheckerProperties;
	
	public ScheduledExecutorService ses = Executors.newScheduledThreadPool(2);
	
	public TimeChecker (TimeCheckerProperties timeCheckerProperties) {
		
		this.timeCheckerProperties = timeCheckerProperties;
		
	}

	public void start(CircuitBreaker circuitBreaker, CacheProperties cacheProperties) {

		Runnable pinger = new Runnable() {
			public void run() {

				Cache cache = null;
				try {
					cache = new Cache(circuitBreaker, cacheProperties);
	
					List<Object> overdueList = cache.getOverdueList();
	
					for (Object ItemToDelete : overdueList) {
						if (cache.isUp)
							cache.deleteItem(ItemToDelete.toString());
					}
				} finally {
					
					if (cache != null)
						cache.close();
				}

			}
		};

		ses.scheduleAtFixedRate(pinger, 5, timeCheckerProperties.getCheckPeriod(), TimeUnit.SECONDS);
	}

	public void close() {

		ses.shutdown();

	}

}
