package ru.miit.cache;

import ru.miit.cacheexception.CachePropertiesException;
import ru.miit.cacheexception.CacheStartFailedException;
import ru.miit.circiutbreaker.CircuitBreaker;
import ru.miit.timechecker.TimeChecker;

public class CacheInstance {
	
	public CacheProperties cacheProperties;
	
	TimeChecker timeChecker;
	CircuitBreaker circuitBreaker;
	
	Cache cache;
	
	public CacheInstance(String configFilePath) throws CacheStartFailedException {
		
		try {
			this.cacheProperties = new CacheProperties(configFilePath);
//			circuitBreaker = new CircuitBreaker(cacheProperties.getMongoProperties());
			
			cache = new Cache(cacheProperties);
			
			if (cacheProperties.getTimeCheckerProperties().isEnable()) {
				timeChecker = new TimeChecker(cacheProperties.getTimeCheckerProperties());
				timeChecker.start(cache, cacheProperties);
			}
			
		} catch (CachePropertiesException e) {
			throw new CacheStartFailedException("Cache cannot start. " + e.getMessage());
		}
	}
	
	public CacheProperties getCacheProperties() {
		
		return cacheProperties;
		
	}
	
	public Cache getCache() {	
		
		return cache;
		
	}
	
	public void close() {
		
		if (timeChecker != null)
			timeChecker.close();
		
		if (cache != null)
			cache.close();
		
	}
}
