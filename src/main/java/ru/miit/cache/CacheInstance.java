package ru.miit.cache;

import ru.miit.cacheexception.CachePropertiesException;
import ru.miit.cacheexception.CacheStartFailedException;
import ru.miit.circiutbreaker.CircuitBreaker;
import ru.miit.timechecker.TimeChecker;

public class CacheInstance {
	
	public CacheProperties cacheProperties;
	
	TimeChecker timeChecker;
	CircuitBreaker circuitBreaker;
	
	public CacheInstance(String configFilePath) throws CacheStartFailedException {
		
		try {
			this.cacheProperties = new CacheProperties(configFilePath);
			circuitBreaker = new CircuitBreaker(cacheProperties.getMongoProperties());
			
			if (cacheProperties.getTimeCheckerProperties().isEnable()) {
				timeChecker = new TimeChecker(cacheProperties.getTimeCheckerProperties());
				timeChecker.start(circuitBreaker, cacheProperties);
			}
			
		} catch (CachePropertiesException e) {
			throw new CacheStartFailedException("Cache cannot start. " + e.getMessage());
		}
	}
	
	public CacheProperties getCacheProperties() {
		
		return cacheProperties;
		
	}
	
	public Cache getCache() {
		
		Cache cache = new Cache(circuitBreaker, cacheProperties);		
		
		return cache;
		
	}
	
	public void close() {
		
		timeChecker.close();
		
	}
}
