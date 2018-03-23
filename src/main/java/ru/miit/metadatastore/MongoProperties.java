package ru.miit.metadatastore;

import java.util.Map;

public class MongoProperties {

	public String dbName;
	public String dbCollectionName;
	public String statisticsCollectionName;
	public String staticticsFieldName;
	public String ip;
	public int port;
	public String userName;
	public char[] userPassword;
	
	public MongoProperties(Map<String, Object> properties) {
		
		dbName = properties.get(MongoParamName.dbName).toString();
		dbCollectionName = properties.get(MongoParamName.dbCollectionName).toString();
		statisticsCollectionName = properties.get(MongoParamName.statisticsCollectionName).toString();
		staticticsFieldName = properties.get(MongoParamName.staticticsFieldName).toString();
		ip = properties.get(MongoParamName.ip).toString();
		port = Integer.parseInt(properties.get(MongoParamName.port).toString());
		userName = properties.get(MongoParamName.userName).toString();
		userPassword = properties.get(MongoParamName.userPassword).toString().toCharArray();
		
		System.out.println(ip + ":" + port + " " + userName + userPassword.toString());
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

	
}
