package ru.unisuite.cache;

import java.io.IOException;

import ru.unisuite.cache.cacheexception.CachePropertiesException;
import ru.unisuite.cache.cacheexception.CacheStartFailedException;
import ru.unisuite.cache.circiutbreaker.CircuitBreaker;
import ru.unisuite.cache.timechecker.TimeChecker;

public class CacheInstance {
	
	public CacheProperties cacheProperties;
	
	TimeChecker timeChecker;
	CircuitBreaker circuitBreaker;
	
	private Cache cache;
	
	public CacheInstance(String configFilePath) throws CacheStartFailedException {
		
		try {
			this.cacheProperties = new CacheProperties(configFilePath);
//			circuitBreaker = new CircuitBreaker(cacheProperties.getMongoProperties());
			
			cache = new Cache(cacheProperties);
			
//			if (cacheProperties.getTimeCheckerProperties().isEnable()) {
//				timeChecker = new TimeChecker(cacheProperties.getTimeCheckerProperties());
//				timeChecker.start(cache, cacheProperties);
//			}
			
		} catch (CachePropertiesException | IOException e) {
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
