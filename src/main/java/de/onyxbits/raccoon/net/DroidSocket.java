package de.onyxbits.raccoon.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.security.SecureRandom;

import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;

import org.bouncycastle.tls.TlsClient;
import org.bouncycastle.tls.TlsClientProtocol;
import org.bouncycastle.tls.crypto.TlsCrypto;
import org.bouncycastle.tls.crypto.impl.bc.BcTlsCrypto;

/**
 * An encrypted layer on top of a plain socket.
 * 
 * @author patrick
 * 
 */
class DroidSocket extends SSLSocket {

	// private HashMap<HandshakeCompletedListener, AccessControlContext>
	// handshakeListeners = new HashMap<>();

	private TlsClient client;
	private Socket base;
	private TlsClientProtocol protocol;

	public DroidSocket(Socket base) {
		this.base = base;
	}

	@Override
	public void startHandshake() throws IOException {
		if (protocol == null) {
			protocol = new TlsClientProtocol(base.getInputStream(),
					base.getOutputStream());
			TlsCrypto crypto = new BcTlsCrypto(new SecureRandom());
			client = new JellyBeanTlsClient(crypto);
			protocol.connect(client);
		}
	}

	@Override
	public void close() throws IOException {
		base.close();
	}

	@Override
	public boolean isClosed() {
		return base.isClosed();
	}

	@Override
	public InputStream getInputStream() throws IOException {
		if (protocol == null) {
			startHandshake();
		}
		return protocol.getInputStream();
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		if (protocol == null) {
			startHandshake();
		}
		return protocol.getOutputStream();
	}

	@Override
	public String[] getSupportedCipherSuites() {
		return new String[] { "SSL_RSA_WITH_RC4_128_MD5",
				"SSL_RSA_WITH_RC4_128_SHA", "TLS_RSA_WITH_AES_128_CBC_SHA",
				"TLS_RSA_WITH_AES_256_CBC_SHA",

				"TLS_ECDH_ECDSA_WITH_RC4_128_SHA",
				"TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA",
				"TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA", "TLS_ECDH_RSA_WITH_RC4_128_SHA",

				"TLS_ECDH_RSA_WITH_AES_128_CBC_SHA",
				"TLS_ECDH_RSA_WITH_AES_256_CBC_SHA",
				"TLS_ECDHE_ECDSA_WITH_RC4_128_SHA",
				"TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA",

				"TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA",
				"TLS_ECDHE_RSA_WITH_RC4_128_SHA", "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA",
				"TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA",

				"TLS_DHE_RSA_WITH_AES_128_CBC_SHA", "TLS_DHE_RSA_WITH_AES_256_CBC_SHA",
				"TLS_DHE_DSS_WITH_AES_128_CBC_SHA", "TLS_DHE_DSS_WITH_AES_256_CBC_SHA",

				"SSL_RSA_WITH_3DES_EDE_CBC_SHA",
				"TLS_ECDH_ECDSA_WITH_3DES_EDE_CBC_SHA",
				"TLS_ECDH_RSA_WITH_3DES_EDE_CBC_SHA",
				"TLS_ECDHE_ECDSA_WITH_3DES_EDE_CBC_SHA",

				"TLS_ECDHE_RSA_WITH_3DES_EDE_CBC_SHA",
				"SSL_DHE_RSA_WITH_3DES_EDE_CBC_SHA",
				"SSL_DHE_DSS_WITH_3DES_EDE_CBC_SHA", "SSL_RSA_WITH_DES_CBC_SHA",

				"SSL_DHE_RSA_WITH_DES_CBC_SHA", "SSL_DHE_DSS_WITH_DES_CBC_SHA",
				"SSL_RSA_EXPORT_WITH_RC4_40_MD5", "SSL_RSA_EXPORT_WITH_DES40_CBC_SHA",

				"SSL_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA",
				"SSL_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA",
				"TLS_EMPTY_RENEGOTIATION_INFO_SCSV" };

	}

	@Override
	public String[] getEnabledCipherSuites() {
		return getSupportedCipherSuites();
	}

	@Override
	public void setEnabledCipherSuites(String[] suites) {
		throw new UnsupportedOperationException(
				"This would change the SSL fingerprint");
	}

	@Override
	public String[] getSupportedProtocols() {
		return new String[] { "SSLv3" };
	}

	@Override
	public String[] getEnabledProtocols() {
		return getSupportedProtocols();
	}

	@Override
	public void setEnabledProtocols(String[] protocols) {
		throw new UnsupportedOperationException(
				"This would change the SSL fingerprint");
	}

	@Override
	public SSLSession getSession() {
		return null;
	}

	@Override
	public void addHandshakeCompletedListener(HandshakeCompletedListener listener) {
		throw new UnsupportedOperationException("Do we need this?");
	}

	@Override
	public void removeHandshakeCompletedListener(
			HandshakeCompletedListener listener) {
		throw new UnsupportedOperationException("Do we need this?");
	}

	@Override
	public void setUseClientMode(boolean mode) {
		if (!mode) {
			throw new UnsupportedOperationException("This socket is client mode only");
		}
	}

	@Override
	public boolean getUseClientMode() {
		return true;
	}

	@Override
	public void setNeedClientAuth(boolean need) {
		if (need) {
			throw new UnsupportedOperationException("Nope!");
		}
	}

	@Override
	public boolean getNeedClientAuth() {
		return false;
	}

	@Override
	public void setWantClientAuth(boolean want) {
		if (want) {
			throw new UnsupportedOperationException("Don't care");
		}
	}

	@Override
	public boolean getWantClientAuth() {
		return false;
	}

	@Override
	public void setEnableSessionCreation(boolean flag) {

	}

	@Override
	public boolean getEnableSessionCreation() {
		return false;
	}

}
