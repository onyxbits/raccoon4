package de.onyxbits.raccoon.net;

import java.io.IOException;

import javax.security.cert.CertificateException;
import javax.security.cert.X509Certificate;

import org.bouncycastle.tls.AlertDescription;
import org.bouncycastle.tls.Certificate;
import org.bouncycastle.tls.CertificateRequest;
import org.bouncycastle.tls.TlsAuthentication;
import org.bouncycastle.tls.TlsCredentials;
import org.bouncycastle.tls.TlsFatalAlert;
import org.bouncycastle.tls.crypto.TlsCertificate;

public class DefaultTlsAuthentication implements TlsAuthentication {

	@Override
	public void notifyServerCertificate(Certificate serverCertificate)
			throws IOException {

		if (serverCertificate == null || serverCertificate.isEmpty()) {
			throw new TlsFatalAlert(AlertDescription.handshake_failure);
		}

		TlsCertificate[] certificates = serverCertificate.getCertificateList();
		for (TlsCertificate c : certificates) {
			try {
				X509Certificate cert = X509Certificate.getInstance(c.getEncoded());
				cert.checkValidity();
			}
			catch (CertificateException e) {
				throw new IOException(e.getMessage());
			}
		}
		// TODO yes, something VERY important is missing here. If that bothers you,
		// either provide a patch or the funding for developing one.
	}

	@Override
	public TlsCredentials getClientCredentials(
			CertificateRequest certificateRequest) throws IOException {
		return null;
	}

}
