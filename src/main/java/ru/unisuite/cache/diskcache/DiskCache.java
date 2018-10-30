package ru.unisuite.cache.diskcache;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface DiskCache {
	
	public String getDirectory();

	public long getMaxSize();

	public int getAppVersion();

	public InputStream get(final String fullFileName) throws IOException;
	
	public void put(String key, InputStream is) throws IOException;
	
	public void writeToStream(FileInputStream fis, OutputStream os) throws IOException;

	public boolean delete(final File fileToDelete) throws IOException;
	
	public void close() throws IOException;
	
	public OutputStream openStream(String key) throws IOException;

	boolean exists(String key);
	
}
