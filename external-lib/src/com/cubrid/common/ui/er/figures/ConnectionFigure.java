/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package com.cubrid.common.ui.er.figures;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Color;

import com.cubrid.common.ui.er.router.Ray;

/**
 * Connection Figure for the relationship connection line figure
 *
 * @author Yu Guojia
 * @version 1.0 - 2013-12-3 created by Yu Guojia
 */
public class ConnectionFigure extends PolylineConnection implements IConnectionFigure {
	private static Color defaultColor = ColorConstants.black;
	private static Color HoveringColor = new Color(null, 40, 157, 201);
	private static Color selectedColor = new Color(null, 0, 161, 88);
	private IFigure sourceFigure;
	private IFigure targetFigure;
	private ERConnectionAnchor sourceAnchor;
	private ERConnectionAnchor targetAnchor;
	private boolean isSelected = false;

	public ConnectionFigure(IFigure sourceFigure, IFigure targetFigure) {
		super();
		this.sourceFigure = sourceFigure;
		this.targetFigure = targetFigure;
	}

	/**
	 * Set hover enter appearance to the figure
	 */
	public void setHoverEnter() {
		this.setForegroundColor(HoveringColor);
		this.setLineWidth(2);
	}

	/**
	 * Set hover existed appearance to the figure
	 */
	public void setHoverExist() {
		if (isSelected()) {
			setSelected(true);
		} else {
			setDefaultState();
		}
	}

	public void setDefaultState() {
		this.setForegroundColor(defaultColor);
		this.setLineWidth(1);
	}

	public void setColor(Color color) {
		this.setForegroundColor(color);
	}

	public boolean isSelected() {
		return isSelected;
	}

	public void setSelected(boolean isSelected) {
		if (isSelected) {
			this.setForegroundColor(selectedColor);
		} else {
			this.setForegroundColor(defaultColor);
		}
		this.setLineWidth(1);
		this.isSelected = isSelected;
	}

	/**
	 * Returns the ConnectionAnchor at the <b>source</b> end of this Connection.
	 *
	 * @return The ConnectionAnchor at the <b>source</b> end of this Connection
	 */
	public ConnectionAnchor getSourceAnchor() {
		return sourceAnchor;
	}

	public void refreshSourceAnchor() {
		if (sourceFigure == null || targetFigure == null) {
			return;
		} else {
			Rectangle sourceBound = sourceFigure.getBounds();
			Rectangle targetBound = targetFigure.getBounds();
			Ray ray = getStartDirection(sourceBound, targetBound);
			if (sourceAnchor == null) {
				sourceAnchor = new ERConnectionAnchor(sourceFigure,
						ERConnectionAnchor.getDirect(ray));
			} else {
				sourceAnchor.setAnchor(ERConnectionAnchor.getDirect(ray));
			}
		}
	}

	/**
	 * Returns the ConnectionAnchor at the <b>target</b> end of this Connection.
	 *
	 * @return The ConnectionAnchor at the <b>target</b> end of this Connection
	 */
	public ConnectionAnchor getTargetAnchor() {
		return targetAnchor;
	}

	public void refreshTargetAnchor() {
		if (sourceFigure == null || targetFigure == null) {
			return;
		} else {
			Rectangle sourceBound = sourceFigure.getBounds();
			Rectangle targetBound = targetFigure.getBounds();
			Ray ray = getStartDirection(targetBound, sourceBound);
			if (targetAnchor == null) {
				targetAnchor = new ERConnectionAnchor(targetFigure,
						ERConnectionAnchor.getDirect(ray));
			} else {
				targetAnchor.setAnchor(ERConnectionAnchor.getDirect(ray));
			}
		}
	}

	/**
	 * Get the start direct from start to end by the min distance.
	 *
	 * @param startBound
	 * @param endBound
	 * @return
	 */
	private Ray getStartDirection(Rectangle startBound, Rectangle endBound) {
		Ray ray;
		double tmp;
		double minDistance = -1;
		// up
		tmp = getMinCenterEdgeDistance(startBound.getTop(), endBound);
		minDistance = tmp;
		ray = Ray.UP;
		// down
		tmp = getMinCenterEdgeDistance(startBound.getBottom(), endBound);
		if (minDistance > tmp) {
			minDistance = tmp;
			ray = Ray.DOWN;
		}
		// left
		tmp = getMinCenterEdgeDistance(startBound.getLeft(), endBound);
		if (minDistance > tmp) {
			minDistance = tmp;
			ray = Ray.LEFT;
		}
		// right
		tmp = getMinCenterEdgeDistance(startBound.getRight(), endBound);
		if (minDistance > tmp) {
			minDistance = tmp;
			ray = Ray.RIGHT;
		}

		return ray;
	}

	/**
	 * Get the min distance that the point to center point of the four edges of
	 * the rectangle
	 *
	 * @param point
	 * @param rec
	 * @return
	 */
	private double getMinCenterEdgeDistance(Point point, Rectangle rec) {
		double minDistance = Integer.MAX_VALUE;
		double up = getDistance(point, rec.getTop());
		double right = getDistance(point, rec.getRight());
		double down = getDistance(point, rec.getBottom());
		double left = getDistance(point, rec.getLeft());
		if (minDistance > up) {
			minDistance = up;
		}
		if (minDistance > right) {
			minDistance = right;
		}
		if (minDistance > down) {
			minDistance = down;
		}
		if (minDistance > left) {
			minDistance = left;
		}

		return minDistance;
	}

	private double getDistance(Point point1, Point point2) {
		return point1.getDistance(point2);
	}

	public IFigure getSourceFigure() {
		return sourceFigure;
	}

	/**
	 * Set new or change the source figure, then create or change the anchor
	 *
	 * @param sourceFigure
	 *            the sourceFigure to set
	 */
	public void setSourceFigure(IFigure sourceFigure) {
		this.sourceFigure = sourceFigure;
		if (sourceFigure == null) {
			sourceAnchor = null;
			return;
		}

		Ray ray = Ray.UP;
		if (targetFigure != null) {
			Rectangle sourceBound = sourceFigure.getBounds();
			Rectangle targetBound = targetFigure.getBounds();
			ray = getStartDirection(sourceBound, targetBound);
		}

		if (sourceAnchor != null) {
			sourceAnchor.setAnchor(ERConnectionAnchor.getDirect(ray));
		} else {
			sourceAnchor = new ERConnectionAnchor(sourceFigure,
					ERConnectionAnchor.getDirect(ray));
		}

		// after change the source figure, the target anchor should be changed
		// too.
		refreshTargetAnchor();
	}

	public IFigure getTargetFigure() {
		return targetFigure;
	}

	/**
	 * Set new or change the target figure, then create or change the anchor
	 *
	 * @param targetFigure
	 *            the targetFigure to set
	 */
	public void setTargetFigure(IFigure targetFigure) {
		this.targetFigure = targetFigure;
		if (targetFigure == null) {
			targetAnchor = null;
			return;
		}

		Ray ray = Ray.UP;
		if (sourceFigure != null) {
			Rectangle sourceBound = sourceFigure.getBounds();
			Rectangle targetBound = targetFigure.getBounds();
			ray = getStartDirection(sourceBound, targetBound);
		}

		if (targetAnchor != null) {
			targetAnchor.setAnchor(ERConnectionAnchor.getDirect(ray));
		} else {
			targetAnchor = new ERConnectionAnchor(targetFigure,
					ERConnectionAnchor.getDirect(ray));
		}

		// after change the target figure, the source anchor should be changed
		// too.
		refreshSourceAnchor();
	}
}