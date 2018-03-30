package ru.miit.cache;

import ru.miit.cacheexception.CachePropertiesException;
import ru.miit.cacheexception.CacheStartFailedException;

public class CacheInstance {
	
	public CacheProperties cacheProperties;
	
	TimeChecker timeChecker;
	
	public CacheInstance(String configFilePath) throws CacheStartFailedException {
		
		try {
			this.cacheProperties = new CacheProperties(configFilePath);
			
			timeChecker = new TimeChecker();
			timeChecker.start(cacheProperties);
			
		} catch (CachePropertiesException e) {
			throw new CacheStartFailedException("Cache cannot start. " + e.getMessage());
		}
	}
	
	public CacheProperties getCacheProperties() {
		
		return cacheProperties;
		
	}
	
	public Cache getCache() {
		
		Cache cache = new Cache(cacheProperties);		
		
		return cache;
		
	}
	
	public void close() {
		
		timeChecker.close();
		
	}
}
