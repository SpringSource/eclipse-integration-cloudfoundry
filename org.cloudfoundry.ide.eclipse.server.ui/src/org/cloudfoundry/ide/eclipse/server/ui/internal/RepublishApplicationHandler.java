/*******************************************************************************
 * Copyright (c) 2012, 2015 Pivotal Software, Inc. 
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License, 
 * Version 2.0 (the "License"); you may not use this file except in compliance 
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *  
 *  Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 ********************************************************************************/
package org.cloudfoundry.ide.eclipse.server.ui.internal;

import java.util.List;

import org.cloudfoundry.client.lib.domain.CloudApplication;
import org.cloudfoundry.ide.eclipse.server.core.internal.CloudFoundryPlugin;
import org.cloudfoundry.ide.eclipse.server.core.internal.CloudFoundryProjectUtil;
import org.cloudfoundry.ide.eclipse.server.core.internal.CloudFoundryServer;
import org.cloudfoundry.ide.eclipse.server.core.internal.RepublishModule;
import org.cloudfoundry.ide.eclipse.server.core.internal.client.CloudFoundryApplicationModule;
import org.cloudfoundry.ide.eclipse.server.core.internal.client.DeploymentInfoWorkingCopy;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.osgi.util.NLS;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.ServerUtil;

/**
 * This handles automatic publishing of an app that has an accessible workspace
 * project by using the same descriptor information as set the first time the
 * manual publishing occurred. This avoids prompting a user again for the
 * descriptor information.
 * 
 * 
 */
public class RepublishApplicationHandler {

	private final CloudFoundryApplicationModule appModule;

	private final List<String> uris;

	private final CloudFoundryServer cloudServer;

	public RepublishApplicationHandler(CloudFoundryApplicationModule appModule, List<String> uris,
			CloudFoundryServer cloudServer) {
		this.appModule = appModule;
		this.uris = uris;
		this.cloudServer = cloudServer;
	}

	protected CloudApplication getExistingCloudApplication(IProgressMonitor monitor) {

		CloudApplication existingApp = appModule.getApplication();
		if (existingApp == null) {
			try {
				existingApp = cloudServer.getBehaviour().getCloudApplication(appModule.getDeployedApplicationName(),
						monitor);
			}
			catch (CoreException e) {
				// Ignore it
			}
		}
		return existingApp;
	}

	public void republish(IProgressMonitor monitor) throws CoreException {

		IProject project = CloudFoundryProjectUtil.getProject(appModule);

		boolean republished = false;

		// Can only republish modules that have an accessible project
		if (project != null) {

			// Get the descriptor from the existing application module
			DeploymentInfoWorkingCopy workingCopy = appModule.resolveDeploymentInfoWorkingCopy(monitor);
			workingCopy.setUris(uris);
			workingCopy.save();
			IServer server = cloudServer.getServer();

			final IModule[] modules = ServerUtil.getModules(project);

			if (modules != null && modules.length == 1) {
				IModule[] add = null;
				if (!ServerUtil.containsModule(server, modules[0], monitor)) {
					add = new IModule[] { modules[0] };
				}
				else {
					// Delete them first
					IServerWorkingCopy wc = server.createWorkingCopy();
					wc.modifyModules(null, modules, monitor);
					wc.save(true, null);
					cloudServer.getBehaviour().updateCloudModule(modules[0], monitor);

					// Create new ones
					IModule[] newModules = ServerUtil.getModules(project);
					if (newModules != null && newModules.length == 1) {
						add = new IModule[] { newModules[0] };
					}
				}
				if (add != null && add.length > 0) {
					IServerWorkingCopy wc = server.createWorkingCopy();
					wc = server.createWorkingCopy();
					IStatus status = wc.canModifyModules(add, null, null);
					if (status.getSeverity() != IStatus.ERROR) {
						CloudFoundryPlugin.getModuleCache().getData(wc.getOriginal())
								.tagForAutomaticRepublish(new RepublishModule(add[0], appModule.getDeploymentInfo()));

						// publish the module
						wc.modifyModules(add, null, monitor);
						wc.save(true, null);
						republished = true;
					}
					else {
						throw new CoreException(status);
					}
				}
			}

		}

		if (!republished) {
			throw new CoreException(CloudFoundryPlugin.getErrorStatus(NLS.bind(
					Messages.RepublishApplicationHandler_ERROR_REPUBLISH_FAIL, appModule.getDeployedApplicationName())));
		}

	}

}
