package lucee.commons.io.log.log4j2;

import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.SimpleMessage;

import lucee.commons.io.SystemUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.CFMLFactoryImpl;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigWeb;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.net.http.ReqRspUtil;

public class ContextualMessage extends SimpleMessage {
	private static final long serialVersionUID = 3106308000640632352L;

	private final String context;
	private final String application;
	private final String message;
	private final Throwable throwable;

	public ContextualMessage(String context, String application, String message, Throwable throwable) {
		super(message != null ? message : "");
		this.context = context != null ? context : "";
		this.application = application != null ? application : "";
		this.message = message != null ? message : "";
		this.throwable = throwable;

	}

	public static Message create(ConfigWeb config, String application, String message, Throwable throwable) {
		return new ContextualMessage(getWebContextLabel(config), application, message, throwable);
	}

	public String getContext() {
		return context;
	}

	public String getApplication() {
		return application;
	}

	@Override
	public Throwable getThrowable() {
		return throwable;
	}

	@Override
	public String toString() {
		return getFormattedMessage(); // Uses parent's implementation
	}

	public static String getWebContextLabel(ConfigWeb config) {
		if (config == null) {
			Config tmp = ThreadLocalPageContext.getConfig();
			if (tmp instanceof ConfigWeb) config = (ConfigWeb) tmp;
			else return null;
		}
		// get URL
		CFMLFactoryImpl factory = (CFMLFactoryImpl) config.getFactory();
		if (factory.getURL() != null) return factory.getURL().toExternalForm();
		// if no URL, get webroot
		String path = ReqRspUtil.getRootPath(factory.getConfig().getServletContext(), null);
		if (path != null) return path;
		// if no webroot, get the label
		if (!StringUtil.isEmpty(factory.getLabel(), true)) return factory.getLabel().toString();
		// if no label, get the hash
		return SystemUtil.hash(factory.getConfig().getServletContext());

	}
}