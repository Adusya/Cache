package ru.unisuite.cache;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class CacheLogger {
	
	private static final String DataSourceLogLevel = "java:comp/env/cacheLogger/level";
	private static final String DataSourceFileLocation = "java:comp/env/cacheLogger/fileLocation";

	public static Logger getLogger(String className) {

//		Logger logger = Logger.getLogger(className);
//		
//		if (logger.getHandlers().length == 0) {
//			
//			FileHandler handler = null;
//			try {
//				handler = new FileHandler(getLogFileLocation(), 1024 * 1024, 10, true);
//				Formatter formatter = new SimpleFormatter();
//				handler.setFormatter(formatter);
//				handler.setLevel(Level.parse(getLevel()));
//			} catch (SecurityException | IOException e) {
//				throw new RuntimeException(e);
//			}
//			
//			logger.addHandler(handler);
//		}
		
		return Logger.getLogger(className);

	}
	
	private static String getLevel() {
		
		Context initialContext = null;
		String level = null;
		try {
			initialContext = new InitialContext();
			level = initialContext.lookup(DataSourceLogLevel).toString();
		} catch (NamingException e) {
			throw new RuntimeException(e);
		} finally {
			if (initialContext != null){
					try {
						initialContext.close();
					} catch (NamingException e) {
						throw new RuntimeException(e);
					}
			}
		}
		
		return level;
		
	}
	
	private static String getLogFileLocation() {

		Context initialContext = null;
		String level = null;
		try {
			initialContext = new InitialContext();
			level = initialContext.lookup(DataSourceFileLocation).toString();
		} catch (NamingException e) {
			throw new RuntimeException(e);
		} finally {
			if (initialContext != null) {
				try {
					initialContext.close();
				} catch (NamingException e) {
					throw new RuntimeException(e);
				}
			}
		}

		return level;

	}
	
}
