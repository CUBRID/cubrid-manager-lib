/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package com.cubrid.common.ui.er.figures;

import org.eclipse.draw2d.ConnectionAnchor;

/**
 * The interface of ConnectionFigure class.
 *
 * @author CHOE JUNGYEON
 */
public interface IConnectionFigure {
	public void refreshTargetAnchor();

	public void refreshSourceAnchor();

	public ConnectionAnchor getTargetAnchor();

	public ConnectionAnchor getSourceAnchor();
}
