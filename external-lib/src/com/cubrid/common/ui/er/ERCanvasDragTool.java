/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.cubrid.common.ui.er;

import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.SharedCursors;
import org.eclipse.gef.tools.SelectionTool;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Cursor;

/**
 * Expand <code>SelectionTool</code> for dragging the erd canvas
 * 
 * @author Yu Guojia
 * @version 1.0 - 2014-2-27 created by Yu Guojia
 */
public class ERCanvasDragTool extends
		SelectionTool {

	private boolean isSpaceBarDown = false;
	private Point viewLocation;

	//The state to indicate that the space bar has been pressed but no drag has been initiated.
	protected static final int PAN = 1;
	//The state to indicate that a pan is in progress.
	protected static final int PAN_IN_PROGRESS = 2;
	//Max state
	protected static final int MAX_STATE = 2000;

	public ERCanvasDragTool() {
		super();
	}

	/**
	 * Shows the given cursor on the current viewer.
	 * 
	 * @param cursor the cursor to display
	 */
	public void setCursor(Cursor cursor) {
		if (getCurrentViewer() != null) {
			getCurrentViewer().setCursor(cursor);
		}
	}

	/**
	 * Returns <code>true</code> if spacebar condition was accepted.
	 * 
	 * @param e the key event
	 * @return true if the space bar was the key event.
	 */
	protected boolean acceptSpaceBar(KeyEvent e) {
		return (e.character == ' ' && (e.stateMask & SWT.MODIFIER_MASK) == 0);
	}

	/**
	 * @see org.eclipse.gef.tools.AbstractTool#getDebugName()
	 */
	protected String getDebugName() {
		return "ERD canvas dragging tool";
	}

	/**
	 * @see org.eclipse.gef.tools.AbstractTool#getDebugNameForState(int)
	 */
	protected String getDebugNameForState(int state) {
		if (state == PAN) {
			return "ERD canvas dragging Initial";
		} else if (state == PAN_IN_PROGRESS) {
			return "ERD dragging In Progress";
		}
		return super.getDebugNameForState(state);
	}

	/**
	 * Returns the cursor used under normal conditions.
	 * 
	 * @see #setDefaultCursor(Cursor)
	 * @return the default cursor
	 */
	protected Cursor getDefaultCursor() {
		if (isInState(PAN | PAN_IN_PROGRESS)) {
			return SharedCursors.HAND;
		}
		return super.getDefaultCursor();
	}

	/**
	 * @see org.eclipse.gef.tools.SelectionTool#handleButtonDown(int)
	 */
	protected boolean handleButtonDown(int which) {
		if (which == 1 && getCurrentViewer().getControl() instanceof FigureCanvas
				&& stateTransition(PAN, PAN_IN_PROGRESS)) {
			viewLocation = ((FigureCanvas) getCurrentViewer().getControl()).getViewport().getViewLocation();
			return true;
		}
		return super.handleButtonDown(which);
	}

	/**
	 * @see org.eclipse.gef.tools.SelectionTool#handleButtonUp(int)
	 */
	protected boolean handleButtonUp(int which) {
		if (which == 1 && isSpaceBarDown && stateTransition(PAN_IN_PROGRESS, PAN)) {
			return true;
		} else if (which == 1 && stateTransition(PAN_IN_PROGRESS, STATE_INITIAL)) {
			refreshCursor();
			return true;
		}

		return super.handleButtonUp(which);
	}

	/**
	 * @see org.eclipse.gef.tools.AbstractTool#handleDrag()
	 */
	protected boolean handleDrag() {
		if (isInState(PAN_IN_PROGRESS) && getCurrentViewer().getControl() instanceof FigureCanvas) {
			FigureCanvas canvas = (FigureCanvas) getCurrentViewer().getControl();
			int x = viewLocation.x - getDragMoveDelta().width;
			int y = viewLocation.y - getDragMoveDelta().height;
			canvas.scrollTo(x, y);
			return true;
		} else {
			return super.handleDrag();
		}
	}

	/**
	 * @see org.eclipse.gef.tools.SelectionTool#handleFocusLost()
	 */
	protected boolean handleFocusLost() {
		if (isInState(PAN | PAN_IN_PROGRESS)) {
			setState(STATE_INITIAL);
			refreshCursor();
			return true;
		}
		return super.handleFocusLost();
	}

	/**
	 * @see org.eclipse.gef.tools.SelectionTool#handleKeyDown(org.eclipse.swt.events.KeyEvent)
	 */
	protected boolean handleKeyDown(KeyEvent e) {
		if (acceptSpaceBar(e)) {
			isSpaceBarDown = true;
			if (stateTransition(STATE_INITIAL, PAN)) {
				refreshCursor();
			}
			return true;
		} else {
			if (stateTransition(PAN, STATE_INITIAL)) {
				refreshCursor();
				isSpaceBarDown = false;
				return true;
			} else if (isInState(PAN_IN_PROGRESS)) {
				isSpaceBarDown = false;
			}
		}

		return super.handleKeyDown(e);
	}

	/**
	 * @see org.eclipse.gef.tools.SelectionTool#handleKeyUp(org.eclipse.swt.events.KeyEvent)
	 */
	protected boolean handleKeyUp(KeyEvent e) {
		if (acceptSpaceBar(e)) {
			isSpaceBarDown = false;
			if (stateTransition(PAN, STATE_INITIAL)) {
				refreshCursor();
			}
			return true;
		}

		return super.handleKeyUp(e);
	}
}
