package com.akdeniz.googleplaycrawler;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.http.HttpResponse;

import com.akdeniz.googleplaycrawler.GooglePlay.ResponseWrapper;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.WireFormat;

public class GooglePlayException extends IOException {
	private static final long serialVersionUID = 1L;
	
	private final int httpStatus;


	public GooglePlayException(int httpStatus, String message) {
		super(message);
		this.httpStatus = httpStatus;
	}
	
	public int getHttpStatus() {
		return httpStatus;
	}
	
	public static GooglePlayException create(HttpResponse httpResponse) {
		String message = httpResponse.getStatusLine().getReasonPhrase();	
		
		// If the reponse contains a Protobuf response, retrieves the message from a ResponseWrapper object
		try (InputStream content = httpResponse.getEntity().getContent()) {
			if ("application/protobuf".equals(httpResponse.getEntity().getContentType().getValue())) {
				ResponseWrapper rw = ResponseWrapper.parseFrom(content);
				if (rw.hasCommands() && rw.getCommands().hasDisplayErrorMessage()) {
					message = rw.getCommands().getDisplayErrorMessage();
				}
			}
		} catch (Exception e) {}

		return new GooglePlayException(httpResponse.getStatusLine().getStatusCode(), message);
	}
}
