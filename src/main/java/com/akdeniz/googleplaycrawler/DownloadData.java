package com.akdeniz.googleplaycrawler;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.zip.GZIPInputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.akdeniz.googleplaycrawler.GooglePlay.AndroidAppDeliveryData;
import com.akdeniz.googleplaycrawler.GooglePlay.HttpCookie;
import com.akdeniz.googleplaycrawler.misc.Base64;

public class DownloadData {

	private AndroidAppDeliveryData appDeliveryData;
	private String downloadUrl;
	private HttpCookie downloadAuthCookie;
	private GooglePlayAPI api;
	private long totalUncompressedSize;
	private long totalCompressedSize;
	private boolean compress;

	public DownloadData(GooglePlayAPI api,
			AndroidAppDeliveryData appDeliveryData) {
		this.appDeliveryData = appDeliveryData;
		this.api = api;
		this.downloadUrl = appDeliveryData.getDownloadUrl();
		for (HttpCookie cookie : appDeliveryData.getDownloadAuthCookieList()) {
			this.downloadAuthCookie = cookie;
		}
		/*
		 * this.totalSize = appDeliveryData.getDownloadSize(); for (int i = 0; i <
		 * appDeliveryData.getAdditionalFileCount(); i++) { totalSize +=
		 * appDeliveryData.getAdditionalFile(i).getSize(); }
		 */
		setCompress(false);
	}

	public void setCompress(boolean c) {
		compress = c;
		this.totalUncompressedSize = appDeliveryData.getDownloadSize();
		this.totalCompressedSize = appDeliveryData.getGzippedDownloadSize();
		for (int i = 0; i < appDeliveryData.getAdditionalFileCount(); i++) {
			if (!appDeliveryData.getAdditionalFile(i).hasCompressedDownloadUrl()) {
				compress = false;
			}
			this.totalUncompressedSize += appDeliveryData.getAdditionalFile(i)
					.getSize();
			this.totalCompressedSize += appDeliveryData.getAdditionalFile(i)
					.getCompressedSize();
		}
		for (int i = 0; i < appDeliveryData.getSplitDeliveryDataCount(); i++) {
			if (!appDeliveryData.getSplitDeliveryData(i).hasGzippedDownloadUrl()) {
				break;
			}
			this.totalUncompressedSize += appDeliveryData.getSplitDeliveryData(i)
					.getDownloadSize();
			this.totalCompressedSize += appDeliveryData.getSplitDeliveryData(i)
					.getGzippedDownloadSize();
		}
		if (!appDeliveryData.hasGzippedDownloadUrl()) {
			compress = false;
		}
	}

	/**
	 * Access the APK file
	 * 
	 * @return an inputstream from which the app can be read (already processed
	 *         through crypto).
	 * @throws NoSuchPaddingException
	 * @throws NoSuchProviderException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidAlgorithmParameterException
	 * @throws InvalidKeyException
	 */
	public InputStream openApp() throws IOException, NoSuchAlgorithmException,
			NoSuchProviderException, NoSuchPaddingException, InvalidKeyException,
			InvalidAlgorithmParameterException {
		InputStream ret = null;
		String tmp = null;
		if (downloadAuthCookie != null) {
			tmp = downloadAuthCookie.getName() + "=" + downloadAuthCookie.getValue();
		}
		if (compress) {
			ret = new GZIPInputStream(
					api.executeDownload(appDeliveryData.getGzippedDownloadUrl(), tmp));
		}
		else {
			ret = api.executeDownload(downloadUrl, tmp);
		}
		if (appDeliveryData.hasEncryptionParams()) {
			int version = ret.read();
			if (version != 0) {
				throw new IOException("Unknown crypto container!");
			}
			ret.skip(4); // Meta data
			byte[] iv = new byte[16];
			ret.read(iv);
			byte[] encoded = appDeliveryData.getEncryptionParams().getEncryptionKey()
					.getBytes("UTF-8");
			byte[] decoded = Base64.decode(encoded, Base64.DEFAULT);
			Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding", "SunJCE");
			SecretKeySpec key = new SecretKeySpec(decoded, "AES");
			cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
			return new CipherInputStream(ret, cipher);
		}
		else {
			return ret;
		}
	}

	public long getAppSize() {
		return appDeliveryData.getDownloadSize();
	}

	/**
	 * Query the total downloadsize
	 * 
	 * @return number of bytes to transfer.
	 */
	public long getTotalSize() {
		if (compress) {
			return totalUncompressedSize;
		}
		else {
			return totalUncompressedSize;
		}
	}

	/**
	 * Access the first expansion
	 * 
	 * @return a stream or null if there is no expansion.
	 */
	public InputStream openMainExpansion() throws IOException {
		if (appDeliveryData.getAdditionalFileCount() < 1) {
			return null;
		}
		if (compress) {
			String url = appDeliveryData.getAdditionalFile(0)
					.getCompressedDownloadUrl();
			return new GZIPInputStream(api.executeDownload(url,
					downloadAuthCookie.getName() + "=" + downloadAuthCookie.getValue()));
		}
		else {
			String url = appDeliveryData.getAdditionalFile(0).getDownloadUrl();
			return api.executeDownload(url,
					downloadAuthCookie.getName() + "=" + downloadAuthCookie.getValue());
		}
	}

	public boolean hasMainExpansion() {
		return appDeliveryData.getAdditionalFileCount() > 0;
	}

	public int getMainFileVersion() {
		if (appDeliveryData.getAdditionalFileCount() > 0) {
			return appDeliveryData.getAdditionalFile(0).getVersionCode();
		}
		return -1;
	}

	public long getMainSize() {
		return appDeliveryData.getAdditionalFile(0).getSize();
	}

	/**
	 * Access the second expansion
	 * 
	 * @return a stream or null if there is no expansion.
	 */
	public InputStream openPatchExpansion() throws IOException {
		if (appDeliveryData.getAdditionalFileCount() < 2) {
			return null;
		}
		if (compress) {
			String url = appDeliveryData.getAdditionalFile(1)
					.getCompressedDownloadUrl();
			return new GZIPInputStream(api.executeDownload(url,
					downloadAuthCookie.getName() + "=" + downloadAuthCookie.getValue()));
		}
		else {
			String url = appDeliveryData.getAdditionalFile(1).getDownloadUrl();
			return api.executeDownload(url,
					downloadAuthCookie.getName() + "=" + downloadAuthCookie.getValue());
		}
	}

	public boolean hasPatchExpansion() {
		return appDeliveryData.getAdditionalFileCount() > 1;
	}

	public long getPatchSize() {
		return appDeliveryData.getAdditionalFile(1).getSize();
	}

	public int getPatchFileVersion() {
		if (appDeliveryData.getAdditionalFileCount() > 1) {
			return appDeliveryData.getAdditionalFile(1).getVersionCode();
		}
		return -1;
	}

	public int getSplitCount() {
		return appDeliveryData.getSplitDeliveryDataCount();
	}

	public InputStream openSplitDelivery(int n) throws IOException {
		if (getSplitCount() < 1) {
			return null;
		}
		if (compress) {
			String url = appDeliveryData.getSplitDeliveryData(n)
					.getGzippedDownloadUrl();
			return new GZIPInputStream(api.executeDownload(url,
					downloadAuthCookie.getName() + "=" + downloadAuthCookie.getValue()));
		}
		else {
			String url = appDeliveryData.getSplitDeliveryData(n).getDownloadUrl();
			return api.executeDownload(url,
					downloadAuthCookie.getName() + "=" + downloadAuthCookie.getValue());
		}
	}

	public String toString() {
		return appDeliveryData.toString();
	}

	public String getSplitId(int n) {
		if (getSplitCount() > 0) {
			return appDeliveryData.getSplitDeliveryData(n).getId();
		}
		return null;
	}

	public long getSplitSize(int n) {
		if (getSplitCount() > 0) {
			return appDeliveryData.getSplitDeliveryData(n).getDownloadSize();
		}
		return -1;
	}

}
