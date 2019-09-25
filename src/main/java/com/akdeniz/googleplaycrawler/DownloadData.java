package com.akdeniz.googleplaycrawler;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.util.zip.GZIPInputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.akdeniz.googleplaycrawler.GooglePlay.AndroidAppDeliveryData;
import com.akdeniz.googleplaycrawler.GooglePlay.AppFileMetadata;
import com.akdeniz.googleplaycrawler.GooglePlay.EncryptionParams;
import com.akdeniz.googleplaycrawler.GooglePlay.HttpCookie;
import com.akdeniz.googleplaycrawler.GooglePlay.SplitDeliveryData;
import com.akdeniz.googleplaycrawler.misc.Base64;

public class DownloadData {	
	private GooglePlayAPI api;
	private AndroidAppDeliveryData appDeliveryData;
	private long totalSize;
	private boolean compress;
	
	private MainApkFile mainApk;
	private AdditionalFile[] additionalFiles;
	private SplitApkFile[] splitApkFiles;
	
	public DownloadData(GooglePlayAPI api, AndroidAppDeliveryData appDeliveryData) {
		this.api = api;
		this.appDeliveryData = appDeliveryData;
		
		this.mainApk = new MainApkFile();
		this.totalSize = mainApk.getSize();
		this.compress = false;
		
		this.additionalFiles = new AdditionalFile[appDeliveryData.getAdditionalFileCount()];
		for (int i=0; i< additionalFiles.length; i++) {
			this.additionalFiles[i] = new AdditionalFile(i);
			this.totalSize += this.additionalFiles[i].getSize();
		}
		this.splitApkFiles = new SplitApkFile[appDeliveryData.getSplitDeliveryDataCount()];
		for (int i=0; i< splitApkFiles.length; i++) {
			this.splitApkFiles[i] = new SplitApkFile(i);
			this.totalSize += this.splitApkFiles[i].getSize();
		}
	}

	/**
	 * Toggles whenever the download is performed in a compressed stream,
	 * saving some bandwidth.
	 * 
	 * This is only a hint -- SOme of the files to be downloaded might not support it,
	 * and will fall back to non-compressed download.
	 */
	public void setCompress(boolean compress) {
		this.compress = compress;
	}
	
	public MainApkFile getMainApk() {
		return mainApk;
	}
	
	public AdditionalFile[] getAdditionalFiles() {
		return additionalFiles;
	}
	
	public SplitApkFile[] getSplitApkFiles() {
		return splitApkFiles;
	}

	/**
	 * Query the total download size
	 * 
	 * @return number of bytes to transfer.
	 */
	public long getTotalSize() {
		return totalSize;
	}
	
	/**
	 * A Base class for various application files that compose the application
	 * being downloaded.
	 *
	 * @param <M> The type of the Google API object corresponding to this AppFile.
	 */
	public abstract class AppFile<M> {
		/**
		 * Returns the Play API object corresponding to this AppFile.
		 */
		public abstract M getMetadata();
		
		/** Returns the uncompressed size of this file */
		public abstract long getSize();
		
		/**
		 * Open a stream to download this file.
		 * 
		 * There is no need to worry about compression and encryption -- if any, it will be handled internally.
		 */
		public abstract InputStream openStream() throws IOException, GeneralSecurityException;
		
		/**
		 * Helper method to perform a possible-compressed and possibly-encrypted download.
		 * @throws IOException 
		 */
		protected InputStream openStream(String downloadUrl, String gzippedDownloadUrl, EncryptionParams encryptionParams) throws IOException, GeneralSecurityException {
			HttpCookie cookie = appDeliveryData.getDownloadAuthCookie(0);
			String cookieStr = cookie.getName() + "=" + cookie.getValue();
			
			InputStream ret = null;
			if (compress && gzippedDownloadUrl != null) {
				ret = new GZIPInputStream(api.executeDownload(gzippedDownloadUrl, cookieStr));
			} else if (downloadUrl != null) {
				ret = api.executeDownload(downloadUrl, cookieStr);
			} else {
				throw new NullPointerException("downloadUrl");
			}
			
			if (encryptionParams == null) {
				return ret;
			}
			
			
			try {
				DataInputStream dataIn = new DataInputStream(ret);
				
				int version = dataIn.readByte();
				if (version != 0) {
					throw new IOException("Unknown crypto container!");
				}
				dataIn.readInt(); // Skips 4 bytes of metadata
				byte[] iv = new byte[16];
				dataIn.readFully(iv);
				Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding", "SunJCE");
				SecretKeySpec key = new SecretKeySpec(Base64.decode(encryptionParams.getEncryptionKey(), Base64.DEFAULT) , "AES");
				cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
				return new CipherInputStream(dataIn, cipher);
			} catch (Throwable t) {
				ret.close();
				throw t;
			}
		}
	}
	
	public class MainApkFile extends AppFile<AndroidAppDeliveryData> {		
		AndroidAppDeliveryData metadata;
		
		public MainApkFile() {
			metadata = appDeliveryData;
		}
		
		@Override
		public AndroidAppDeliveryData getMetadata() {
			return metadata;
		}
		
		@Override
		public long getSize() {
			return appDeliveryData.getDownloadSize();
		}
		
		@Override
		public InputStream openStream() throws IOException, GeneralSecurityException {
			return openStream(
					metadata.hasDownloadUrl() ? metadata.getDownloadUrl() : null,
					metadata.hasGzippedDownloadUrl() ? metadata.getGzippedDownloadUrl() : null,
					metadata.hasEncryptionParams() ? metadata.getEncryptionParams() : null);
		}
	}
		
	public class AdditionalFile extends AppFile<AppFileMetadata> {
		public static final int MAIN = 0;
		public static final int PATCH = 1;
		
		private final int index;
		private final AppFileMetadata metadata;
		
		public AdditionalFile(int index) {
			this.index = index;
			this.metadata = appDeliveryData.getAdditionalFile(index);
		}

		public int getIndex() {
			return index;
		}
		
		@Override
		public AppFileMetadata getMetadata() {
			return metadata;
		}
		
		@Override
		public long getSize() {
			return metadata.getSize();
		}
		
		public int getVersionCode() {
			return metadata.getVersionCode();
		}
		
		@Override
		public InputStream openStream() throws IOException, GeneralSecurityException {
			return openStream(
					metadata.hasDownloadUrl() ? metadata.getDownloadUrl() : null,
					metadata.hasCompressedDownloadUrl() ? metadata.getCompressedDownloadUrl() : null,
					null);
		}
	}
	
	public class SplitApkFile extends AppFile<SplitDeliveryData> {
		private final int index;
		private final SplitDeliveryData metadata;

		public SplitApkFile(int index) {
			this.index = index;
			this.metadata = appDeliveryData.getSplitDeliveryData(index);
		}
		
		@Override
		public SplitDeliveryData getMetadata() {
			return metadata;
		}
		
		public String getId() {
			return metadata.getId();
		}
		
		
		@Override
		public long getSize() {
			return metadata.getDownloadSize();
		}
		
		@Override
		public InputStream openStream() throws IOException, GeneralSecurityException {
			return openStream(
					metadata.hasDownloadUrl() ? metadata.getDownloadUrl() : null,
					metadata.hasGzippedDownloadUrl() ? metadata.getGzippedDownloadUrl() : null,
					null);
		}
	}

	public String toString() {
		return appDeliveryData.toString();
	}
}
