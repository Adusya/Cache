package ru.unisuite.cache.metadatastore;

import java.util.Map;

import ru.unisuite.cache.cacheexception.CachePropertiesException;

public class MongoProperties {

	public String dbName;
	public String dbCollectionName;
	public String statisticsCollectionName;
	public String staticticsFieldName;
	public String ip;
	public int port;
	public String userName;
	public char[] userPassword;
	public int errorsLimit;
	public int waitingConnectionTime;
	public Long periodCheckConnectionTime;

	public MongoProperties(Map<String, Object> properties) throws CachePropertiesException {
		
		dbName = properties.get(MongoParamName.dbName).toString();
		dbCollectionName = properties.get(MongoParamName.dbCollectionName).toString();
		statisticsCollectionName = properties.get(MongoParamName.statisticsCollectionName).toString();
		staticticsFieldName = properties.get(MongoParamName.staticticsFieldName).toString();
		ip = properties.get(MongoParamName.ip).toString();
		
		userName = properties.get(MongoParamName.userName).toString();
		userPassword = properties.get(MongoParamName.userPassword).toString().toCharArray();
		
		try {
			
			port = Integer.parseInt(properties.get(MongoParamName.port).toString());
			errorsLimit = Integer.parseInt(properties.get(MongoParamName.errorsLimit).toString());
			waitingConnectionTime = Integer.parseInt(properties.get(MongoParamName.waitingConnectionTime).toString());
			periodCheckConnectionTime = Long.parseLong(properties.get(MongoParamName.periodCheckConnectionTime).toString());

		} catch (NumberFormatException e) {
			
			throw new CachePropertiesException("Wrong format of properties. " + e.getMessage());
		}
	
	}
	
	public long getPeriodCheckConnectionTime() {
		return periodCheckConnectionTime;
	}
	public int getErrorsLimit() {
		return errorsLimit;
	}
	public String getDbName() {
		return dbName;
	}
	public String getDbCollectionName() {
		return dbCollectionName;
	}
	public String getStatisticsCollectionName() {
		return statisticsCollectionName;
	}
	public String getStaticticsFieldName() {
		return staticticsFieldName;
	}
	public String getIp() {
		return ip;
	}
	public int getPort() {
		return port;
	}
	public String getUserName() {
		return userName;
	}
	public char[] getUserPassword() {
		return userPassword;
	}
	public int getWaitingConnectionTime() {
		return waitingConnectionTime;
	}


	
}
