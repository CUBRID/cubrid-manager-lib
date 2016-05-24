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

import org.eclipse.gef.EditDomain;
import org.eclipse.gef.dnd.TemplateTransferDragSourceListener;
import org.eclipse.gef.ui.palette.PaletteViewer;
import org.eclipse.gef.ui.palette.PaletteViewerProvider;

/**
 * PaletteViewerProvider subclass used for initialising drag and drop
 * 
 * @author Yu Guojia
 * @version 1.0 - 2013-7-12 created by Yu Guojia
 */
public class SchemaPaletteViewerProvider extends PaletteViewerProvider {
	public SchemaPaletteViewerProvider(EditDomain editDomain) {
		super(editDomain);
	}

	@Override
	protected void configurePaletteViewer(PaletteViewer paletteViewer) {
		super.configurePaletteViewer(paletteViewer);
		paletteViewer.addDragSourceListener(new TemplateTransferDragSourceListener(paletteViewer));
	}
}