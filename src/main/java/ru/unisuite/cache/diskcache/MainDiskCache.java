package ru.unisuite.cache.diskcache;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;

public class MainDiskCache implements DiskCache {

	public MainDiskCache(String dir, int appVersion, long maxSize) throws IOException {

		this.directory = dir;
		this.appVersion = appVersion;
		this.maxSize = maxSize;
		this.diskCache = SimpleDiskCache.open(new File(directory), appVersion, maxSize);
	}

	private String directory;
	private long maxSize;
	private int appVersion;

	private SimpleDiskCache diskCache;

	public String getDirectory() {
		return directory;
	}

	public long getMaxSize() {
		return maxSize;
	}

	public int getAppVersion() {
		return appVersion;
	}

	public SimpleDiskCache getDiskCache() {
		return diskCache;
	}
	
	@Override
	public InputStream get(String fullFileName) throws IOException {

		return diskCache.getInputStream(fullFileName).getInputStream();

	}

	@Override
	public void writeToStream(FileInputStream fis, OutputStream os) throws IOException {
		IOUtils.copy(fis, os);

	}

	@Override
	public boolean delete(File fileToDelete) throws IOException {
		return diskCache.remove(fileToDelete.getName());
	}

	@Override
	public boolean exists(String key) {
		try {
			return diskCache.contains(key);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public void put(String key, InputStream is) throws IOException {

		diskCache.put(key, is);
		
	}

	@Override
	public void close() throws IOException {
		if (diskCache != null)
			diskCache.getCache().close();
	}

	@Override
	public OutputStream openStream(String key) throws IOException {
		return diskCache.openStream(key);
	}

}
