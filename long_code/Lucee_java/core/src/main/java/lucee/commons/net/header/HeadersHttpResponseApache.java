package lucee.commons.net.header;

import org.apache.http.Header;
import org.apache.http.HttpResponse;

public final class HeadersHttpResponseApache implements HeadersCollection {

	private HttpResponse response;

	public HeadersHttpResponseApache(HttpResponse response) {
		this.response = response;
	}

	@Override
	public Header[] getHeaders(String name) {
		return response.getHeaders(name);
	}
}