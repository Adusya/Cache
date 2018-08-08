package ru.unisuite.cache.circiutbreaker;

public class CircuitBreakerState {
	
	public String state;

	public static CircuitBreakerState CLOSED = new CircuitBreakerState("CLOSED");
	
	public static CircuitBreakerState HALF_OPEN = new CircuitBreakerState("HALF_OPEN");
	
	public static CircuitBreakerState OPEN = new CircuitBreakerState("OPEN");
	
	public CircuitBreakerState(String state) {
		
		this.state = state;
		
	}
}
