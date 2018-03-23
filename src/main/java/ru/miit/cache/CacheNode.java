package ru.miit.cache;

public class CacheNode {
	
	public String nodeName;
	public Long capacity;
	public String directory;
	public Long timeToLive;
	public Long timeToIdle;

	public CacheNode(String nodeName, Long capacity, String directory, Long timeToLive, Long timeToIdle) {
		
		this.nodeName = nodeName;
		this.capacity = capacity;
		this.directory = directory;
		this.timeToLive = timeToLive;
		this.timeToIdle = timeToIdle;

	}
	
	public Long getTimeToIdle() {
		return timeToIdle;
	}

	public void setTimeToIdle(Long timeToIdle) {
		this.timeToIdle = timeToIdle;
	}
	
	public Long getTimeToLive() {
		return timeToLive;
	}

	public void setTimeToLive(Long timeToLive) {
		this.timeToLive = timeToLive;
	}

	public String getNodeName() {
		return nodeName;
	}
	
	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}
	
	public Long getCapacity() {
		return capacity;
	}
	
	public void setCapacity(Long capacity) {
		this.capacity = capacity;
	}
	
	public String getDirectory() {
		return directory;
	}

	public void setDirectory(String directory) {
		this.directory = directory;
	}
	
}
