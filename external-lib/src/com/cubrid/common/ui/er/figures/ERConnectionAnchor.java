/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package com.cubrid.common.ui.er.figures;

import org.eclipse.draw2d.AbstractConnectionAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;

import com.cubrid.common.ui.er.router.Ray;

/**
 * Anchor for ER tables. The anchor is located on the four edges of table, and
 * the point is default at the middle that the <code> offsetPercent </code> is
 * 50%.
 *
 * @author Yu Guojia
 * @version 1.0 - 2013-12-11 created by Yu Guojia
 */
public class ERConnectionAnchor extends AbstractConnectionAnchor {
	public static int UP = 1;
	public static int RIGHT = 2;
	public static int DOWN = 3;
	public static int LEFT = 4;

	private int anchor;
	private double offsetPercent = 0.5;

	public ERConnectionAnchor(IFigure source, int direction) {
		super(source);
		anchor = direction;
	}

	public ERConnectionAnchor(IFigure source, int direction, double offset) {
		super(source);
		anchor = direction;
		offsetPercent = offset;
	}

	/**
	 * Get the converted direct by ray.
	 *
	 * @param ray
	 * @return
	 */
	public static int getDirect(Ray ray) {
		if (ray.equals(Ray.UP)) {
			return UP;
		} else if (ray.equals(Ray.RIGHT)) {
			return RIGHT;
		} else if (ray.equals(Ray.DOWN)) {
			return DOWN;
		} else if (ray.equals(Ray.LEFT)) {
			return LEFT;
		}

		return UP;
	}

	public Point getLocation(Point reference) {
		Rectangle rect = getOwner().getBounds().getCopy();
		this.getOwner().translateToAbsolute(rect);
		if (anchor == UP) {
			return rect.getTopLeft().translate(
					new Double(rect.width * offsetPercent).intValue(), 0);
		} else if (anchor == RIGHT) {
			return rect.getTopRight().translate(0,
					new Double(rect.height * offsetPercent).intValue());
		} else if (anchor == DOWN) {
			return rect.getBottomLeft().translate(
					new Double(rect.width * offsetPercent).intValue(), 0);
		} else if (anchor == LEFT) {
			return rect.getTopLeft().translate(0,
					new Double(rect.height * offsetPercent).intValue());
		}

		return null;
	}

	public int getAnchor() {
		return anchor;
	}

	public void setAnchor(int anchor) {
		this.anchor = anchor;
	}

	public double getOffsetPercent() {
		return offsetPercent;
	}

	public void setOffsetPercent(double offsetPercent) {
		this.offsetPercent = offsetPercent;
	}
}
