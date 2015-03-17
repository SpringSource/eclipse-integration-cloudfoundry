/*******************************************************************************
 * Copyright (c) 2012, 2014 Pivotal Software, Inc. 
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License, 
 * Version 2.0 (the "License�); you may not use this file except in compliance 
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
package org.cloudfoundry.ide.eclipse.server.ui.internal.editor;

import org.cloudfoundry.ide.eclipse.server.core.internal.CloudFoundryBrandingExtensionPoint;
import org.cloudfoundry.ide.eclipse.server.core.internal.CloudFoundryConstants;
import org.cloudfoundry.ide.eclipse.server.core.internal.CloudFoundryServer;
import org.cloudfoundry.ide.eclipse.server.core.internal.CloudServerEvent;
import org.cloudfoundry.ide.eclipse.server.core.internal.CloudServerListener;
import org.cloudfoundry.ide.eclipse.server.core.internal.ServerEventHandler;
import org.cloudfoundry.ide.eclipse.server.ui.internal.CloudFoundryURLNavigation;
import org.cloudfoundry.ide.eclipse.server.ui.internal.CloudUiUtil;
import org.cloudfoundry.ide.eclipse.server.ui.internal.Messages;
import org.cloudfoundry.ide.eclipse.server.ui.internal.UpdatePasswordDialog;
import org.cloudfoundry.ide.eclipse.server.ui.internal.wizards.OrgsAndSpacesWizard;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.ui.editor.ServerEditorSection;

/**
 * @author Andy Clement
 * @author Christian Dupuis
 * @author Leo Dos Santos
 * @author Steffen Pingel
 * @author Terry Denney
 */
public class CloudFoundryAccountSection extends ServerEditorSection implements CloudServerListener {

	private CloudFoundryServer cfServer;

	private Text emailText;

	private Text passwordText;

	private String sectionTitle;

	private Text urlText;

	private Text orgText;

	private Text spaceText;

	private Label validateLabel;

	// private CloudUrlWidget urlWidget;

	// private Combo urlCombo;

	public CloudFoundryAccountSection() {
	}

	public void update() {
		if (cfServer.getUsername() != null && emailText != null && !cfServer.getUsername().equals(emailText.getText())) {
			emailText.setText(cfServer.getUsername());
		}
		if (cfServer.getPassword() != null && passwordText != null
				&& !cfServer.getPassword().equals(passwordText.getText())) {
			passwordText.setText(cfServer.getPassword());
		}
		if (cfServer.getUrl() != null
				&& urlText != null
				&& !CloudUiUtil.getDisplayTextFromUrl(cfServer.getUrl(), cfServer.getServer().getServerType().getId())
						.equals(urlText.getText())) {
			urlText.setText(CloudUiUtil.getDisplayTextFromUrl(cfServer.getUrl(), cfServer.getServer().getServerType()
					.getId()));
		}
		if (cfServer.hasCloudSpace()) {
			if (cfServer.getCloudFoundrySpace() != null && cfServer.getCloudFoundrySpace().getOrgName() != null
					&& orgText != null && !cfServer.getCloudFoundrySpace().getOrgName().equals(orgText.getText())) {
				orgText.setText(cfServer.getCloudFoundrySpace().getOrgName());
			}
			if (cfServer.getCloudFoundrySpace() != null && cfServer.getCloudFoundrySpace().getSpaceName() != null
					&& spaceText != null && !cfServer.getCloudFoundrySpace().getSpaceName().equals(spaceText.getText())) {
				spaceText.setText(cfServer.getCloudFoundrySpace().getSpaceName());
			}
		}

	}

	@Override
	public void createSection(Composite parent) {
		super.createSection(parent);

		FormToolkit toolkit = getFormToolkit(parent.getDisplay());

		Section section = toolkit.createSection(parent, ExpandableComposite.TWISTIE | ExpandableComposite.TITLE_BAR);
		section.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		section.setText(sectionTitle);

		Composite composite = toolkit.createComposite(section);
		section.setClient(composite);

		GridLayout layout = new GridLayout();
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Composite topComposite = new Composite(composite, SWT.NONE);
		topComposite.setLayout(new GridLayout(2, false));
		topComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		Label emailLabel = toolkit.createLabel(topComposite, Messages.COMMONTXT_EMAIL_WITH_COLON, SWT.NONE);
		emailLabel.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
		emailLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));

		emailText = toolkit.createText(topComposite, ""); //$NON-NLS-1$
		emailText.setEditable(false);
		emailText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		emailText.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
		if (cfServer.getUsername() != null) {
			emailText.setText(cfServer.getUsername());
		}
		emailText.addModifyListener(new DataChangeListener(DataType.EMAIL));

		Label passwordLabel = toolkit.createLabel(topComposite, Messages.COMMONTXT_PW, SWT.NONE);
		passwordLabel.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
		passwordLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));

		passwordText = toolkit.createText(topComposite, "", SWT.PASSWORD); //$NON-NLS-1$
		passwordText.setEditable(false);
		passwordText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		passwordText.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
		if (cfServer.getPassword() != null) {
			passwordText.setText(cfServer.getPassword());
		}
		passwordText.addModifyListener(new DataChangeListener(DataType.PASSWORD));

		Label label = toolkit.createLabel(topComposite, Messages.COMMONTXT_URL);
		label.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));

		urlText = toolkit.createText(topComposite, "", SWT.NONE); //$NON-NLS-1$
		urlText.setEditable(false);
		urlText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		urlText.setData(FormToolkit.KEY_DRAW_BORDER, Boolean.FALSE);
		if (cfServer.getUrl() != null) {
			urlText.setText(CloudUiUtil.getDisplayTextFromUrl(cfServer.getUrl(), cfServer.getServer().getServerType()
					.getId()));
		}

		Label orgLabel = toolkit.createLabel(topComposite, Messages.CloudFoundryAccountSection_LABEL_ORG, SWT.NONE);
		orgLabel.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
		orgLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));

		orgText = toolkit.createText(topComposite, "", SWT.NONE); //$NON-NLS-1$
		orgText.setEditable(false);
		orgText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		orgText.setData(FormToolkit.KEY_DRAW_BORDER, Boolean.FALSE);
		if (cfServer.getCloudFoundrySpace() != null && cfServer.getCloudFoundrySpace().getOrgName() != null) {
			orgText.setText(cfServer.getCloudFoundrySpace().getOrgName());
		}

		Label spaceLabel = toolkit.createLabel(topComposite, Messages.CloudFoundryAccountSection_LABEL_SPACE, SWT.NONE);
		spaceLabel.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
		spaceLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));

		spaceText = toolkit.createText(topComposite, "", SWT.NONE); //$NON-NLS-1$
		spaceText.setEditable(false);
		spaceText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		spaceText.setData(FormToolkit.KEY_DRAW_BORDER, Boolean.FALSE);
		if (cfServer.getCloudFoundrySpace() != null && cfServer.getCloudFoundrySpace().getSpaceName() != null) {
			spaceText.setText(cfServer.getCloudFoundrySpace().getSpaceName());
		}

		// urlWidget = new CloudUrlWidget(cfServer);
		// urlWidget.createControls(topComposite);
		// urlWidget.getUrlCombo().addModifyListener(new
		// DataChangeListener(DataType.URL));
		//
		final Composite buttonComposite = toolkit.createComposite(composite);

		buttonComposite.setLayout(new GridLayout(4, false));
		GridDataFactory.fillDefaults().align(SWT.END, SWT.FILL).grab(true, false).applyTo(buttonComposite);

		final Composite validateComposite = toolkit.createComposite(composite);
		validateComposite.setLayout(new GridLayout(1, false));
		validateComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		validateLabel = toolkit.createLabel(validateComposite, "", SWT.NONE); //$NON-NLS-1$
		validateLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		createCloneServerArea(buttonComposite, toolkit);

		final Button changePasswordButton = toolkit.createButton(buttonComposite,
				Messages.CloudFoundryAccountSection_BUTTON_CHANGE_PW, SWT.PUSH);

		// Pivotal Tracker: 54644658 - Disable for CF 1.5.0 until fixed.
		changePasswordButton.setEnabled(true);

		changePasswordButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		changePasswordButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (server.isDirty()) {
					boolean confirm = MessageDialog.openQuestion(getShell(),
							Messages.CloudFoundryAccountSection_DIALOG_UNSAVE_TITLE,
							Messages.CloudFoundryAccountSection_DIALOG_UNSAVE_BODY);
					if (!confirm) {
						return;
					}
				}

				UpdatePasswordDialog dialog = new UpdatePasswordDialog(getShell(), cfServer.getUsername());

				if (dialog.open() == IDialogConstants.OK_ID) {
					final String newPassword = dialog.getPassword();
					String errorMsg = CloudUiUtil.updatePassword(newPassword, cfServer, server);

					if (errorMsg != null) {
						validateLabel.setText(errorMsg);
						validateLabel.setForeground(validateLabel.getDisplay().getSystemColor(SWT.COLOR_RED));
					}
					else {
						validateLabel.setText(Messages.CloudFoundryAccountSection_LABEL_PW_CHANGED);
						passwordText.setText(newPassword);
					}
				}
			}
		});

		final Button validateButton = toolkit.createButton(buttonComposite,
				Messages.CloudFoundryAccountSection_BUTTON_VALIDATE_ACCOUNT, SWT.PUSH);
		validateButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		validateButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				final String url = cfServer.getUrl();
				final String userName = emailText.getText();
				final String password = passwordText.getText();
				final String org = orgText.getText();
				final String space = spaceText.getText();
				try {
					CloudUiUtil.validateCredentials(userName, password, url, false,
							cfServer.getSelfSignedCertificate(), null);

					if (org != null && space != null) {
						validateLabel.setText(Messages.VALID_ACCOUNT);
						validateLabel.setForeground(validateLabel.getDisplay().getSystemColor(SWT.COLOR_BLACK));
					}
					else {
						String errorMsg = null;
						if (org == null) {
							errorMsg = Messages.ERROR_INVALID_ORG;
						}
						else if (space == null) {
							errorMsg = Messages.ERROR_INVALID_SPACE;
						}
						validateLabel.setText(errorMsg);
						validateLabel.setForeground(validateLabel.getDisplay().getSystemColor(SWT.COLOR_RED));
					}

				}
				catch (CoreException e) {
					validateLabel.setText(e.getMessage());
				}
				buttonComposite.layout(new Control[] { validateButton });
				validateComposite.layout(new Control[] { validateLabel });
			}
		});

		// Create signup button only if the server is not local or micro
		if (CloudFoundryURLNavigation.canEnableCloudFoundryNavigation(cfServer)) {
			Button cfSignup = toolkit.createButton(buttonComposite,
					CloudFoundryConstants.PUBLIC_CF_SERVER_SIGNUP_LABEL, SWT.PUSH);
			cfSignup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
			cfSignup.addSelectionListener(new SelectionAdapter() {

				public void widgetSelected(SelectionEvent event) {
					IServer iServer = cfServer.getServer();
					if (iServer != null) {
						String signupURL = CloudFoundryBrandingExtensionPoint.getSignupURL(cfServer.getServerId(),
								cfServer.getUrl());
						if (signupURL != null) {
							CloudFoundryURLNavigation nav = new CloudFoundryURLNavigation(signupURL);
							nav.navigate();
						}
					}
				}
			});
		}

		toolkit.paintBordersFor(topComposite);
		section.setExpanded(true);

		ServerEventHandler.getDefault().addServerListener(this);
	}

	protected void createCloneServerArea(Composite parent, FormToolkit toolkit) {
		final Button changeSpaceButton = toolkit.createButton(parent,
				Messages.CloudFoundryAccountSection_BUTTON_CLONE_SERVER, SWT.PUSH);

		changeSpaceButton.setEnabled(true);

		changeSpaceButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		changeSpaceButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				OrgsAndSpacesWizard wizard = new OrgsAndSpacesWizard(cfServer);

				WizardDialog dialog = new WizardDialog(getShell(), wizard);
				dialog.open();

			}
		});
	}

	@Override
	public void init(IEditorSite site, IEditorInput input) {
		super.init(site, input);
		// String serviceName = null;
		if (server != null) {
			cfServer = (CloudFoundryServer) server.loadAdapter(CloudFoundryServer.class, null);
			update();
			// serviceName =
			// CloudFoundryBrandingExtensionPoint.getServiceName(server.getServerType().getId());
		}
		// if (serviceName == null) {
		sectionTitle = Messages.COMMONTXT_ACCOUNT_INFO;

		// }
		// else {
		// sectionTitle = serviceName + " Account";
		// }
	}

	private class DataChangeListener implements ModifyListener {

		private String newValue;

		private String oldValue;

		private final DataType type;

		private DataChangeListener(DataType type) {
			this.type = type;
		}

		public void modifyText(ModifyEvent e) {
			switch (type) {
			case EMAIL:
				oldValue = cfServer.getUsername();
				newValue = emailText.getText();
				break;
			case PASSWORD:
				oldValue = cfServer.getPassword();
				newValue = passwordText.getText();
				break;
			// case URL:
			// Combo urlCombo = urlWidget.getUrlCombo();
			// int index = urlCombo.getSelectionIndex();
			// oldValue = cfServer.getUrl();
			// newValue = index < 0? null:
			// CloudUiUtil.getUrlFromDisplayText(urlCombo.getItem(index));
			// break;
			}

			execute(new AbstractOperation("CloudFoundryServerUpdate") { //$NON-NLS-1$

				@Override
				public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
					updateServer(newValue);
					return Status.OK_STATUS;
				}

				@Override
				public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
					updateServer(newValue);
					return Status.OK_STATUS;
				}

				@Override
				public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
					updateServer(oldValue);
					return Status.OK_STATUS;
				}

				private void updateServer(String value) {
					switch (type) {
					case EMAIL:
						if (!value.equals(cfServer.getUsername())) {
							cfServer.setUsername(value);
						}
						updateTextField(value, emailText);
						break;
					case PASSWORD:
						if (!value.equals(cfServer.getPassword())) {
							cfServer.setPassword(value);
						}
						updateTextField(value, passwordText);
						break;
					// case URL:
					// cfServer.setUrl(value);
					// updateComboBox(value, urlWidget.getUrlCombo());
					// break;
					}
				}

				private void updateTextField(String input, Text text) {
					if (!text.getText().equals(input)) {
						text.setText(input == null ? "" : input); //$NON-NLS-1$
					}
				}

				// private void updateComboBox(String input, Combo combo) {
				// int index = combo.getSelectionIndex();
				// if (index < 0) {
				// if (input == null) {
				// return;
				// }
				// } else if (combo.getItem(index).equals(input)) {
				// return;
				// }
				//
				// for(int i=0; i<combo.getItemCount(); i++) {
				// if (combo.getItem(i).equals(input)) {
				// combo.select(i);
				// return;
				// }
				// }
				// combo.deselectAll();
				// }
			});
		}
	}

	private enum DataType {
		EMAIL, PASSWORD, URL
	}

	public void serverChanged(CloudServerEvent event) {
		if (event.getType() == CloudServerEvent.EVENT_UPDATE_PASSWORD) {
			cfServer = event.getServer();

			Display.getDefault().syncExec(new Runnable() {

				public void run() {
					if (passwordText != null && !passwordText.isDisposed()
							&& !passwordText.getText().equals(cfServer.getPassword())) {
						passwordText.setText(cfServer.getPassword());
					}
				}
			});
		}
	}

	@Override
	public void dispose() {
		ServerEventHandler.getDefault().removeServerListener(this);
	}

}
