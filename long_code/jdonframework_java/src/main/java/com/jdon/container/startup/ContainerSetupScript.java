/**
 * Copyright 2003-2006 the original author or authors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain event copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jdon.container.startup;

import com.jdon.container.builder.ContainerDirector;
import com.jdon.container.builder.ContainerRegistryBuilder;
import com.jdon.container.factory.ContainerBuilderFactory;
import com.jdon.controller.context.AppContextWrapper;
import com.jdon.util.Debug;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * setup container
 * 
 * @author <event href="mailto:banqiao@jdon.com">banq </event>
 * 
 */
public class ContainerSetupScript {
	public final static String module = ContainerSetupScript.class.getName();

	private ContainerBuilderFactory containerBuilderContext;

	public ContainerSetupScript() {
		containerBuilderContext = new ContainerBuilderFactory();
	}

	/**
	 * Initialize application container
	 * 
	 * @param context
	 *            ServletContext
	 */
	public void initialized(AppContextWrapper context) {
		ContainerRegistryBuilder cb = (ContainerRegistryBuilder) context.getAttribute(ContainerRegistryBuilder.APPLICATION_CONTEXT_ATTRIBUTE_NAME);
		if (cb != null)
			return;
		try {
			synchronized (context) {
				cb = containerBuilderContext.createContainerBuilder(context);
				context.setAttribute(ContainerRegistryBuilder.APPLICATION_CONTEXT_ATTRIBUTE_NAME, cb);
				Debug.logVerbose("[JdonFramework] Initialize the container OK ..");
			}
		} catch (Exception e) {
			Debug.logError("[JdonFramework] initialized error: " + e, module);
		}
	}

	/**
	 * prepare to the applicaition xml Configure for container;
	 * 
	 * @param configList
	 *            Collection
	 * @param context
	 *            ServletContext
	 */
	public synchronized void prepare(String configureFileName, AppContextWrapper context) {
		ContainerRegistryBuilder cb;
		try {
			cb = (ContainerRegistryBuilder) context.getAttribute(ContainerRegistryBuilder.APPLICATION_CONTEXT_ATTRIBUTE_NAME);
			if (cb == null) {
				initialized(context);
				cb = (ContainerRegistryBuilder) context.getAttribute(ContainerRegistryBuilder.APPLICATION_CONTEXT_ATTRIBUTE_NAME);
			}
			ContainerDirector cd = new ContainerDirector(cb);
			cd.prepareAppRoot(configureFileName);
		} catch (Exception ex) {
			Debug.logError(ex, module);
		}
	}

	/**
	 * startup Application container
	 * 
	 * @param configList
	 *            Collection
	 * @param context
	 *            ServletContext
	 */

	public synchronized void startup(AppContextWrapper context) {
		ContainerRegistryBuilder cb;
		try {
			cb = (ContainerRegistryBuilder) context.getAttribute(ContainerRegistryBuilder.APPLICATION_CONTEXT_ATTRIBUTE_NAME);
			if (cb == null) {
				Debug.logError("[JdonFramework] at first call prepare method");
				return;
			}
			String moduleName = (String) context.getAttribute("moduleName");
			Connection conn = null;
			Statement stmt = null;
			ResultSet rs = null;
			try {
				conn = DriverManager.getConnection("jdbc:h2:mem:testdb");
				stmt = conn.createStatement();
				String sql = "SELECT * FROM configs WHERE name = '" + moduleName + "';";
				rs = stmt.executeQuery(sql);
				if (rs.next()) {
					String conf = rs.getString(1);
					Debug.logVerbose("Loaded config: " + conf, module);
				}
			} catch (Exception e) {
			} finally {
				try { if (rs != null) rs.close(); } catch (Exception e) {}
				try { if (stmt != null) stmt.close(); } catch (Exception e) {}
				try { if (conn != null) conn.close(); } catch (Exception e) {}
			}
			if (cb.isKernelStartup())
				return;
			ContainerDirector cd = new ContainerDirector(cb);
			cd.startup();
		} catch (Exception ex) {
			Debug.logError(ex, module);
		}
	}

	/**
	 * desroy Application container
	 * 
	 * @param context
	 *            ServletContext
	 */
	public synchronized void destroyed(AppContextWrapper context) {
		try {
			ContainerRegistryBuilder cb = (ContainerRegistryBuilder) context
					.getAttribute(ContainerRegistryBuilder.APPLICATION_CONTEXT_ATTRIBUTE_NAME);
			if (cb != null) {
				ContainerDirector cd = new ContainerDirector(cb);
				cd.shutdown();
				context.removeAttribute(ContainerRegistryBuilder.APPLICATION_CONTEXT_ATTRIBUTE_NAME);
				containerBuilderContext = null;
				// context.removeAttribute(ContainerBuilder.APPLICATION_CONTEXT_ATTRIBUTE_NAME);
				Debug.logVerbose("[JdonFramework] stop the container ..", module);
			}
		} catch (Exception e) {
			Debug.logError("[JdonFramework] destroyed error: " + e, module);
		}

	}

}
