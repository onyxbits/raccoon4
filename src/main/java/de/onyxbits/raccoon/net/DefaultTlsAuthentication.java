/*******************************************************************************
 * Copyright 2020 Patrick Ahlbrecht
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package de.onyxbits.raccoon.net;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.bouncycastle.tls.AlertDescription;
import org.bouncycastle.tls.Certificate;
import org.bouncycastle.tls.KeyExchangeAlgorithm;
import org.bouncycastle.tls.ServerOnlyTlsAuthentication;
import org.bouncycastle.tls.TlsFatalAlert;
import org.bouncycastle.tls.TlsUtils;
import org.bouncycastle.tls.crypto.TlsCertificate;

public class DefaultTlsAuthentication extends ServerOnlyTlsAuthentication {

	private TrustManager[] trustManagers;
	private CertificateFactory certificateFactory;
	private String authType;

	public DefaultTlsAuthentication(int selectedCipherSuite) {
		try {
			TrustManagerFactory trustManagerFactory = TrustManagerFactory
					.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			trustManagerFactory.init((KeyStore) null);
			trustManagers = trustManagerFactory.getTrustManagers();
			certificateFactory = CertificateFactory.getInstance("X.509");
			int keyExchangeAlgorithm = TlsUtils
					.getKeyExchangeAlgorithm(selectedCipherSuite);
			authType = getAuthTypeServer(keyExchangeAlgorithm);
		}
		catch (Exception e) {
		}
	}

	@Override
	public void notifyServerCertificate(Certificate serverCertificate)
			throws IOException {

		if (serverCertificate == null || serverCertificate.isEmpty()) {
			throw new TlsFatalAlert(AlertDescription.handshake_failure);
		}
		if (trustManagers == null || certificateFactory == null) {
			throw new TlsFatalAlert(AlertDescription.unknown_ca);
		}
		if (authType == null) {
			throw new TlsFatalAlert(AlertDescription.internal_error);
		}

		TlsCertificate[] certificates = serverCertificate.getCertificateList();
		X509Certificate[] chain = new X509Certificate[certificates.length];
		ByteArrayInputStream bis = null;
		for (int i = 0; i < chain.length; i++) {
			bis = new ByteArrayInputStream(certificates[i].getEncoded());
			try {
				chain[i] = (X509Certificate) certificateFactory
						.generateCertificate(bis);
				chain[i].checkValidity();
			}
			catch (CertificateExpiredException e) {
				throw new TlsFatalAlert(AlertDescription.certificate_expired);
			}
			catch (CertificateNotYetValidException e) {
				throw new TlsFatalAlert(AlertDescription.certificate_expired);
			}
			catch (CertificateException e) {
				throw new TlsFatalAlert(AlertDescription.decode_error, e);
			}
		}

		for (TrustManager trustManager : trustManagers) {
			if (trustManager instanceof X509TrustManager) {
				X509TrustManager x509TrustManager = (X509TrustManager) trustManager;
				try {
					x509TrustManager.checkServerTrusted(chain, authType);
				}
				catch (Exception e) {
					throw new IOException(e.getCause());
				}
			}
		}
	}

	private String getAuthTypeServer(int keyExchangeAlgorithm) {
		switch (keyExchangeAlgorithm) {
			case KeyExchangeAlgorithm.DH_anon:
				return "DH_anon";
			case KeyExchangeAlgorithm.DH_DSS:
				return "DH_DSS";
			case KeyExchangeAlgorithm.DH_RSA:
				return "DH_RSA";
			case KeyExchangeAlgorithm.DHE_DSS:
				return "DHE_DSS";
			case KeyExchangeAlgorithm.DHE_PSK:
				return "DHE_PSK";
			case KeyExchangeAlgorithm.DHE_RSA:
				return "DHE_RSA";
			case KeyExchangeAlgorithm.ECDH_anon:
				return "ECDH_anon";
			case KeyExchangeAlgorithm.ECDH_ECDSA:
				return "ECDH_ECDSA";
			case KeyExchangeAlgorithm.ECDH_RSA:
				return "ECDH_RSA";
			case KeyExchangeAlgorithm.ECDHE_ECDSA:
				return "ECDHE_ECDSA";
			case KeyExchangeAlgorithm.ECDHE_PSK:
				return "ECDHE_PSK";
			case KeyExchangeAlgorithm.ECDHE_RSA:
				return "ECDHE_RSA";
			case KeyExchangeAlgorithm.RSA:
				return "RSA";
			case KeyExchangeAlgorithm.RSA_PSK:
				return "RSA_PSK";
			case KeyExchangeAlgorithm.SRP:
				return "SRP";
			case KeyExchangeAlgorithm.SRP_DSS:
				return "SRP_DSS";
			case KeyExchangeAlgorithm.SRP_RSA:
				return "SRP_RSA";
			default:
				return null;
		}
	}
}
