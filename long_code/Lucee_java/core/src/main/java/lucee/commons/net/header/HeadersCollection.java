package lucee.commons.net.header;

import org.apache.http.Header;

public interface HeadersCollection {
	public Header[] getHeaders(String name);
}