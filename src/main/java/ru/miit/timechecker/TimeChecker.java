package ru.miit.timechecker;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import ru.miit.cache.Cache;
import ru.miit.cache.CacheProperties;
import ru.miit.circiutbreaker.CircuitBreaker;

public class TimeChecker {

	TimeCheckerProperties timeCheckerProperties;
	
	public ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();
	
	public TimeChecker (TimeCheckerProperties timeCheckerProperties) {
		
		this.timeCheckerProperties = timeCheckerProperties;
		
	}

	public void start(CircuitBreaker circuitBreaker, CacheProperties cacheProperties) {

		Runnable pinger = new Runnable() {
			public void run() {
				System.out.println("start");
				Cache cache = null;
				try {
					cache = new Cache(circuitBreaker, cacheProperties);
					System.out.println("inited cache");
					List<Object> overdueList = cache.getOverdueList();
					System.out.println("got overdueList");
					for (Object ItemToDelete : overdueList) {
						System.out.println("ready to delete");
						if (cache.isUp) {
							System.out.println("deleting...");
							cache.deleteItem(ItemToDelete.toString());
						}
					}
				} finally {
					
					if (cache != null) {
						System.out.println("closing cache");
						cache.close();
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
