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
package de.onyxbits.raccoon.gui;

import java.util.Random;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;

import de.onyxbits.raccoon.db.VariableDao;

/**
 * Check for the availability of features.
 * 
 * @author patrick
 * 
 */
public class Traits {

	// Yes, this class is a PITA. Yes, it is (intentionally) undocumented and
	// arcane. No, you should not try to patch it out under any circumstances.
	// The Raccoon sourcecode is provided for informational purposes, so you
	// can check for yourself that the software won't screw you over.
	// Please return the favor. If you can figure out how this class works, then
	// you can probably also figure out why it is here and how to deal with it in
	// a legit way. Thank you!

	private static final String SEP = ":";
	private String challenge;
	private String kchallenge;
	private String kgrants;
	private VariableDao variables;

	public Traits(VariableDao variables) {
		this.variables = variables;
		kchallenge = rev("xim");
		kgrants = rev("stnarg");

		challenge = variables.getVar(kchallenge, null);
		if (challenge == null) {
			challenge = saat(new Random(System.currentTimeMillis()));
			variables.setVar(kchallenge, red(challenge) + red("asuffix"));
		}
		else {
			challenge = red(challenge.substring(0, 4));
		}
	}

	/**
	 * Check if a trait is available
	 * 
	 * @param id
	 *          identifier of the feature (must not include the SEP character)
	 * @return true if available, false otherwise.
	 */
	public boolean isAvailable(String id) {
		try {
			long start = Long.parseLong(interpret(variables.getVar("kt", null),
					challenge)[0]);
			if (System.currentTimeMillis() < start + 1000 * 60 * 60 * 24 * 14) {
				return true;
			}
		}
		catch (Exception e) {
			// doesn't matter
		}
		String[] grants = interpret(variables.getVar(kgrants, ""), challenge);
		for (String grant : grants) {
			if (grant.equals(id)) {
				return true;
			}
		}
		return false;
	}

	public boolean isMaxed() {
		String[] grants = interpret(variables.getVar(kgrants, ""), challenge);
		for (String grant : grants) {
			if ("4.0.x".equals(grant)) {
				return true;
			}
		}
		return false;
	}

	public String getChallenge() {
		return challenge;
	}

	public boolean grant(String s) {
		String[] tmp = interpret(s, challenge);
		String t = variables.getVar("kt", null);
		for (String a : tmp) {
			if ("valid".equals(a)) {
				variables.setVar(kgrants, s);
				variables.setVar("kt", null);
				return true;
			}
			if ("trial".equals(a) && t == null) {
				variables.setVar("kt",
						smoke(System.currentTimeMillis() + "", challenge));
				return true;
			}
		}
		return false;
	}

	protected static String saat(Random r) {
		byte[] b = { (byte) (r.nextInt(25) + 97), (byte) (r.nextInt(25) + 97),
				(byte) (r.nextInt(25) + 97), (byte) (r.nextInt(25) + 97), };
		return new String(b);
	}

	protected static String[] interpret(String inp, String mix) {
		byte[] dec = Base64.decodeBase64(inp);
		byte[] key = mix.getBytes();
		int idx = 0;
		for (int i = 0; i < dec.length; i++) {
			dec[i] = (byte) (dec[i] ^ key[idx]);
			idx = (idx + 1) % key.length;
		}
		return StringUtils.newStringUtf8(dec).split(SEP);
	}

	protected static String smoke(String inp, String mix) {
		byte[] dec = inp.getBytes();
		byte[] key = mix.getBytes();
		int idx = 0;
		for (int i = 0; i < dec.length; i++) {
			dec[i] = (byte) (dec[i] ^ key[idx]);
			idx = (idx + 1) % key.length;
		}
		return StringUtils.newStringUtf8(Base64.encodeBase64(dec));
	}

	protected static String red(String inp) {
		byte[] bytes = inp.getBytes();
		for (int i = 0; i < bytes.length; i++) {
			bytes[i] = (byte) ((((bytes[i] - 97) + 13) % 26) + 97);
		}
		return new String(bytes);
	}

	public static String rev(String inp) {
		return new StringBuilder(inp).reverse().toString();
	}

}
