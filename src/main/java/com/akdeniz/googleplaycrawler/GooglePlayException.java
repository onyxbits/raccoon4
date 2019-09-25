package com.akdeniz.googleplaycrawler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.http.HttpResponse;

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
		int httpStatus = httpResponse.getStatusLine().getStatusCode();
		String httpStatusString = httpResponse.getStatusLine().getReasonPhrase();
		byte[] response = null;
		String message = null;
		try {
			response = Utils.readAll(httpResponse.getEntity().getContent());
		} catch (Exception e) {}
		
		try {
			String contentType = httpResponse.getEntity().getContentType().getValue();
			if (contentType.equals("text/html")) {
				message = stripExtraSpaces(htmlToText(response));
			} else if (contentType.startsWith("text/")) {
				message = stripExtraSpaces(new String(response));
			} else if (contentType.equals("application/protobuf")) {
				message = "";
				for (String str : findProtobufStrings(response)) {
					message += "\n" + str;
				}
				message = stripExtraSpaces(message);
			}
		} catch (Exception e) {}
		
		if (message == null || message.isEmpty()) {
			message = httpStatus + " " + httpStatusString;
		}

		return new GooglePlayException(httpStatus, message);
	}

	
	/**
	 * Very simple and crappy method to remove HTML tags from
	 * an error response and keep only the text.
	 */
	private static String htmlToText(byte[] response) {
		return new String(response).replaceAll("(<[^>]*>)", "\n");
	}
	
	/**
	 * Best-effort attempt to retrieve error strings from an unknown Protobuf blob.
	 */
	private static List<String> findProtobufStrings(byte[] buffer) {
		try {
			List<String> ret = new ArrayList<String>();
			CodedInputStream in = CodedInputStream.newInstance(buffer);
			while (true) {
				int tag = in.readTag();
				if (tag == 0) {
					break;
				}
				if (WireFormat.getTagWireType(tag) == WireFormat.WIRETYPE_LENGTH_DELIMITED) {
					// The field is either a string of, maybe, a nested object
					byte[] blob = in.readByteArray();
					
					// Checks if the blob has only ascii-printable characters
					boolean isString = true;
					for (byte c : blob) {
						if (c < 0x20 || c >= 0x79) {
							isString = false;
							break;
						}
					}
					if (isString) {
						// We found it! An Ascii string
						ret.add(new String(blob));
					} else {
						// Not a string? Maybe a nested protobuf object
						ret.addAll(findProtobufStrings(blob));
					}
				} else {
					// The field is not a string and not a nested object -- We can ignore it
					in.skipField(tag);
				}
			}
			return ret;
		} catch (Exception e) {
			return Collections.emptyList();
		}
		
	}
	/**
	 * Removes extra spaces from response text
	 */
	private static String stripExtraSpaces(String text) {
		StringBuffer stripped = new StringBuffer();
		for (String line : text.split("\n")) {
			line = line.replaceAll("\\s+", " ").trim();
			if (line.isEmpty()) {
				continue;
			}
			if (stripped.length() > 0) {
				stripped.append(" - ");
			}
			stripped.append(line);
		}
		return stripped.toString();
	}
}
