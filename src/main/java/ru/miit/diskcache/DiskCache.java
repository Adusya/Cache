package ru.miit.diskcache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.io.IOUtils;

public class DiskCache {

	final public String directory;

	public DiskCache(String directory) {
		this.directory = directory;
	}

//	public void put(final String location, final String hashName, final InputStream is) {
//		
//
//		
//		File file = new File(location + hashName);
//
//		try (OutputStream os = new FileOutputStream(file)) {
//
//			IOUtils.copy(is, os);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//
//	}

	public void get(final String fullFileName, OutputStream os) throws IOException {		
		
		try (FileInputStream fis = new FileInputStream(fullFileName);
	            FileChannel filechannel = fis.getChannel();
	            FileLock lock = filechannel.lock(0, Long.MAX_VALUE, true)) {

	        IOUtils.copy(fis, os);

	    }

	}

	public boolean delete(final File fileToDelete) {

		return fileToDelete.delete();
	}

	public boolean exists(final File file) {

		return file.exists();

	}
}
