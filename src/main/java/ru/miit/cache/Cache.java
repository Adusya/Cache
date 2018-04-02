package ru.miit.cache;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletResponse;

import com.mongodb.MongoException;

import ru.miit.cacheexception.CacheGetException;
import ru.miit.cacheexception.CacheMetadataStoreConnectionException;
import ru.miit.cacheexception.CachePropertiesException;
import ru.miit.cacheexception.CacheStartFailedException;
import ru.miit.circiutbreaker.CircuitBreaker;
import ru.miit.diskcache.DiskCache;
import ru.miit.metadatastore.MetadataStore;
import ru.miit.metadatastore.MongoMetadataStore;

public class Cache {

	private final Logger logger = CacheLogger.getLogger(Cache.class.getName());

	private MetadataStore metaDatabase;
	private DiskCache diskCache;
	private CacheProperties cacheProperties;
	public Boolean isUp = false;

	public ScheduledExecutorService executor = Executors.newScheduledThreadPool(10);
	
	final static public String defaultNodeName = "general";

	public Cache(CacheProperties cacheProperties) {

		this.cacheProperties = cacheProperties;

		CircuitBreaker circuitBreaker = new CircuitBreaker(cacheProperties.getMongoProperties());
		
		try {
			metaDatabase = circuitBreaker.getMetadataStore();
			isUp = true;
		} catch (CacheStartFailedException e) {
			isUp = false;
			//Logger
		}

		diskCache = new DiskCache(cacheProperties.getCacheDirectory());

	}

	public Boolean put(final String idinCache, Map<String, Object> parameters)
			throws CacheMetadataStoreConnectionException {

		if (!connectionIsUp()) {
			logger.log(Level.SEVERE, "requested connection is closed.");
			throw new CacheMetadataStoreConnectionException("Connection is closed. ");
		}

		if (idinCache == null) {

			logger.log(Level.SEVERE, "Illegal object id value. Id is null.");
			throw new IllegalArgumentException("The object id can't be empty");

		}

		String type = parameters.get(CacheParamName.type).toString();
		CacheNode node = cacheProperties.getNodeByName(type);

		String cacheDiractory = cacheProperties.getCacheDirectory();
		String nodeDirectory = node.getDirectory();
		String location = cacheDiractory + nodeDirectory + File.separator;

		parameters.put(CacheParamName.location, location);

		parameters.put(CacheParamName.node, node.getNodeName());
		parameters.put(CacheParamName.folder, nodeDirectory);

		parameters.put(CacheParamName.timeToLive, node.getTimeToLive());
		parameters.put(CacheParamName.timeToIdle, node.getTimeToIdle());

		parameters.put(CacheParamName.id, idinCache);

		trimToSize(parameters);
		
		metaDatabase.put(idinCache, parameters);

		return true;
	}

	public CompletableFuture<Boolean> putAsync(final String idInCache, final Map<String, Object> parameters) {

		if (!connectionIsUp()) {
			logger.log(Level.SEVERE, "requested connection is closed.");
			throw new CacheMetadataStoreConnectionException("Connection is closed. ");
		}

		if (idInCache == null) {

			logger.log(Level.SEVERE, "Illegal object id value. Id is null.");
			throw new IllegalArgumentException("The object id can't be empty. ");

		}
		
		CompletableFuture<Boolean> future = CompletableFuture.supplyAsync(() -> put(idInCache, parameters), executor);

		return future;

	}

	public void get(final String idInCache, final OutputStream out, HttpServletResponse response)
			throws CacheGetException, CacheMetadataStoreConnectionException {

		if (!connectionIsUp()) {
			logger.log(Level.SEVERE, "requested connection is closed.");
			throw new CacheMetadataStoreConnectionException("Connection is closed. ");
		}

		if (idInCache == null) {
			logger.log(Level.SEVERE, "Illegal object id value. Id is null.");
			throw new IllegalArgumentException("The object id can't be empty. ");
		}

		Map<String, Object> parameters = metaDatabase.getParameters(idInCache);

		String location = parameters.get(CacheParamName.location).toString();
		String fullLocation = location + idInCache;

		try {
			diskCache.get(fullLocation, out);

			String contentType = parameters.get(CacheParamName.contentType).toString();
			response.setContentType(contentType);

			int size = Integer.parseInt(parameters.get(CacheParamName.size).toString());
			response.setContentLength(size);

			metaDatabase.updateTime(idInCache);
		} catch (IOException e) {

			logger.log(Level.SEVERE, "Object cannot be taken from ru.miit.cache. " + e.toString());
			throw new CacheGetException(e.getMessage());
		}

	}

	public boolean exists(final String id) throws CacheMetadataStoreConnectionException {

		if (!connectionIsUp()) {
			logger.log(Level.SEVERE, "requested connection is closed.");
			throw new CacheMetadataStoreConnectionException("Connection is closed. ");
		}

		if (metaDatabase.exists(id)) {

			String location = metaDatabase.getValue(id, CacheParamName.location).toString();
			File file = new File(location + id);

			if (diskCache.exists(file)) {

				return true;

			} else {

				metaDatabase.delete(id);

				return false;
			}

		} else {

			return false;
		}

	}

	private Long getSize(final File catalog) {

		long length = 0;
		for (File file : catalog.listFiles()) {
			if (file.isFile()) {
				length += file.length();
			} else {
				length += getSize(file);
			}

		}
		return length;
	}

	private void trimToSize(final Map<String, Object> parameters) {

		// нахождение размера файла
		int fileSize = Integer.parseInt(parameters.get(CacheParamName.size).toString());

		// определение объема нода
		Map<String, CacheNode> nodesCollection = cacheProperties.getCacheNodes();

		CacheNode node = cacheProperties.getNodeByName(parameters.get(CacheParamName.type).toString());
		Long capacity = node.getCapacity();

		// местонахождение нода
		String folder = parameters.get(CacheParamName.folder).toString();
		String cacheNodeLocation = diskCache.directory + folder;

		String lastUpdatedId;
		while (capacity < getSize(new File(cacheNodeLocation)) + ((long) fileSize)) {

			Object lastUpdated = metaDatabase.getLastUpdated(node.getNodeName());
			if (lastUpdated == null) {
				break;
			} else {
				lastUpdatedId = lastUpdated.toString();
				deleteItemAsync(lastUpdatedId);
			}

		}
	}

	public String getHashById(final String id) throws CacheMetadataStoreConnectionException {

		if (!connectionIsUp()) {
			logger.log(Level.SEVERE, "requested connection is closed.");
			throw new CacheMetadataStoreConnectionException("Connection is closed. ");
		}

		Object hash = metaDatabase.getValue(id, CacheParamName.hash);
		if (hash == null) {
			return null;
		} else {
			return metaDatabase.getValue(id, CacheParamName.hash).toString();
		}
	}

	private void checkForFolder(final String location) {

		File file = new File(location);
		if (!file.exists()) {
			file.mkdirs();
		}

	}

	public Boolean deleteItem(final String id) throws CacheMetadataStoreConnectionException {
																							

		if (!connectionIsUp()) {
			logger.log(Level.SEVERE, "requested connection is closed.");
			throw new CacheMetadataStoreConnectionException("Connection is closed. ");
		}
		
		boolean result = false;

		try {
			result = diskCache.delete(new File(metaDatabase.getValue(id, CacheParamName.location) + id));
			metaDatabase.delete(id);
		} catch (IOException | InterruptedException e) {
			//Logger
			Thread.currentThread().interrupt();
		}

		return result;
	}
	
	public CompletableFuture<Boolean> deleteItemAsync(final String id) {
		
		CompletableFuture<Boolean> future = CompletableFuture.supplyAsync(() -> deleteItem(id), executor);
		
		return future;
		
	}

	public void applyDowntine(final long downtime) throws CacheMetadataStoreConnectionException {

		if (!connectionIsUp()) {
			logger.log(Level.SEVERE, "requested connection is closed.");
			throw new CacheMetadataStoreConnectionException("Connection is closed. ");
		}

		if (downtime > 0) {
			metaDatabase.applyDowntime(downtime);
		}

	}
	
	public List<Object> getOverdueList() {
		
		return metaDatabase.getOverdueList();
		
	}
	
//	public void allowAccess(String idInCache) {
//		
//		metaDatabase.allowAccess(idInCache);
//		
//	}

	private Map<String, String> getNodeNameMap() {

		return cacheProperties.getNodeNameMap();
	}

	private String getFileLocationByNodeName(String nodeName) {

		String fileLocation = null;

		Map<String, String> nodeNameMap = getNodeNameMap();

		if (nodeNameMap.get(nodeName) == null) {
			fileLocation = nodeNameMap.get(CacheParamName.general);
		} else {
			fileLocation = nodeNameMap.get(nodeName);
		}

		return fileLocation;
	}

	public FileOutputStream getFileOutputStream(String nodeName, String idInCache) throws FileNotFoundException {

		String location = getFileLocationByNodeName(nodeName);
		
		checkForFolder(location);
		
		String fullLocation = getFileLocationByNodeName(nodeName) + idInCache;

		FileOutputStream fos = new FileOutputStream(new File(fullLocation));

		return fos;
	}

	public CacheStatist getStatistics() throws CacheMetadataStoreConnectionException {

		if (!connectionIsUp()) {
			logger.log(Level.SEVERE, "requested connection is closed.");
			throw new CacheMetadataStoreConnectionException("Connection is closed. ");
		}

		Map<String, Object> statisticsMap = metaDatabase.getStatistics();

		long cacheHits = Long.parseLong(statisticsMap.get(CacheParamName.cacheHits).toString());
		long cacheMisses = Long.parseLong(statisticsMap.get(CacheParamName.cacheMisses).toString());

		return new CacheStatist(cacheHits, cacheMisses);

	}

	public void increaseHits() throws CacheMetadataStoreConnectionException {

		if (!connectionIsUp()) {
			logger.log(Level.SEVERE, "requested connection is closed.");
			throw new CacheMetadataStoreConnectionException("Connection is closed. ");
		}

		metaDatabase.increaseHits();

	}

	public void increaseMisses() throws CacheMetadataStoreConnectionException {

		if (!connectionIsUp()) {
			logger.log(Level.SEVERE, "requested connection is closed.");
			throw new CacheMetadataStoreConnectionException("Connection is closed. ");
		}

		metaDatabase.increaseMisses();

	}

	public void clear() throws CacheMetadataStoreConnectionException {

		if (!connectionIsUp()) {
			logger.log(Level.SEVERE, "requested connection is closed.");
			throw new CacheMetadataStoreConnectionException("Connection is closed. ");
		}

		List<Object> fullList = metaDatabase.getfullList();

		for (Object entry : fullList) {

			deleteItemAsync(entry.toString());

		}

	}

	public void clearStatistics() throws CacheMetadataStoreConnectionException {

		if (!connectionIsUp()) {
			logger.log(Level.SEVERE, "requested connection is closed.");
			throw new CacheMetadataStoreConnectionException("Connection is closed. ");
		}

		metaDatabase.clearStatistics();

	}
	
	public boolean connectionIsUp() {
		
		if (metaDatabase != null) {
			
			return metaDatabase.connectionIsUp();
		} else {
			return false;
		}
		
	}

	public void close() {

		executor.shutdown();
		
		if (metaDatabase != null)
			metaDatabase.close();

	}

}