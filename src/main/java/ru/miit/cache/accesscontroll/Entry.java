package ru.miit.cache.accesscontroll;

public class Entry {

	public String name;
	
	public boolean newbie;

	public int readers;

	public int writers;

	public Entry() {

		newbie = true;
		
		this.readers = 0;

		this.writers = 0;

	}	
	
	public Entry(int readers, int writers) {

		newbie = true;
		
		this.readers = readers;

		this.writers = writers;

	}

	public int increaseReaders() {

		return ++readers;

	}
	
	public void makeNotNewbie() {
		
		newbie = false;
		
	}

	public int increaseWriters() {

		return ++writers;

	}
	
	public int reduceReaders() {

		return readers -= 1;

	}

	public int reduceWriters() {

		return writers -= 1;

	}

	public int getReaders() {
		return readers;
	}

	public int getWriters() {
		return writers;
	}
	
	public String getName() {
		return name;
	}
	
	public boolean isNewbie() {
		return newbie;
	}
}
