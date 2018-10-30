package ru.unisuite.cache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

import ru.unisuite.cache.cacheexception.CacheGetException;
import ru.unisuite.cache.cacheexception.CacheMetadataStoreConnectionException;
import ru.unisuite.cache.diskcache.DiskCache;
import ru.unisuite.cache.diskcache.MainDiskCache;
import ru.unisuite.cache.metadatastore.MetadataStore;
import ru.unisuite.cache.metadatastore.MongoMetadataStore;

public class Cache {

	private final Logger logger = CacheLogger.getLogger(Cache.class.getName());

	private MetadataStore metaDatabase;
	private DiskCache diskCache;
	private CacheProperties cacheProperties;
	public Boolean isUp = false;
	
//	public AccessController controller = new AccessController();

	public ExecutorService executor = Executors.newFixedThreadPool(200);
	
	final static public String defaultNodeName = "general";

	public Cache(CacheProperties cacheProperties) throws IOException {
		System.out.println("cache created");
		this.cacheProperties = cacheProperties;
		
		metaDatabase = new MongoMetadataStore(cacheProperties.getMongoProperties());
		if (metaDatabase.connectionIsUp()) {
			isUp = true;
		} else {
			isUp = false;
		}

		diskCache = new MainDiskCache(cacheProperties.getCacheDirectory(), 1, 800000L);

	}

	public Boolean put(final String idInCache, String fullAdress, Map<String, Object> parameters)
			throws CacheMetadataStoreConnectionException, IOException {

		if (!connectionIsUp()) {
			logger.log(Level.SEVERE, "requested connection is closed.");
			throw new CacheMetadataStoreConnectionException("Connection is closed. ");
		}

		if (idInCache == null) {

			logger.log(Level.SEVERE, "Illegal object id value. Id is null.");
			throw new IllegalArgumentException("The object id can't be empty");

		}

//		try (InputStream is = new FileInputStream(new File(fullAdress))) {
//			
//			diskCache.put(idInCache, is);
			
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

			parameters.put(CacheParamName.id, idInCache);
			
			metaDatabase.put(idInCache, parameters);
			
			return true;
//			
//		}
	}

	public CompletableFuture<Boolean> putAsync(final String idInCache, String fullAdress, final Map<String, Object> parameters) {

		CompletableFuture<Boolean> future = CompletableFuture.supplyAsync(() -> {
			try {
				return put(idInCache, fullAdress, parameters);
			} catch (CacheMetadataStoreConnectionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return false;
		}, executor);

		return future;

	}

	public void get(final String idInCache, OutputStream out, HttpServletResponse response)
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

//		try {
			
//			controller.get(idInCache, fullLocation, out);
//			usualDiskCache.get(fullLocation, out);
		
		
		try (InputStream is = diskCache.get(idInCache)) {
			
			IOUtils.copy(is, out);
			String contentType = parameters.get(CacheParamName.contentType).toString();
			response.setContentType(contentType);

			int size = Integer.parseInt(parameters.get(CacheParamName.size).toString());
			response.setContentLength(size);

			metaDatabase.updateTime(idInCache);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

			
//		} catch (IOException e) {
//
//			logger.log(Level.SEVERE, "Object cannot be taken from ru.unisuite.cache. " + e.toString());
//			throw new CacheGetException(e.getMessage());
//		}

	}

	public boolean exists(final String id) throws CacheMetadataStoreConnectionException {

		if (!connectionIsUp()) {
			logger.log(Level.SEVERE, "requested connection is closed.");
			throw new CacheMetadataStoreConnectionException("Connection is closed. ");
		}

		if (metaDatabase.exists(id)) {

//			String location = metaDatabase.getValue(id, CacheParamName.location).toString();
//			File file = new File(location + id);

			if (diskCache.exists(id)) {

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

	private void trimToSize(final Map<String, Object> parameters) throws CacheMetadataStoreConnectionException, IOException {

		// нахождение размера файла
		long fileSize = Long.parseLong(parameters.get(CacheParamName.size).toString());

		// определение объема нода 
		Map<String, CacheNode> nodesCollection = cacheProperties.getCacheNodes();

		CacheNode node = cacheProperties.getNodeByName(parameters.get(CacheParamName.type).toString());
		
		Long capacity = node.getCapacity();
		
		// местонахождение нода
		String folder = parameters.get(CacheParamName.folder).toString();
		String cacheNodeLocation = diskCache.getDirectory() + folder;

		Object lastUpdatedId;
		while (capacity < getSize(new File(cacheNodeLocation)) + fileSize) {

			lastUpdatedId = metaDatabase.getLastUpdated(node.getNodeName());
			if (lastUpdatedId == null) {
				break;
			} else {
				deleteItem(lastUpdatedId.toString());
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
			return hash.toString();
		}
	}

	private void checkForFolder(final String location) {

		File file = new File(location);
		if (!file.exists()) {
			file.mkdirs();
		}

	}

	public Boolean deleteItem(final String id) throws CacheMetadataStoreConnectionException, IOException {
																							

		if (!connectionIsUp()) {
			logger.log(Level.SEVERE, "requested connection is closed.");
			throw new CacheMetadataStoreConnectionException("Connection is closed. ");
		}

		File fileToDelete = new File(metaDatabase.getValue(id, CacheParamName.location) + id);
				
		boolean result = diskCache.delete(fileToDelete);
		int i = 0;
		
		while(!result && i < 5 && fileToDelete.exists()) {
			try {
				Thread.sleep(1000 * i);
			} catch (InterruptedException e) {
				logger.log(Level.SEVERE, e.getMessage());
			}
			i++;
			result = diskCache.delete(fileToDelete);
		}
		
		if (result || !fileToDelete.exists()) {
			metaDatabase.delete(id);
		}
		
		return result;

	}
	
	public CompletableFuture<Boolean> deleteItemAsync(final String id) {
		
		CompletableFuture<Boolean> future = CompletableFuture.supplyAsync(() -> {
			try {
				return deleteItem(id);
			} catch (CacheMetadataStoreConnectionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}, executor);
		
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
	
	public void allowAccess(String idInCache) {
		
		metaDatabase.allowAccess(idInCache);
		
	}

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
		
		if (metaDatabase == null) {
			return false;			
		} else {
			return metaDatabase.connectionIsUp();
		}
		
	}

	public void close() {


		if (metaDatabase != null)
			metaDatabase.close();
		
		if (diskCache != null)
			try {
				diskCache.close();
			} catch (IOException e) {
				logger.log(Level.WARNING, "Disk cache was not close. " + e.getMessage(), e);
			}

	}
	
	public void shutdown() {
		
		executor.shutdown();
		try {
			executor.awaitTermination(3000, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			logger.log(Level.SEVERE, e.getMessage());
		}
		
	}
	
	public void writeToTwoStreams(String idInCache, Blob blobObject, OutputStream os1, OutputStream os2) throws IOException, SQLException {
		
		int length;
		int bufSize = 4096;
		byte buffer[] = new byte[bufSize];
		try (InputStream is = blobObject.getBinaryStream()) {
			while ((length = is.read(buffer, 0, bufSize)) != -1) {
				os1.write(buffer, 0, length);
				os2.write(buffer, 0, length);
			}
			os1.flush();
			os2.flush();
		}
		
	}
	
	public OutputStream openStream(String key) throws IOException {
		
		return diskCache.openStream(key);
		
	}

}