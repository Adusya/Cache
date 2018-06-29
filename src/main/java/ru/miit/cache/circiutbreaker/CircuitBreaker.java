package ru.miit.cache.circiutbreaker;

import ru.miit.cache.cacheexception.CacheStartFailedException;
import ru.miit.cache.metadatastore.MetadataStore;
import ru.miit.cache.metadatastore.MongoMetadataStore;
import ru.miit.cache.metadatastore.MongoProperties;

public class CircuitBreaker {

	CircuitBreakerState state;
	
	private Long lastStateChanged;
	private Long timeToHalfOpenWait;
	
	public MongoProperties mongoProperties;
	
	private int errorsCount = 0;
	private int errorsLimit;
	
	public CircuitBreaker(MongoProperties mongoProperties) {
		
		this.mongoProperties = mongoProperties;
		errorsLimit = mongoProperties.getErrorsLimit();
		timeToHalfOpenWait = mongoProperties.getPeriodCheckConnectionTime();
		state = CircuitBreakerState.CLOSED;
	}
			
	public MetadataStore getMetadataStore() throws CacheStartFailedException {
		
		if (state == CircuitBreakerState.CLOSED) {
			
			MetadataStore metadataSotre = new MongoMetadataStore(mongoProperties);
			
			if (!metadataSotre.connectionIsUp()) {
				
				reOpen();
				throw new CacheStartFailedException("Cache cannot connect to MongoDB.");
			}
			
			return metadataSotre;
			
		} else {
			
			if (state == CircuitBreakerState.HALF_OPEN || isTimeExpired()) {
				
				state = CircuitBreakerState.HALF_OPEN;
				
				MetadataStore metadataSotre = new MongoMetadataStore(mongoProperties);
				
				if (!metadataSotre.connectionIsUp()) {
					
					increaseErrorsNumber();
					throw new CacheStartFailedException("Cache cannot connect to MongoDB.");
				}
				
				reset();
				return metadataSotre;
				
			}
			
		}
		
		throw new CacheStartFailedException("Cache cannot connect to MongoDB.");
		
	}
	
	private boolean isTimeExpired() {
		return lastStateChanged + timeToHalfOpenWait < System.currentTimeMillis();
		
	}
	
	private void reOpen() {
		
		state = CircuitBreakerState.OPEN;
		lastStateChanged = System.currentTimeMillis();
		errorsCount = 0;
		
	}
	
	private void increaseErrorsNumber() {
		
		errorsCount++;
		
		if (errorsCount >= errorsLimit) {
			
			errorsCount = 0;
			lastStateChanged = System.currentTimeMillis();
			state = CircuitBreakerState.OPEN;
			
		}
		
	}
	
	private void reset() {
		
		errorsCount = 0;
		state = CircuitBreakerState.CLOSED;
		
	}		
	
}
