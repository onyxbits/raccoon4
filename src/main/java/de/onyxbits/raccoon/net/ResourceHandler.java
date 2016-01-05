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
package de.onyxbits.raccoon.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

/**
 * Serves resources from the classpath.
 * 
 * @author patrick
 * 
 */
class ResourceHandler extends AbstractHandler {

	@Override
	public void handle(String target, Request baseRequest,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {

		response.setContentType("application/octet-stream");
		response.setStatus(HttpServletResponse.SC_OK);
		if (target.toLowerCase().endsWith("png")) {
			response.setContentType("image/png");
		}
		if (target.toLowerCase().endsWith("css")) {
			response.setContentType("text/css");
		}

		// Prevent path traversal -> all resources must be in /web, no sub
		// directories.
		String tmp = target.substring(target.lastIndexOf('/'), target.length());

		InputStream in = getClass().getResourceAsStream("/web" + tmp);
		OutputStream out = response.getOutputStream();

		IOUtils.copy(in, out);
		in.close();
		out.flush();
		out.close();

		baseRequest.setHandled(true);
	}

}
