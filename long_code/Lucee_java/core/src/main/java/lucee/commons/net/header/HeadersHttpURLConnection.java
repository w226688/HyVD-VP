package lucee.commons.net.header;

import java.net.HttpURLConnection;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

import lucee.commons.lang.StringUtil;

public final class HeadersHttpURLConnection implements HeadersCollection {

	private HttpURLConnection conn;

	public HeadersHttpURLConnection(HttpURLConnection conn) {
		this.conn = conn;
	}

	@Override
	public Header[] getHeaders(String name) {
		String val = conn.getHeaderField(name);
		if (!StringUtil.isEmpty(val, true)) {
			return new Header[] { new BasicHeader(name, val) };
		}
		return null;
	}
}