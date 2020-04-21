package de.onyxbits.raccoon.net;

import java.io.IOException;
import java.util.Hashtable;

import org.bouncycastle.tls.CipherSuite;
import org.bouncycastle.tls.DefaultTlsClient;
import org.bouncycastle.tls.ECPointFormat;
import org.bouncycastle.tls.NamedCurve;
import org.bouncycastle.tls.ProtocolVersion;
import org.bouncycastle.tls.TlsAuthentication;
import org.bouncycastle.tls.TlsECCUtils;
import org.bouncycastle.tls.TlsSession;
import org.bouncycastle.tls.TlsUtils;
import org.bouncycastle.tls.crypto.TlsCrypto;
import org.bouncycastle.util.Arrays;

/**
 * Spoofs the SSL Handshake of a Jelly Bean device.
 * 
 * @author patrick
 * 
 */
class JellyBeanTlsClient extends DefaultTlsClient {

	private static final int[] SUITES = new int[] {
			CipherSuite.TLS_RSA_WITH_RC4_128_MD5,
			CipherSuite.TLS_RSA_WITH_RC4_128_SHA,
			CipherSuite.TLS_RSA_WITH_AES_128_CBC_SHA,
			CipherSuite.TLS_RSA_WITH_AES_256_CBC_SHA,
			CipherSuite.TLS_ECDH_ECDSA_WITH_RC4_128_SHA,
			CipherSuite.TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA,
			CipherSuite.TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA,
			CipherSuite.TLS_ECDH_RSA_WITH_RC4_128_SHA,
			CipherSuite.TLS_ECDH_RSA_WITH_AES_128_CBC_SHA,
			CipherSuite.TLS_ECDH_RSA_WITH_AES_256_CBC_SHA,
			CipherSuite.TLS_ECDHE_ECDSA_WITH_RC4_128_SHA,
			CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA,
			CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA,
			CipherSuite.TLS_ECDHE_RSA_WITH_RC4_128_SHA,
			CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA,
			CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA,
			CipherSuite.TLS_DHE_RSA_WITH_AES_128_CBC_SHA,
			CipherSuite.TLS_DHE_RSA_WITH_AES_256_CBC_SHA,
			CipherSuite.TLS_DHE_DSS_WITH_AES_128_CBC_SHA,
			CipherSuite.TLS_DHE_DSS_WITH_AES_256_CBC_SHA,
			CipherSuite.TLS_RSA_WITH_3DES_EDE_CBC_SHA,
			CipherSuite.TLS_ECDH_ECDSA_WITH_3DES_EDE_CBC_SHA,
			CipherSuite.TLS_ECDH_RSA_WITH_3DES_EDE_CBC_SHA,
			CipherSuite.TLS_ECDHE_ECDSA_WITH_3DES_EDE_CBC_SHA,
			CipherSuite.TLS_ECDHE_RSA_WITH_3DES_EDE_CBC_SHA,
			CipherSuite.TLS_DHE_RSA_WITH_3DES_EDE_CBC_SHA,
			CipherSuite.TLS_DHE_DSS_WITH_3DES_EDE_CBC_SHA,
			CipherSuite.TLS_RSA_WITH_DES_CBC_SHA,
			CipherSuite.TLS_DHE_RSA_WITH_DES_CBC_SHA,
			CipherSuite.TLS_DHE_DSS_WITH_DES_CBC_SHA,
			CipherSuite.TLS_RSA_EXPORT_WITH_RC4_40_MD5,
			CipherSuite.TLS_RSA_EXPORT_WITH_DES40_CBC_SHA,
			CipherSuite.TLS_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA,
			CipherSuite.TLS_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA,
			CipherSuite.TLS_EMPTY_RENEGOTIATION_INFO_SCSV };

	public JellyBeanTlsClient(TlsCrypto crypto) {
		super(crypto);
	}

	@Override
	public TlsAuthentication getAuthentication() throws IOException {
		return new DefaultTlsAuthentication(selectedCipherSuite);
	}

	public ProtocolVersion getClientVersion() {
		return ProtocolVersion.TLSv10;
	}

	public Hashtable<?, ?> getClientExtensions() throws IOException {
		OrderedHashtable ret = new OrderedHashtable();

		ProtocolVersion clientVersion = context.getClientVersion();

		/*
		 * RFC 5246 7.4.1.4.1. Note: this extension is not meaningful for TLS
		 * versions prior to 1.2. Clients MUST NOT offer it if they are offering
		 * prior versions.
		 */
		if (TlsUtils.isSignatureAlgorithmsExtensionAllowed(clientVersion)) {
			this.supportedSignatureAlgorithms = getSupportedSignatureAlgorithms();

			TlsUtils.addSignatureAlgorithmsExtension(ret,
					supportedSignatureAlgorithms);
		}

		if (TlsECCUtils.containsECCipherSuites(getCipherSuites())) {
			/*
			 * RFC 4492 5.1. A client that proposes ECC cipher suites in its
			 * ClientHello message appends these extensions (along with any others),
			 * enumerating the curves it supports and the point formats it can parse.
			 * Clients SHOULD send both the Supported Elliptic Curves Extension and
			 * the Supported Point Formats Extension.
			 */
			this.namedCurves = new int[] { NamedCurve.sect571r1,
					NamedCurve.sect571k1, NamedCurve.secp521r1, NamedCurve.sect409k1,
					NamedCurve.sect409r1, NamedCurve.secp384r1, NamedCurve.sect283k1,
					NamedCurve.sect283r1, NamedCurve.secp256k1, NamedCurve.secp256r1,
					NamedCurve.sect239k1, NamedCurve.sect233k1, NamedCurve.sect233r1,
					NamedCurve.secp224k1, NamedCurve.secp224r1, NamedCurve.sect193r1,
					NamedCurve.sect193r2, NamedCurve.secp192k1, NamedCurve.secp192r1,
					NamedCurve.sect163k1, NamedCurve.sect163r1, NamedCurve.sect163r2,
					NamedCurve.secp160k1, NamedCurve.secp160r1, NamedCurve.secp160r2 };

			this.clientECPointFormats = new short[] { ECPointFormat.uncompressed,
					ECPointFormat.ansiX962_compressed_prime,
					ECPointFormat.ansiX962_compressed_char2, };

			TlsECCUtils.addSupportedPointFormatsExtension(ret, clientECPointFormats);
			TlsECCUtils.addSupportedEllipticCurvesExtension(ret, namedCurves);
		}

		return ret;
	}

	public int[] getCipherSuites() {
		return Arrays.clone(SUITES);
	}

	public void notifySessionID(byte[] sessionID) {
		super.notifySessionID(sessionID);
	}

	public TlsSession getSessionToResume() {
		return null;
	}

}
