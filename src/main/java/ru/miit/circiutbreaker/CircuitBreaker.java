package ru.miit.circiutbreaker;

import ru.miit.cacheexception.CacheStartFailedException;
import ru.miit.metadatastore.MetadataStore;
import ru.miit.metadatastore.MongoMetadataStore;
import ru.miit.metadatastore.MongoProperties;

public class CircuitBreaker {

	CircuitBreakerState state;
	
	private Long lastStateChanged;
	private Long timeToHalfOpenWait = 20000L;
	
	MongoProperties mongoProperties;
	
	private int errorsCount = 0;
	private int errorsLimit;
	
	public CircuitBreaker(MongoProperties mongoProperties) {
		
		this.mongoProperties = mongoProperties;
		errorsLimit = mongoProperties.getErrorsLimit();
		
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
		System.out.println("timeIsExpired: " + (lastStateChanged + timeToHalfOpenWait < System.currentTimeMillis()));
		return lastStateChanged + timeToHalfOpenWait < System.currentTimeMillis();
		
	}
	
	private void reOpen() {
		
		state = CircuitBreakerState.OPEN;
		lastStateChanged = System.currentTimeMillis();
		errorsCount = 0;
		System.out.println("state: " + state.state + "   errorsNum: " + errorsCount);
		
	}
	
	private void increaseErrorsNumber() {
		
		errorsCount++;
		
		if (errorsCount >= errorsLimit) {
			
			errorsCount = 0;
			state = CircuitBreakerState.OPEN;
			
		}
		System.out.println("state: " + state.state + "   errorsNum: " + errorsCount);
		
	}
	
	private void reset() {
		
		errorsCount = 0;
		state = CircuitBreakerState.CLOSED;
		System.out.println("state: " + state.state + "   errorsNum: " + errorsCount);
		
	}		
	
}
