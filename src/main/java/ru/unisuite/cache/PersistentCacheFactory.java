package ru.unisuite.cache;

import java.io.IOException;

import ru.unisuite.cache.cacheexception.CachePropertiesException;
import ru.unisuite.cache.cacheexception.CacheStartFailedException;
import ru.unisuite.cache.circiutbreaker.CircuitBreaker;
import ru.unisuite.cache.timechecker.TimeChecker;

public class PersistentCacheFactory {
	
	public CacheProperties cacheProperties;
	
	TimeChecker timeChecker;
	CircuitBreaker circuitBreaker;
	
	private PersistentCache persistentCache;
	
	public PersistentCacheFactory(String configFilePath) throws CacheStartFailedException {
		
		try {
			this.cacheProperties = new CacheProperties(configFilePath);
//			circuitBreaker = new CircuitBreaker(cacheProperties.getMongoProperties());
			
			persistentCache = new PersistentCache(cacheProperties);
			
//			if (cacheProperties.getTimeCheckerProperties().isEnable()) {
//				timeChecker = new TimeChecker(cacheProperties.getTimeCheckerProperties());
//				timeChecker.start(persistentCache, cacheProperties);
//			}
			
		} catch (CachePropertiesException | IOException e) {
			throw new CacheStartFailedException("PersistentCache cannot start. " + e.getMessage());
		}
	}
	
	public CacheProperties getCacheProperties() {
		
		return cacheProperties;
		
	}
	
	public PersistentCache getCache() {	
		
		return persistentCache;
		
	}
	
	public void close() {
		
		if (timeChecker != null)
			timeChecker.close();
		
		if (persistentCache != null)
			persistentCache.close();
		
	}
}
