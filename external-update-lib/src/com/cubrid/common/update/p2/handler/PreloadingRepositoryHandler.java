/*******************************************************************************
 * Copyright (c) 2008, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.cubrid.common.update.p2.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.equinox.p2.ui.LoadMetadataRepositoryJob;
import org.eclipse.equinox.p2.ui.ProvisioningUI;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

/**
 * Preloading repository handler. PreloadingRepositoryHandler class is created
 * using same class on org.eclipse.equinox.p2.examples.rcp.cloud.
 * 
 * @author pangqiren, modified at Jan 7,2011
 */
public abstract class PreloadingRepositoryHandler extends AbstractHandler {
	public PreloadingRepositoryHandler() {
	}

	public Object execute(ExecutionEvent event) {
		doExecuteAndLoad();
		return null;
	}

	protected void doExecuteAndLoad() {
		if (preloadRepositories()) {
			// cancel any load that is already running
			Job.getJobManager().cancel(LoadMetadataRepositoryJob.LOAD_FAMILY);
			final LoadMetadataRepositoryJob loadJob = new LoadMetadataRepositoryJob(getProvisioningUI());
			setLoadJobProperties(loadJob);
			if (waitForPreload()) {
				loadJob.addJobChangeListener(new JobChangeAdapter() {
					public void done(IJobChangeEvent event) {
						if (PlatformUI.isWorkbenchRunning() && event.getResult().isOK()) {
							PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
								public void run() {
									doExecute(loadJob);
								}
							});
						}
					}
				});
				loadJob.setUser(true);
				loadJob.schedule();
			} else {
				loadJob.setSystem(true);
				loadJob.setUser(false);
				loadJob.schedule();
				doExecute(null);
			}
		} else {
			doExecute(null);
		}
	}

	protected abstract void doExecute(LoadMetadataRepositoryJob job);

	protected abstract boolean preloadRepositories();

	protected abstract boolean waitForPreload();

	protected void setLoadJobProperties(Job loadJob) {
		loadJob.setProperty(LoadMetadataRepositoryJob.ACCUMULATE_LOAD_ERRORS, Boolean.toString(true));
	}

	protected ProvisioningUI getProvisioningUI() {
		return ProvisioningUI.getDefaultUI();
	}

	protected Shell getShell() {
		return PlatformUI.getWorkbench().getModalDialogShellProvider().getShell();
	}
}
