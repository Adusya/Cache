package ru.miit.cache;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import ru.miit.cache.cacheexception.CachePropertiesException;
import ru.miit.cache.metadatastore.MongoProperties;
import ru.miit.cache.timechecker.TimeCheckerProperties;

public class CacheProperties {

	private Logger loggerCacheProperties = CacheLogger.getLogger(CacheProperties.class.getName());

	public String defaultNodeName = "general";
	public long defaultNodeCapacity = 500 * 1000;
	public String defaultNodeDirectiry = File.separator + "general";
	public Long defaultTimeToLive = 0L;
	public Long defaultTimeToIdle = 0L;
	
	public boolean defaultTimeCheckerEnable = false;
	public long defaultTimeCheckerPeriod = 60L;

	public String cacheDirectory;
	public String dbCollectionName;
	public Map<String, CacheNode> nodesCollection;

	public MongoProperties mongoProperties;
	
	public TimeCheckerProperties timeCheckerProperties;

	// TODO сделать проверку на валидность конф файла
	public CacheProperties(final String configFilePath) throws CachePropertiesException {

		File xmlFile = new File(configFilePath);
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		Document document = null;
		try {
			db = dbf.newDocumentBuilder();
			document = db.parse(xmlFile);
		} catch (ParserConfigurationException | SAXException | IOException e) {

			loggerCacheProperties.log(Level.WARNING, "System cannot read configuration file. " + e.toString());
			throw new CachePropertiesException(e.getMessage());
		}

		document.getDocumentElement().normalize();

		String cacheDirectory = document.getDocumentElement().getElementsByTagName(CacheParamName.cacheDirectory)
				.item(0).getTextContent();
		setCacheDirectory(cacheDirectory);

		String dbCollection = document.getDocumentElement().getElementsByTagName(CacheParamName.dbCollection).item(0)
				.getTextContent();
		setDbCollectionName(dbCollection);

		NodeList nodesList = document.getDocumentElement().getElementsByTagName(CacheParamName.node);
		setCacheNodes(nodesList);

		Element mongoProperties = (Element) document.getDocumentElement().getElementsByTagName(CacheParamName.mongo)
				.item(0);
		setMongoProperties(mongoProperties);
		
		Element timeCheckerProperties = (Element) document.getDocumentElement().getElementsByTagName(CacheParamName.timeChecker)
				.item(0);
		setTimeCheckerProperties(timeCheckerProperties);
	}

	public String getDbCollectionName() {
		return dbCollectionName;
	}

	public void setDbCollectionName(final String dbCollectionName) {
		this.dbCollectionName = dbCollectionName;
	}

	public String getCacheDirectory() {
		return cacheDirectory;
	}

	public void setCacheDirectory(final String cachedirectory) {
		this.cacheDirectory = cachedirectory;
	}

	public Map<String, CacheNode> getCacheNodes() {
		return nodesCollection;
	}

	public Map<String, String> getNodeNameMap() {

		Map<String, String> nodeNameMap = new HashMap<>();

		Map<String, CacheNode> cacheNodeMap = getCacheNodes();

		for (Map.Entry<String, CacheNode> entry : cacheNodeMap.entrySet()) {

			CacheNode node = entry.getValue();
			String nodeDirectory = node.getDirectory();
			nodeNameMap.put(entry.getKey(), cacheDirectory + nodeDirectory + File.separator);

		}

		return nodeNameMap;

	}

	public void setCacheNodes(final NodeList nodesList) {

		Map<String, CacheNode> nodesCollection = new HashMap<>();

		CacheNode defaultCacheNode = new CacheNode(defaultNodeName, defaultNodeCapacity, defaultNodeDirectiry,
				defaultTimeToLive, defaultTimeToIdle);
		nodesCollection.put(defaultNodeName, defaultCacheNode);

		for (int i = 0; i < nodesList.getLength(); i++) {

			Element node = (Element) nodesList.item(i);

			String nameNode = node.getAttribute(CacheParamName.name);
			Long capacity = Long.parseLong(node.getElementsByTagName(CacheParamName.capacity).item(0).getTextContent());
			String directory = File.separator
					+ node.getElementsByTagName(CacheParamName.directory).item(0).getTextContent();
			Long timeToLive = Long
					.parseLong(node.getElementsByTagName(CacheParamName.timeToLive).item(0).getTextContent());
			Long timeToIdle = Long
					.parseLong(node.getElementsByTagName(CacheParamName.timeToIdle).item(0).getTextContent());

			CacheNode cacheNode = new CacheNode(nameNode, capacity, directory, timeToLive, timeToIdle);

			nodesCollection.put(node.getAttribute(CacheParamName.name), cacheNode);

		}

		this.nodesCollection = nodesCollection;
	}
	
	public MongoProperties getMongoProperties() {

		return mongoProperties;
	}

	public void setMongoProperties(final Element properties) throws CachePropertiesException {
		
		Map<String, Object> mongoPropertiesMap = new HashMap<>();
		
		try {
			Object dbName = properties.getElementsByTagName(CacheParamName.dbName).item(0).getTextContent();
			Object dbCollectionName = properties.getElementsByTagName(CacheParamName.dbCollectionName).item(0).getTextContent();
			Object statisticsCollectionName = properties.getElementsByTagName(CacheParamName.statisticsCollectionName).item(0).getTextContent();
			Object staticticsFieldName = properties.getElementsByTagName(CacheParamName.staticticsFieldName).item(0).getTextContent();
			Object ip = properties.getElementsByTagName(CacheParamName.ip).item(0).getTextContent();
			Object port = properties.getElementsByTagName(CacheParamName.port).item(0).getTextContent();
			Object userName = properties.getElementsByTagName(CacheParamName.userName).item(0).getTextContent();
			Object userPassword = properties.getElementsByTagName(CacheParamName.userPassword).item(0).getTextContent();
			Object errorsLimit = properties.getElementsByTagName(CacheParamName.errorsLimit).item(0).getTextContent();
			Object waitingConnectionTime = properties.getElementsByTagName(CacheParamName.waitingConnectionTime).item(0).getTextContent();
			Object periodCheckConnectionTime = properties.getElementsByTagName(CacheParamName.periodCheckConnectionTime).item(0).getTextContent();
			
			mongoPropertiesMap.put(CacheParamName.dbName, dbName);
			mongoPropertiesMap.put(CacheParamName.dbCollectionName, dbCollectionName);
			mongoPropertiesMap.put(CacheParamName.statisticsCollectionName, statisticsCollectionName);
			mongoPropertiesMap.put(CacheParamName.staticticsFieldName, staticticsFieldName);
			mongoPropertiesMap.put(CacheParamName.ip, ip);
			mongoPropertiesMap.put(CacheParamName.port, port);
			mongoPropertiesMap.put(CacheParamName.userName, userName);
			mongoPropertiesMap.put(CacheParamName.userPassword, userPassword);
			mongoPropertiesMap.put(CacheParamName.errorsLimit, errorsLimit);
			mongoPropertiesMap.put(CacheParamName.waitingConnectionTime, waitingConnectionTime);
			mongoPropertiesMap.put(CacheParamName.periodCheckConnectionTime, periodCheckConnectionTime);
				
		} catch(NullPointerException e) {
			
			throw new CachePropertiesException("You must fill all of the mongoDB properties: " + e.getMessage());
		}
		
		mongoProperties = new MongoProperties(mongoPropertiesMap);
		
		
	}
	
	public TimeCheckerProperties getTimeCheckerProperties() {
		return timeCheckerProperties;
	}

	public void setTimeCheckerProperties(final Element properties) {
		
		boolean enable;
		long checkPeriod;
		
		try {
			enable = Boolean.parseBoolean(properties.getElementsByTagName(CacheParamName.enable).item(0).getTextContent().toString());
		} catch (NullPointerException e) {
			
			enable = defaultTimeCheckerEnable;
			loggerCacheProperties.log(Level.WARNING, "Field 'enable' of timeChecker cannot be null. " + e.getMessage());
		}
		
		try {
			checkPeriod = Long.parseLong(properties.getElementsByTagName(CacheParamName.checkPeriod).item(0).getTextContent().toString());
		} catch (NullPointerException | NumberFormatException e) {
			
			checkPeriod = defaultTimeCheckerPeriod;
			loggerCacheProperties.log(Level.WARNING, "Field 'checkPeriod' of timeChecker cannot be initialized. " + e.getMessage());
		}
		
		timeCheckerProperties = new TimeCheckerProperties(enable, checkPeriod);
		
	}

	public CacheNode getNodeByName(String nodeName) {

		CacheNode node = null;

		if (nodeName == null || nodeName.length() == 0) {
			node = nodesCollection.get(defaultNodeName);
		} else {
			if (nodesCollection.get(nodeName) == null) {
				node = nodesCollection.get(defaultNodeName);
			} else {
				node = nodesCollection.get(nodeName);
			}

		}

		return node;
	}

}
