package ru.unisuite.cache.diskcache;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.AsynchronousByteChannel;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

public class UsualDiskCache implements DiskCache{

	final public String directory;

	public UsualDiskCache(String directory) {
		this.directory = directory;
	}

	// public void put(final String location, final String hashName, final
	// InputStream is) {
	//
	//
	//
	// File file = new File(location + hashName);
	//
	// try (OutputStream os = new FileOutputStream(file)) {
	//
	// IOUtils.copy(is, os);
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	//
	// }

	public synchronized void get(final String fullFileName, OutputStream os) throws IOException {

		try (FileInputStream fis = new FileInputStream(fullFileName);
				FileChannel filechannel = fis.getChannel();
				FileLock lock = filechannel.lock(0, Long.MAX_VALUE, true)) {

			writeToStream(fis, os);

		}

	}

	public void writeToStream(FileInputStream fis, OutputStream os) throws IOException {

		int length;
		int bufSize = 4096;
		byte buffer[] = new byte[bufSize];
		while ((length = fis.read(buffer, 0, bufSize)) != -1) {
			os.write(buffer, 0, length);
		}
		os.flush();

	}

	public boolean delete(final File fileToDelete) {

		return fileToDelete.delete();
	}

	public boolean exists(final File file) {

		return file.exists();

	}

	@Override
	public String getDirectory() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getMaxSize() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getAppVersion() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void put(String key, InputStream is) throws IOException {
		// TODO Auto-generated method stub
	}

	@Override
	public InputStream get(String fullFileName) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public OutputStream openStream(String key) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean exists(String key) {
		// TODO Auto-generated method stub
		return false;
	}
}
