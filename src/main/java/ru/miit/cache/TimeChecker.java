package ru.miit.cache;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import ru.miit.cacheexception.CacheStartFailedException;

public class TimeChecker {

	public ScheduledExecutorService ses = Executors.newScheduledThreadPool(2);

	public void start(CacheProperties cacheProperties) {

		Runnable pinger = new Runnable() {
			public void run() {

				Cache cache = null;
				try {
					cache = new Cache(cacheProperties);
	
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

		ses.scheduleAtFixedRate(pinger, 5, 20, TimeUnit.SECONDS);
	}

	public void close() {

		ses.shutdown();

	}

}
