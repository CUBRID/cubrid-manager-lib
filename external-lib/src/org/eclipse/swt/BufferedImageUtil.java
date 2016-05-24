/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.swt;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DirectColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;

import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;

public class BufferedImageUtil {
	/**
	 * Get AWT Image.
	 * This method is created using Snippet156.java on
	 * http://www.eclipse.org/swt/snippets/ site.
	 * 
	 * @param imageData
	 * @return
	 */
	public static BufferedImage getAWTImage(ImageData imageData) {
		ColorModel colorModel = null;
		PaletteData paletteData = imageData.palette;
		if (paletteData.isDirect) {
			colorModel = new DirectColorModel(imageData.depth, paletteData.redMask,
					paletteData.greenMask, paletteData.blueMask);
			BufferedImage bufferedImage = new BufferedImage(colorModel,
					colorModel.createCompatibleWritableRaster(imageData.width, imageData.height),
					false, null);
			for (int y = 0; y < imageData.height; y++) {
				for (int x = 0; x < imageData.width; x++) {
					int pixel = imageData.getPixel(x, y);
					RGB rgb = paletteData.getRGB(pixel);
					bufferedImage.setRGB(x, y, rgb.red << 16 | rgb.green << 8 | rgb.blue);
				}
			}
			return bufferedImage;
		} else {
			RGB[] rgbs = paletteData.getRGBs();
			byte[] red = new byte[rgbs.length];
			byte[] green = new byte[rgbs.length];
			byte[] blue = new byte[rgbs.length];
			for (int i = 0; i < rgbs.length; i++) {
				RGB rgb = rgbs[i];
				red[i] = (byte) rgb.red;
				green[i] = (byte) rgb.green;
				blue[i] = (byte) rgb.blue;
			}
			if (imageData.transparentPixel != -1) {
				colorModel = new IndexColorModel(imageData.depth, rgbs.length, red, green, blue,
						imageData.transparentPixel);
			} else {
				colorModel = new IndexColorModel(imageData.depth, rgbs.length, red, green, blue);
			}
			BufferedImage bufferedImage = new BufferedImage(colorModel,
					colorModel.createCompatibleWritableRaster(imageData.width, imageData.height),
					false, null);
			WritableRaster writableRaster = bufferedImage.getRaster();
			int[] pixelArray = new int[1];
			for (int y = 0; y < imageData.height; y++) {
				for (int x = 0; x < imageData.width; x++) {
					int pixel = imageData.getPixel(x, y);
					pixelArray[0] = pixel;
					writableRaster.setPixel(x, y, pixelArray);
				}
			}
			return bufferedImage;
		}
	}
}
