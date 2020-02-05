/*
 * Copyright 2015 Patrick Ahlbrecht
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.onyxbits.raccoon.setup;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.net.UnknownHostException;

import javax.swing.JPanel;
import javax.swing.JProgressBar;

import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import com.akdeniz.googleplaycrawler.GooglePlayAPI;

import de.onyxbits.raccoon.gplay.PlayManager;
import de.onyxbits.raccoon.gplay.PlayProfile;
import de.onyxbits.raccoon.gui.HyperTextPane;
import de.onyxbits.raccoon.net.DroidConnectionSocketFactory;

/**
 * Intermediate panel to show a progress bar while trying to log into GPlay.
 * 
 * @author patrick
 * 
 */
public class LoginLogic extends WizardBuilder {

	private JProgressBar progress;
	private HyperTextPane status;
	private Exception err;

	@Override
	protected JPanel assemble() {
		progress = new JProgressBar();
		status = new HyperTextPane("                             ")
				.withTransparency().withWidth(400);

		JPanel ret = new JPanel();
		ret.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.NONE;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.insets.top = 20;
		ret.add(progress, gbc);
		gbc.weighty = 1;
		gbc.gridy++;
		gbc.fill = GridBagConstraints.BOTH;
		ret.add(status, gbc);
		return ret;
	}

	@Override
	public void onActivate() {
		super.onActivate();
		status.setText("");
		progress.setIndeterminate(true);
		doInBackground();
	}

	public void onDoInBackground() {
		err = null;
		try {
			PlayProfile pp = globals.get(PlayProfile.class);
			GooglePlayAPI api = PlayManager.createConnection(pp);
			api.setClient(createLoginClient());
			api.login();
			pp.setToken(api.getToken());
		}
		catch (Exception e) {
			err = e;
		}
	}

	protected static HttpClient createLoginClient() {
		RegistryBuilder<ConnectionSocketFactory> rb = RegistryBuilder.create();
		rb.register("https", new DroidConnectionSocketFactory());
		// rb.register("http", new DroidConnectionSocketFactory());
		PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager(
				rb.build());
		connManager.setMaxTotal(100);
		connManager.setDefaultMaxPerRoute(30);
		// TODO: Increase the max connection limits. If we are doing bulkdownloads,
		// we will download from multiple hosts.
		int timeout = 9;
		RequestConfig config = RequestConfig.custom()
				.setConnectTimeout(timeout * 1000)
				.setConnectionRequestTimeout(timeout * 1000)
				.setSocketTimeout(timeout * 1000).build();
		HttpClientBuilder hcb = HttpClientBuilder.create().setDefaultRequestConfig(
				config);

		return hcb.setConnectionManager(connManager).build();
	}

	public void onDone() {
		progress.setIndeterminate(false);
		if (err == null) {
			onNext();
		}
		else {
			previous.setEnabled(true);
			if ("Error=BadAuthentication".equals(err.getMessage())) {
				// TODO rewrite the Google play API to throw a proper exception.
				status.setText(Messages.getString("LoginLogic.badcredentials"));
				return;
			}
			if (err.getMessage().startsWith("Error=NeedsBrowser")) {
				status.setText(Messages.getString("LoginLogic.oauthonly"));
				return;
			}
			if (err instanceof UnknownHostException) {
				status.setText(Messages.getString("LoginLogic.unknownhost"));
				return;
			}
			status.setText(err.getLocalizedMessage());
			throw (new RuntimeException(err));
		}
	}

	@Override
	protected void onNext() {
		show(DeviceLogic.class);
	}

	@Override
	protected void onPrevious() {
		PlayProfile pp = globals.get(PlayProfile.class);
		if (pp.getProxyAddress() != null) {
			show(ProxyLogic.class);
		}
		else {
			show(AccountLogic.class);
		}
	}
}
