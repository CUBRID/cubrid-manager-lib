/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package com.cubrid.common.ui.er.router;

import org.eclipse.draw2d.geometry.Point;

/**
 * Represents a 2-dimensional directional Vector, or Ray.
 * {@link java.util.Vector} is commonly imported, so the name Ray was chosen.
 * 
 * @deprecated Use {@link Vector} instead, which offers double precision instead
 *             of integer precision.
 */
public class Ray {
	public int x;
	public int y;

	public static Ray UP = new Ray(0, -1), DOWN = new Ray(0, 1), LEFT = new Ray(-1, 0),
			RIGHT = new Ray(1, 0);
	public static Ray UPRIGHT = new Ray(1, -1), RIGHTDOWN = new Ray(1, 1),
			DOWNLEFT = new Ray(-1, 1), UPLEFT = new Ray(1, -1);

	/**
	 * Constructs a Ray &lt;0, 0&gt; with no direction and magnitude.
	 */
	public Ray() {
	}

	/**
	 * Constructs a Ray pointed in the specified direction.
	 * 
	 * @param x X value.
	 * @param y Y value.
	 */
	public Ray(int x, int y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * Constructs a Ray by other ray.
	 * 
	 * @param ray
	 */
	public Ray(Ray ray) {
		this.x = ray.x;
		this.y = ray.y;
	}

	/**
	 * Constructs a Ray pointed in the direction specified by a Point.
	 * 
	 * @param p the Point
	 */
	public Ray(Point p) {
		x = p.x;
		y = p.y;
	}

	public Point toPoint() {
		return new Point(x, y);
	}

	/**
	 * Get one the default ray(:up,right, down,left). Its relations : 0->up,
	 * 1->right, 2->down, 3->left. other value return: up.
	 * 
	 * @param i
	 * @return
	 */
	public static Ray getRayByIndex(int i) {
		switch (i) {
		case 0:
			return UP;
		case 1:
			return RIGHT;
		case 2:
			return DOWN;
		case 3:
			return LEFT;
		default:
			return UP;
		}
	}

	/**
	 * Constructs a Ray representing the direction and magnitude between to
	 * provided Points.
	 * 
	 * @param start Strarting Point
	 * @param end End Point
	 */
	public Ray(Point start, Point end) {
		x = end.x - start.x;
		y = end.y - start.y;
	}

	/**
	 * Constructs a Ray representing the difference between two provided Rays.
	 * 
	 * @param start The start Ray
	 * @param end The end Ray
	 */
	public Ray(Ray start, Ray end) {
		x = end.x - start.x;
		y = end.y - start.y;
	}

	/**
	 * Calculates the magnitude of the cross product of this Ray with another.
	 * Represents the amount by which two Rays are directionally different.
	 * Parallel Rays return a value of 0.
	 * 
	 * @param r Ray being compared
	 * @return The assimilarity
	 * @see #similarity(Ray)
	 */
	public int assimilarity(Ray r) {
		return Math.abs(x * r.y - y * r.x);
	}

	/**
	 * Calculates the dot product of this Ray with another.
	 * 
	 * @param r the Ray used to perform the dot product
	 * @return The dot product
	 */
	public int dotProduct(Ray r) {
		return x * r.x + y * r.y;
	}

	/**
	 * Calculates the dot product of this Ray with another.
	 * 
	 * @param ray
	 * @return The dot product
	 */
	public Ray getDotProductRay(Ray r) {
		return new Ray(x * r.x, y * r.y);
	}

	/**
	 * Calculates the dot product of this Ray with another.
	 * 
	 * @param r the Ray used to perform the dot product
	 * @return The dot product as <code>long</code> to avoid possible integer
	 *         overflow
	 */
	long dotProductL(Ray r) {
		return (long) x * r.x + (long) y * r.y;
	}

	/**
	 * Get a new ray which is a reversion direction towards this ray
	 * 
	 * @return
	 */
	public Ray getReverseRay() {
		Ray ray = new Ray(this);
		ray.x = -1 * ray.x;
		ray.y = -1 * ray.y;

		return ray;
	}

	/**
	 * @see java.lang.Object#equals(Object)
	 */
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (obj instanceof Ray) {
			Ray r = (Ray) obj;
			return x == r.x && y == r.y;
		}
		return false;
	}

	/**
	 * Creates a new Ray which is the sum of this Ray with another.
	 * 
	 * @param r Ray to be added with this Ray
	 * @return a new Ray
	 */
	public Ray getAdded(Ray r) {
		return new Ray(r.x + x, r.y + y);
	}

	/**
	 * Creates a new Ray which represents the average of this Ray with another.
	 * 
	 * @param r Ray to calculate the average.
	 * @return a new Ray
	 */
	public Ray getAveraged(Ray r) {
		return new Ray((x + r.x) / 2, (y + r.y) / 2);
	}

	/**
	 * Creates a new Ray which represents this Ray scaled by the amount
	 * provided.
	 * 
	 * @param s Value providing the amount to scale.
	 * @return a new Ray
	 */
	public Ray getScaled(int s) {
		return new Ray(x * s, y * s);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + x;
		result = prime * result + y;
		return result;
	}

	/**
	 * Returns true if this Ray has a non-zero horizontal comonent.
	 * 
	 * @return true if this Ray has a non-zero horizontal comonent
	 */
	public boolean isHorizontal() {
		return x != 0;
	}

	/**
	 * Returns the length of this Ray.
	 * 
	 * @return Length of this Ray
	 */
	public double length() {
		return Math.sqrt(dotProductL(this));
	}

	/**
	 * Calculates the similarity of this Ray with another. Similarity is defined
	 * as the absolute value of the dotProduct()
	 * 
	 * @param r Ray being tested for similarity
	 * @return the Similarity
	 * @see #assimilarity(Ray)
	 */
	public int similarity(Ray r) {
		return Math.abs(dotProduct(r));
	}

	/**
	 * @return a String representation
	 */
	public String toString() {
		return "(" + x + "," + y + ")";
	}

}
