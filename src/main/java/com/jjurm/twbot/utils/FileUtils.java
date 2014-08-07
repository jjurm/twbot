package com.jjurm.twbot.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.zip.GZIPInputStream;

public class FileUtils {
	private FileUtils() {} // Prevent instantiation

	/**
	 * Unzip .gz file
	 * 
	 * @param input_file
	 * @param output_file
	 * @throws IOException
	 */
	public static void gunzip(File input_file, File output_file) throws IOException {

		try (GZIPInputStream gzis = new GZIPInputStream(new FileInputStream(input_file));
				FileOutputStream out = new FileOutputStream(output_file);
				ReadableByteChannel rbc = Channels.newChannel(gzis);) {

			out.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);

		} catch (IOException e) {
			throw e;
		}

	}

	/**
	 * Download file from Internet
	 * 
	 * @param remote_file
	 * @param target
	 * @throws IOException
	 */
	public static void downloadFile(String remote_file, File target) throws IOException {
		URL url;
		try {
			url = new URL(remote_file);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return;
		}
		try (ReadableByteChannel rbc = Channels.newChannel(url.openStream());
				FileOutputStream out = new FileOutputStream(target)) {

			out.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);

		} catch (IOException e) {
			throw e;
		}
	}

	/**
	 * This will delete the file and create new file.
	 * 
	 * @param file
	 * @throws IOException
	 */
	public static void clearFile(File file) throws IOException {
		if (file.exists())
			file.delete();
		file.createNewFile();
	}
	
	/**
	 * Delegated method from {@link org.apache.commons.io.FileUtils}
	 * 
	 * @param file source file to copy
	 * @param target destination file
	 * @throws IOException
	 */
	public static void copyFile(File file, File target) throws IOException {
		org.apache.commons.io.FileUtils.copyFile(file, target);
	}

}
