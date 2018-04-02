package ru.miit.timechecker;

public class TimeCheckerProperties {
	
	public boolean enable;
	
	public long checkPeriod;
	
	public TimeCheckerProperties(boolean enable, long checkPeriod) {
		
		this.enable = enable;
		
		this.checkPeriod = checkPeriod;
		
	}
	

	public boolean isEnable() {
		return enable;
	}

	public long getCheckPeriod() {
		return checkPeriod;
	}

}
