/*******************************************************************************
 * Copyright (c) 2013, 2015 Pivotal Software, Inc. 
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
package org.cloudfoundry.ide.eclipse.server.ui.internal.wizards;

import org.cloudfoundry.ide.eclipse.server.core.ApplicationDeploymentInfo;
import org.cloudfoundry.ide.eclipse.server.core.internal.CloudFoundryServer;
import org.cloudfoundry.ide.eclipse.server.ui.internal.CloudFoundryImages;
import org.cloudfoundry.ide.eclipse.server.ui.internal.EnvironmentVariablesPart;
import org.cloudfoundry.ide.eclipse.server.ui.internal.IPartChangeListener;
import org.cloudfoundry.ide.eclipse.server.ui.internal.Messages;
import org.cloudfoundry.ide.eclipse.server.ui.internal.PartChangeEvent;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;

public class CloudFoundryApplicationEnvVarWizardPage extends PartsWizardPage {

	private final CloudFoundryServer cloudServer;

	protected final ApplicationDeploymentInfo deploymentInfo;

	private EnvironmentVariablesPart envVarPart;

	public CloudFoundryApplicationEnvVarWizardPage(CloudFoundryServer cloudServer,
			ApplicationDeploymentInfo deploymentInfo) {
		super(Messages.CloudFoundryApplicationEnvVarWizardPage_TEXT_ENV_VAR_WIZ, null, null);
		Assert.isNotNull(deploymentInfo);

		this.cloudServer = cloudServer;
		this.deploymentInfo = deploymentInfo;
	}

	public void createControl(Composite parent) {
		setTitle(Messages.COMMONTXT_ENV_VAR);
		setDescription(Messages.CloudFoundryApplicationEnvVarWizardPage_TEXT_EDIT_ENV_VAR);
		ImageDescriptor banner = CloudFoundryImages.getWizardBanner(cloudServer.getServer().getServerType().getId());
		if (banner != null) {
			setImageDescriptor(banner);
		}

		Composite mainArea = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(2).spacing(new Point(SWT.DEFAULT,20)).applyTo(mainArea);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(mainArea);
		envVarPart = new EnvironmentVariablesPart();

		envVarPart.addPartChangeListener(new IPartChangeListener() {

			public void handleChange(PartChangeEvent event) {
				deploymentInfo.setEnvVariables(envVarPart.getVariables());
			}
		});
		envVarPart.createPart(mainArea);

		if (deploymentInfo.getEnvVariables() != null) {
			envVarPart.setInput(deploymentInfo.getEnvVariables());
		}

		setControl(mainArea);

	}

	public boolean isPageComplete() {
		return true;
	}

}
