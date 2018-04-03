package ru.miit.metadatastore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.MongoException;
import com.mongodb.ServerAddress;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

public class MongoMetadataStore implements MetadataStore {

	public MongoClient mongoClient;
	public MongoDatabase database;
	public String collectionName;
	public MongoCollection<Document> collection;
	
	private static String statsFieldName;
	private static final String whereOperator = "$where";
	
	public MongoCollection<Document> statsCollection;

	public MongoMetadataStore(MongoProperties mongoProperties) {

		String userName = mongoProperties.getUserName();
		char[] userPassword = mongoProperties.getUserPassword();
		String databaseName = mongoProperties.getDbName();
		String ip = mongoProperties.getIp();
		int port = (int) mongoProperties.getPort();
		int waitingConnectionTime = mongoProperties.getWaitingConnectionTime() * 1000;
		
		MongoCredential credential = MongoCredential.createCredential(userName, databaseName, userPassword);
		MongoClientOptions options =  new MongoClientOptions.Builder().serverSelectionTimeout(waitingConnectionTime).build();
		mongoClient = new MongoClient(new ServerAddress(ip, port), Arrays.asList(credential), options);

		database = mongoClient.getDatabase(databaseName);
		collectionName = mongoProperties.getDbCollectionName();
		collection = database.getCollection(collectionName);  
		
		String statsCollectionName = mongoProperties.getStatisticsCollectionName();
		statsFieldName = mongoProperties.getStaticticsFieldName();
		statsCollection = database.getCollection(statsCollectionName);

	}

	@Override
	public void put(final String id, final Map<String, Object> parameters) {

		Document document = new Document(MongoParamName.id, id)
				.append(MongoParamName.fileName, parameters.get(MongoParamName.fileName))
				.append(MongoParamName.size, parameters.get(MongoParamName.size))
				.append(MongoParamName.hash, parameters.get(MongoParamName.hash))
				.append(MongoParamName.location, parameters.get(MongoParamName.location))
				.append(MongoParamName.updateTime, System.currentTimeMillis())
				.append(MongoParamName.timeToLive, parameters.get(MongoParamName.timeToLive))
				.append(MongoParamName.timeToIdle, parameters.get(MongoParamName.timeToIdle))
				.append(MongoParamName.contentType, parameters.get(MongoParamName.contentType))
				.append(MongoParamName.node, parameters.get(MongoParamName.node))
				.append(MongoParamName.type, parameters.get(MongoParamName.type));

		if (exists(id)) {
			update(document, id);
		} else {
			document.append(MongoParamName.creatingTime, System.currentTimeMillis());
			document.append(MongoParamName.pending, Boolean.TRUE);
			collection.insertOne(document);
		}

	} 

	@Override
	public void close() {
		
		if(connectionIsUp()) {
			mongoClient.close();
		}
		
	}
	
	@Override
	public boolean connectionIsUp() {
		
		try {
			mongoClient.getAddress();
			return true;
		} catch (Exception e) {
			return false;
		}
		
	}

	@Override
	public boolean exists(final String id) {
		Bson filter = new Document(MongoParamName.id, id);
//		Object pending =  getValue(id, MongoParamName.pending);
		if (collection.find(filter).first() == null) {// || pending == null || (Boolean) pending == true) {
			return false;
		}
		else {
			return true;
		}
	}

	@Override
	public Object getValue(final String id, final String field) {

		Bson filter = new Document(MongoParamName.id, id);
		Document document = collection.find(filter).first();

		if (document == null) {

			return null;
		} else {
			String result = document.toJson();
			Document resultJSON = Document.parse(result);

			return resultJSON.get(field);
		}
	}

	@Override
	public void delete(final String id) {

		Bson filter = new Document(MongoParamName.id, id);

		collection.deleteOne(filter);

	}

	@Override
	public void update(final Document document, final String id) {
		Bson filter = new Document(MongoParamName.id, id);
		collection.updateOne(filter, new Document("$set", document));
	}

	@Override
	public void reduceSize() {

		Bson command = new Document("compact", collectionName);
		database.runCommand(command);

	}

	@Override
	public Map<String, Object> getParameters(final String id) {

		Map<String, Object> parameters = new HashMap<>();

		Bson filter = new Document(MongoParamName.id, id);
		Document document = collection.find(filter).first();
		if (document == null) {
			return null;
		} else {
			String result = document.toJson();
			Document resultJSON = Document.parse(result);

			parameters.put(id, resultJSON.get(MongoParamName.id));

			if (resultJSON.get(MongoParamName.size) != null)
				parameters.put(MongoParamName.size, resultJSON.get(MongoParamName.size));

			if (resultJSON.get(MongoParamName.hash) != null)
				parameters.put(MongoParamName.hash, resultJSON.get(MongoParamName.hash));

			if (resultJSON.get(MongoParamName.location) != null)
				parameters.put(MongoParamName.location, resultJSON.get(MongoParamName.location));

			if (resultJSON.get(MongoParamName.updateTime) != null)
				parameters.put(MongoParamName.updateTime, resultJSON.get(MongoParamName.updateTime));

			if (resultJSON.get(MongoParamName.contentType) != null)
				parameters.put(MongoParamName.contentType, resultJSON.get(MongoParamName.contentType));

			if (resultJSON.get(MongoParamName.node) != null)
				parameters.put(MongoParamName.node, resultJSON.get(MongoParamName.node));

			if (resultJSON.get(MongoParamName.type) != null)
				parameters.put(MongoParamName.type, resultJSON.get(MongoParamName.type));

			return parameters;
		}

	}

	@Override
	public Object getLastUpdated() {

		// 1-по убыванию, -1-возрастанию
		Bson sort = new Document(MongoParamName.updateTime, 1);
		Bson filter = new Document(MongoParamName.updateTime, new Document("$exists", "true"));

		Document document = collection.find(filter).sort(sort).first();
		if (document == null) {
			return null;
		} else {
			String result = document.toJson();
			Document resultJSON = Document.parse(result);

			return resultJSON.get(MongoParamName.id);

		}
	}

	public Object getLastUpdated(final String nodeName) {

		// 1-по убыванию, -1-возрастанию
		Bson sort = new Document(MongoParamName.updateTime, 1);
		Bson nodeFilter = new Document(MongoParamName.node, nodeName);
		Bson filter = new Document(MongoParamName.updateTime, new Document("$exists", "true"));

		Document document = collection.find(filter).filter(nodeFilter).sort(sort).first();
		if (document == null) {
			return null;
		} else {
			String result = document.toJson();
			Document resultJSON = Document.parse(result);

			return resultJSON.get(MongoParamName.id);

		}

	}

	public List<Object> getOverdueList() {

		List<Object> overdueList = new ArrayList<Object>();
		
		Bson ttlFilter = new Document(whereOperator, "this." + MongoParamName.creatingTime + "+this."
				+ MongoParamName.timeToLive + " <= " + System.currentTimeMillis());
		Bson ttiFilter = new Document(whereOperator, "this." + MongoParamName.updateTime + "+this."
				+ MongoParamName.timeToIdle + " <= " + System.currentTimeMillis());

		Bson ttlZeroFilter = new Document(whereOperator, "this." + MongoParamName.timeToLive + " != 0 ");
		Bson ttiZeroFilter = new Document(whereOperator, "this." + MongoParamName.timeToIdle + " != 0 ");

		Bson ttlCheckFilter = Filters.and(ttlFilter, ttlZeroFilter);
		Bson ttiCheckFilter = Filters.and(ttiFilter, ttiZeroFilter); 

		Bson orFilter = Filters.or(ttlCheckFilter, ttiCheckFilter);

		FindIterable<Document> iterable = collection.find(orFilter);
		for (Document document : iterable) {
			if (document != null) {

				String result = document.toJson();
				Document resultJSON = Document.parse(result);

				overdueList.add(resultJSON.get(MongoParamName.id));
			}

		}
		return overdueList;
	}

	@Override
	public List<Object> getfullList() {

		List<Object> fullList = new ArrayList<Object>();

		FindIterable<Document> iterable = collection.find();

		for (Document document : iterable) {
			if (document != null) {

				String result = document.toJson();
				Document resultJSON = Document.parse(result);

				fullList.add(resultJSON.get(MongoParamName.id));
			}

		}
		return fullList;
	}

	@Override
	public void updateTime(final String id) {

		Document document = new Document(MongoParamName.id, id).append(MongoParamName.updateTime,
				System.currentTimeMillis());

		update(document, id);

	}
	
	@Override
	public void applyDowntime(final long downtime) {
		
		Bson filter = new Document();
		
		Bson update = new Document(MongoParamName.creatingTime, downtime).append(MongoParamName.updateTime, downtime);
		
		// $inc - увеличение значения на заданную величину
		Bson updateInc = new Document("$inc", update);
		
		collection.updateMany(filter, updateInc);
	}
	
//	@Override
//	public void allowAccess(String idInCache) {
//		
//		Document document = new Document(MongoParamName.id, idInCache).append(MongoParamName.pending,
//				Boolean.FALSE);
//		
//		update(document, idInCache);
//		
//	}
	
	@Override
	public void increaseHits() {
		
		Bson filter = new Document(MongoParamName.id, statsFieldName);
		
		Bson update = new Document(MongoParamName.cacheHits, 1).append(MongoParamName.cacheMisses, 0);
		
		Bson updateInc = new Document("$inc", update);
		
		statsCollection.updateMany(filter, updateInc);
			
	}
	
	@Override
	public void increaseMisses() {
		
		Bson filter = new Document(MongoParamName.id, statsFieldName);
		
		Bson update = new Document(MongoParamName.cacheHits, 0).append(MongoParamName.cacheMisses, 1);
		
		Bson updateInc = new Document("$inc", update);
		
		statsCollection.updateMany(filter, updateInc);
			
	}
	
	@Override
	public Map<String, Object> getStatistics() {
		
		Bson filter = new Document(MongoParamName.id, statsFieldName);
		
		Document document = statsCollection.find(filter).first();
		
		Map<String, Object> statisticsMap = new HashMap<>();
		if (document == null) {
			
			statisticsMap.put(MongoParamName.cacheHits, 0L);
			statisticsMap.put(MongoParamName.cacheMisses, 0L);
			
			Document clearStatsDocument = new Document(MongoParamName.id, statsFieldName)
					.append(MongoParamName.cacheHits, 0L)
					.append(MongoParamName.cacheMisses, 0L);
			
			statsCollection.insertOne(clearStatsDocument);
			
		} else {
			
			String result = document.toJson();
			Document resultJSON = Document.parse(result);

			long hits = resultJSON.getLong(MongoParamName.cacheHits);
			long misses = resultJSON.getLong(MongoParamName.cacheMisses);
			
			statisticsMap.put(MongoParamName.cacheHits, hits);
			statisticsMap.put(MongoParamName.cacheMisses, misses);

		}
		
		return statisticsMap;
	}
	
	@Override
	public void clearStatistics() {
		
		Bson filter = new Document(MongoParamName.id, statsFieldName);
		
		Document statsDocument = statsCollection.find(filter).first();
		
		if (statsDocument != null) {
			
			Bson update = new Document(MongoParamName.cacheHits, 0L)
					.append(MongoParamName.cacheMisses, 0L);
			
			Bson updateSet = new Document("$set", update);
			
			statsCollection.updateOne(filter, updateSet);
			
		}
		
	}
	
}
