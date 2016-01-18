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
package de.onyxbits.raccoon.gplay;

/**
 * Contains all the required information to get service from Google Play.
 * 
 * @author patrick
 * 
 */
public class PlayProfile {

	/**
	 * The handle/nickname of the passport (the user name is not used for this
	 * because it is a) probably too long to fit in some UI elements b) slightly
	 * sensitive (some people don't want it to show in screenshots/tutorials c)
	 * impractical if the user owns multiple vastly different hardware devices.
	 */
	private String alias;

	/**
	 * User name
	 */
	private String user;

	/**
	 * Password - Only needed to obtain the token, so don't persist it in order to
	 * avoid arguments with "security experts" who "know" that storing plaintext
	 * passwords is a problem in case they want to run shady applications on their
	 * machine.
	 */
	private String password;

	/**
	 * Auth cookie
	 */
	private String token;

	/**
	 * Useragent string
	 */
	private String agent;

	/**
	 * Proxy address (may be null)
	 */
	private String proxyAddress;

	/**
	 * Proxy port
	 */
	private int proxyPort;

	/**
	 * User account for the proxy server (may be null)
	 */
	private String proxyUser;

	/**
	 * Password for the proxy server (may be null).
	 */
	private String proxyPassword;

	/**
	 * Google Service Framework identifier
	 */
	private String gsfId;

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String auth) {
		this.token = auth;
	}

	public String getAgent() {
		return agent;
	}

	public void setAgent(String agent) {
		this.agent = agent;
	}

	public String getGsfId() {
		return gsfId;
	}

	public void setGsfId(String gsfId) {
		this.gsfId = gsfId;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public String getProxyAddress() {
		return proxyAddress;
	}

	public void setProxyAddress(String proxyAddress) {
		this.proxyAddress = proxyAddress;
	}

	public String getProxyUser() {
		return proxyUser;
	}

	public void setProxyUser(String proxyUser) {
		this.proxyUser = proxyUser;
	}

	public String getProxyPassword() {
		return proxyPassword;
	}

	public void setProxyPassword(String proxyPassword) {
		this.proxyPassword = proxyPassword;
	}

	public int getProxyPort() {
		return proxyPort;
	}

	public void setProxyPort(int proxyPort) {
		this.proxyPort = proxyPort;
	}

	public String toString() {
		return alias;
	}
}
