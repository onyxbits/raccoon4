package de.onyxbits.raccoon.gplay;

import java.util.ArrayList;
import java.util.List;

import com.akdeniz.googleplaycrawler.GooglePlay.DocV2;
import com.akdeniz.googleplaycrawler.GooglePlay.PreFetch;
import com.akdeniz.googleplaycrawler.GooglePlay.ResponseWrapper;
import com.google.protobuf.InvalidProtocolBufferException;

/**
 * A (relatively) smart adapter for transforming the various search response
 * formats into a flat, continuous list.
 * 
 * @author patrick
 * 
 */
public class SearchEngineResultPage {

	private ArrayList<DocV2> items;
	private String nextPageUrl;
	private String title;
	private int type;

	/**
	 * Type: everything
	 */
	public static final int ALL = 0;

	/**
	 * Type: only append what was searched for
	 */
	public static final int SEARCH = 1;

	/**
	 * Type: only append similar items. This requires an exact match
	 */
	public static final int SIMILIAR = 2;

	/**
	 * Type: only append items of the "other users also..." type. This requires an
	 * exact match.
	 */
	public static final int RELATED = 3;

	/**
	 * 
	 * @param type
	 *          Either ALL, SEARCH, SIMILAR or RELATED. Only Applies when trying
	 *          to add {@link DocumentType#MULTILIST}.
	 */
	public SearchEngineResultPage(int type) {
		this.items = new ArrayList<DocV2>();
		this.nextPageUrl = null;
		this.type = type;
	}

	/**
	 * Try to make sense of a {@link ResponseWrapper}, containing a search result.
	 * 
	 * @param rw
	 *          a wrapper containing either a {@link SearchResponse},
	 *          {@link ListResponse} or a {@link PreFetch}
	 */
	public void append(ResponseWrapper rw) {
		// The SearchResponse format changed considerably over time. The message
		// type seems to have gotten deprecated for Android 5 and later in favor of
		// ListResponse. Apparently, SearchResponse got too too unwieldy.
		append(Unwrap.searchResponse(rw).getDocList());
		append(Unwrap.listResponse(rw).getDocList());
		for (PreFetch pf : rw.getPreFetchList()) {
			try {
				append(ResponseWrapper.parseFrom(pf.getResponse()));
			}
			catch (InvalidProtocolBufferException e) {
				// We tried, we failed.
			}
		}
	}

	private void append(List<DocV2> list) {
		for (DocV2 doc : list) {
			append(doc);
		}
	}

	/**
	 * Grow the SERP
	 * 
	 * @param doc
	 *          a document of type {@link DocumentType#PRODUCTLIST} or a document
	 *          containing a {@link DocumentType#PRODUCTLIST}.
	 */
	public void append(DocV2 doc) {
		switch (doc.getDocType()) {
			case 46: {
				for (DocV2 child : doc.getChildList()) {
					if (accept(child)) {
						append(child);
					}
				}
				break;
			}
			case 45: {
				for (DocV2 d:doc.getChildList()) {
					if (d.getDocType()==1) {
						items.add(d);
					}
				}
				nextPageUrl = null;
				if (doc.hasContainerMetadata()) {
					nextPageUrl = doc.getContainerMetadata().getNextPageUrl();
				}
				if (title == null && doc.hasTitle()) {
					title = doc.getTitle();
				}
				break;
			}
			default: {
				for (DocV2 child : doc.getChildList()) {
					append(child);
				}
				break;
			}
		}
	}

	private boolean accept(DocV2 doc) {
		String dbid = doc.getBackendDocid();
		switch (type) {
			case ALL: {
				return true;
			}
			case SEARCH: {
				return (dbid != null && dbid.matches(".*search.*"));
			}
			case SIMILIAR: {
				return (dbid != null && dbid.matches("similar_apps"));
			}
			case RELATED: {
				return (dbid != null && dbid
						.matches("pre_install_users_also_installed"));
			}
			default: {
				return false;
			}
		}
	}

	/**
	 * Get the entry list.
	 * 
	 * @return a flat list.
	 */
	public List<DocV2> getContent() {
		return items;
	}

	/**
	 * Get the title of this page (if any).
	 * 
	 * @return null or the title of the first appended doc.
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Check if results are available.
	 * 
	 * @return null if there are no more search results to load.
	 */
	public String getNextPageUrl() {
		return nextPageUrl;
	}

	public String toString() {
		StringBuilder ret = new StringBuilder();
		if (title != null) {
			ret.append('[');
			ret.append(title);
			ret.append("]\n");
		}
		for (DocV2 item : items) {
			ret.append(item.getDocid());
			ret.append(", ");
			ret.append("\"");
			ret.append(item.getTitle());
			ret.append("\"\n");
		}
		if (nextPageUrl != null) {
			ret.append("-> ");
			ret.append(nextPageUrl);
			ret.append('\n');
		}
		return ret.toString();
	}
}
