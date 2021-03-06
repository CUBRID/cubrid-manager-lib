/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.cubrid.common.ui.query.control.jface.text;


/**
 * A listener which is notified when the target's input changes.
 * <p>
 * Clients can implement that interface and its extension interfaces.</p>
 *
 * @since 3.4
 */
public interface IInputChangedListener {

	/**
	 * Called when a the input has changed.
	 *
	 * @param newInput the new input, or <code>null</code> iff the listener should not show any new input
	 */
	void inputChanged(Object newInput);
}
