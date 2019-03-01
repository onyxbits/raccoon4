package de.onyxbits.raccoon.gplay;

import com.akdeniz.googleplaycrawler.GooglePlay.BulkDetailsResponse;
import com.akdeniz.googleplaycrawler.GooglePlay.DeliveryResponse;
import com.akdeniz.googleplaycrawler.GooglePlay.DetailsResponse;
import com.akdeniz.googleplaycrawler.GooglePlay.ListResponse;
import com.akdeniz.googleplaycrawler.GooglePlay.Payload;
import com.akdeniz.googleplaycrawler.GooglePlay.ResponseWrapper;
import com.akdeniz.googleplaycrawler.GooglePlay.SearchResponse;
import com.akdeniz.googleplaycrawler.GooglePlay.TocResponse;
import com.akdeniz.googleplaycrawler.GooglePlay.UploadDeviceConfigResponse;


/**
 * Extract a response from a {@link ResponseWrapper}. Return an empty instance
 * if the requested message is not available.
 * 
 * @author patrick
 * 
 */
public class Unwrap {

	public static Payload payload(ResponseWrapper rw) {
		if (rw != null && rw.hasPayload()) {
			return rw.getPayload();
		}
		return Payload.getDefaultInstance();
	}

	public static SearchResponse searchResponse(ResponseWrapper rw) {
		Payload pl = payload(rw);
		if (payload(rw).hasSearchResponse()) {
			return pl.getSearchResponse();
		}
		return SearchResponse.getDefaultInstance();
	}

	public static ListResponse listResponse(ResponseWrapper rw) {
		Payload pl = payload(rw);
		if (pl.hasListResponse()) {
			return pl.getListResponse();
		}
		return ListResponse.getDefaultInstance();
	}

	public static DeliveryResponse deliveryResponse(ResponseWrapper rw) {
		Payload pl = payload(rw);
		if (pl.hasDeliveryResponse()) {
			return pl.getDeliveryResponse();
		}
		return DeliveryResponse.getDefaultInstance();
	}

	public static BulkDetailsResponse bulkDetailsResponse(ResponseWrapper rw) {
		Payload pl = payload(rw);
		if (pl.hasBulkDetailsResponse()) {
			return pl.getBulkDetailsResponse();
		}
		return BulkDetailsResponse.getDefaultInstance();
	}

	public static DetailsResponse detailsResponse(ResponseWrapper rw) {
		Payload pl = payload(rw);
		if (pl.hasDetailsResponse()) {
			return pl.getDetailsResponse();
		}
		return DetailsResponse.getDefaultInstance();
	}

	public static TocResponse tocResponse(ResponseWrapper rw) {
		Payload pl = payload(rw);
		if (pl.hasTocResponse()) {
			return pl.getTocResponse();
		}
		return TocResponse.getDefaultInstance();
	}

	public static UploadDeviceConfigResponse uploadDeviceConfigResponse(
			ResponseWrapper rw) {
		Payload pl = payload(rw);
		if (pl.hasUploadDeviceConfigResponse()) {
			return pl.getUploadDeviceConfigResponse();
		}
		return UploadDeviceConfigResponse.getDefaultInstance();
	}

}
