package ru.miit.cache;

import ru.miit.cacheexception.CachePropertiesException;

public class CacheInstance {
	
	public CacheProperties cacheProperties;
	
	public String configFileName = "cacheConfig.xml";
	
	TimeChecker timeChecker;
	
	public CacheInstance(String configFilePath) throws CachePropertiesException {
		
		this.cacheProperties = new CacheProperties(configFilePath);
		
		timeChecker = new TimeChecker();
		timeChecker.start(cacheProperties);
		
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
