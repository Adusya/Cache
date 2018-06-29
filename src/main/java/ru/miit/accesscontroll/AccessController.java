package ru.miit.accesscontroll;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class AccessController {

	public Map<String, Entry> entrySet = new HashMap<String, Entry>();

	public boolean put(String entryName, Blob blobData, OutputStream os1, FileOutputStream os2) {
		synchronized (entrySet) {
			Entry entry = entrySet.get(entryName);

			if (entry == null) {
				entry = new Entry();
				entrySet.put(entryName, entry);
			}

			if (!entry.isNewbie()) {
				while (entrySet.get(entryName).getWriters() != 0) {
					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}

			try {
				entrySet.put(entryName, increaseWriters(entrySet.get(entryName)));
				writeBlobToOsAndFos(blobData, os1, os2);
				return true;
			} catch (IOException | SQLException e) {
				return false;
			} finally {
				entrySet.put(entryName, reduceWriters(entrySet.get(entryName)));
				if (entry.getReaders() != 0) {

					System.out.println("num of readers: " + entry.getReaders());

				}

				if (entry.getWriters() != 0) {

					System.out.println("num of writers: " + entry.getWriters());

				}
			}
		}

	}

	public boolean get(String entryName, String fileLocation, OutputStream os) {

		synchronized (entrySet) {

			Entry entry = entrySet.get(entryName);

			if (entry == null) {
				entry = new Entry();
				entrySet.put(entryName, entry);
			}

			if (!entry.isNewbie()) {
				while (entrySet.get(entryName).getWriters() != 0) {
					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}

			try (FileInputStream fis = new FileInputStream(new File(fileLocation))) {
				entrySet.put(entryName, increaseReaders(entrySet.get(entryName)));
				writeToFisToOs(fis, os);
				return true;
			} catch (IOException e) {
				return false;
			} finally {
				entrySet.put(entryName, reduceReaders(entrySet.get(entryName)));
				if (entry.getReaders() != 0) {

					System.out.println("num of readers: " + entry.getReaders());

				}

				if (entry.getWriters() != 0) {

					System.out.println("num of writers: " + entry.getWriters());

				}
			}
		}

		// if (entry.getReaders() ==0 && entry.getWriters() == 0) {
		// System.out.println("");
		// }

	}

	public void writeToFisToOs(FileInputStream fis, OutputStream os) throws IOException {

		int length;
		int bufSize = 4096;
		byte buffer[] = new byte[bufSize];
		while ((length = fis.read(buffer, 0, bufSize)) != -1) {
			os.write(buffer, 0, length);
		}
		os.flush();

	}

	public synchronized void writeBlobToOsAndFos(Blob blobData, OutputStream os1, FileOutputStream os2)
			throws IOException, SQLException {

		int length;
		int bufSize = 4096;
		byte buffer[] = new byte[bufSize];
		try (InputStream is = blobData.getBinaryStream();
				FileChannel filechannel = os2.getChannel();
				FileLock lock = filechannel.lock(0, Long.MAX_VALUE, false)) {
			while ((length = is.read(buffer, 0, bufSize)) != -1) {
				os1.write(buffer, 0, length);
				os2.write(buffer, 0, length);
			}
			os1.flush();
			os2.flush();
		}

	}

	public Entry increaseReaders(Entry entry) {

		entry.increaseReaders();

		return entry;

	}

	public Entry increaseWriters(Entry entry) {

		entry.increaseWriters();

		return entry;

	}

	public Entry reduceReaders(Entry entry) {

		entry.reduceReaders();

		return entry;

	}

	public Entry reduceWriters(Entry entry) {

		entry.reduceWriters();

		return entry;

	}

}
