/*******************************************************************************
 * Copyright (c) 2012, 2014 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.cloudfoundry.ide.eclipse.internal.server.ui;

import java.net.HttpURLConnection;
import java.net.URL;

import org.eclipse.jst.server.core.Servlet;
import org.eclipse.swt.widgets.Display;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IModuleArtifact;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.model.IURLProvider;
import org.eclipse.wst.server.core.model.IURLProvider2;
import org.eclipse.wst.server.core.util.HttpLaunchable;
import org.eclipse.wst.server.core.util.WebResource;

/**
 * @author Christian Dupuis
 * @author Terry Denney
 */
public class CloudFoundryLaunchable extends HttpLaunchable {

    public CloudFoundryLaunchable(final IServer server, final IModuleArtifact moduleObject) {
        super(new IURLProvider2() {
            public URL getModuleRootURL(IModule module) {
                IURLProvider urlProvider = (IURLProvider) server.loadAdapter(IURLProvider.class, null);
                return urlProvider.getModuleRootURL(module);
            }

            public URL getLaunchableURL() {
                try {

                	
                    URL url = getModuleRootURL(moduleObject.getModule());
                    
                    if (url == null) {
                        return null;
                    }

                    if (moduleObject instanceof Servlet) {
                        Servlet servlet = (Servlet) moduleObject;
                        if (servlet.getAlias() != null) {
                            String path = servlet.getAlias();
                            if (path.startsWith("/"))
                                path = path.substring(1);
                            url = new URL(url, path);
                        } else {
                            url = new URL(url, "servlet/" + servlet.getServletClassName());
                        }
                    } else if (moduleObject instanceof WebResource) {
                        WebResource resource = (WebResource) moduleObject;
                        String path = resource.getPath().toString();
                        if (path != null && path.startsWith("/") && path.length() > 0) {
                            path = path.substring(1);
                        }
                        if (path != null && path.length() > 0) {
                            url = new URL(url, path);
                        }
                    }
                    final URL url2 = url;
                    Display.getDefault().syncExec(new Runnable() {
						public void run() {
							waitForUrlAvaiable(url2, 10000);
						}
                    });

                    return url;
                } catch (Exception e) {
                    return null;
                }
            }
            
            private boolean waitForUrlAvaiable(URL moduleUrl, long timeout) {
                HttpURLConnection conn = null;
                int code = -1;
                long interval = timeout/20;
                for (int i = 0; i < 20; i++) {
                    try {
                        conn = (HttpURLConnection) moduleUrl.openConnection();
                        conn.setUseCaches(false);
                        conn.setRequestMethod("GET");
                        conn.setReadTimeout(5000);
                        conn.connect();
                        code = conn.getResponseCode();
                        if (code != HttpURLConnection.HTTP_NOT_FOUND) {
                            return true;
                        }
                    } catch (Throwable e) {
                        // Do nothing
                    }
                    if (conn != null) {
                        conn.disconnect();
                        conn = null;
                    }
                    try {
                    	Thread.sleep(interval);
                    } catch (Exception e) {
                    	// Do nothing.
                    }
                }
                return false;
            }
        });
    }
}
