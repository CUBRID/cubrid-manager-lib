/*******************************************************************************
 * Copyright (c) 2014 Search Solution Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Yu Guojia
 *******************************************************************************/
package com.cubrid.common.ui.er;

import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import org.eclipse.swt.events.FocusEvent;

/**
 * GraphicalViewer which also knows about ValidationMessageHandler to output
 * error messages to
 * 
 * @author Yu Guojia
 * @version 1.0 - 2013-7-10 created by Yu Guojia
 */
public class ValidationGraphicalViewer extends ScrollingGraphicalViewer {
	private final ValidationMessageHandler messageHandler;
	
	public ValidationGraphicalViewer(ValidationMessageHandler messageHandler) {
		super();
		this.messageHandler = messageHandler;
	}

	public ValidationMessageHandler getValidationHandler() {
		return messageHandler;
	}

	@Override
	protected void handleFocusLost(FocusEvent fe) {
		super.handleFocusLost(fe);
		messageHandler.reset();
	}
}