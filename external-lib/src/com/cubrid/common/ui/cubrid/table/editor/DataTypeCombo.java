/*******************************************************************************
 * Copyright (c) 2008, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.cubrid.common.ui.cubrid.table.editor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.ACC;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleControlAdapter;
import org.eclipse.swt.accessibility.AccessibleControlEvent;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.accessibility.AccessibleTextAdapter;
import org.eclipse.swt.accessibility.AccessibleTextEvent;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TypedListener;

/**
 * DataTypeCombo
 * This class is created using org.eclipse.swt.widgets.List class.
 *
 * @author Kevin.Wang
 * Create at 2014-4-3
 */
public class DataTypeCombo extends Composite {

	static int checkStyle(int style) {
		int mask = SWT.BORDER | SWT.READ_ONLY | SWT.FLAT | SWT.LEFT_TO_RIGHT
				| SWT.RIGHT_TO_LEFT;
		return SWT.NO_FOCUS | (style & mask);
	}
	Shell parentShell;
	Button arrow;
	Font font;
	Color foreground, background;
	boolean hasFocus;
	List list;
	Listener listener, filter;
	Shell popup;
	Text text;

	int visibleItemCount = 10;

	public DataTypeCombo(Composite parent, int style) {
		super(parent, style = checkStyle(style));
		this.parentShell = super.getShell();

		int textStyle = SWT.SINGLE;
		if ((style & SWT.READ_ONLY) != 0)
			textStyle |= SWT.READ_ONLY;
		if ((style & SWT.FLAT) != 0)
			textStyle |= SWT.FLAT;
		text = new Text(this, textStyle);
		text.addVerifyListener(new VerifyListener() {
			public void verifyText(VerifyEvent e) {
				if (text.getText() != null && text.getText().trim().length() != 0 && !text.getText().toUpperCase().startsWith("ENUM")) {
					e.text = e.text.toUpperCase();
				}
			}
		});

		int arrowStyle = SWT.ARROW | SWT.DOWN;
		if ((style & SWT.FLAT) != 0)
			arrowStyle |= SWT.FLAT;
		arrow = new Button(this, arrowStyle);

		listener = new Listener() {
			public void handleEvent(Event event) {
				if (isDisposed())
					return;
				if (popup == event.widget) {
					popupEvent(event);
					return;
				}
				if (text == event.widget) {
					textEvent(event);
					return;
				}
				if (list == event.widget) {
					listEvent(event);
					return;
				}
				if (arrow == event.widget) {
					arrowEvent(event);
					return;
				}
				if (DataTypeCombo.this == event.widget) {
					comboEvent(event);
					return;
				}
				if (getShell() == event.widget) {
					getDisplay().asyncExec(new Runnable() {
						public void run() {
							if (isDisposed())
								return;
							handleFocus(SWT.FocusOut);
						}
					});
				}
			}
		};
		filter = new Listener() {
			public void handleEvent(Event event) {
				if (isDisposed())
					return;
				if (event.type == SWT.Selection) {
					if (event.widget instanceof ScrollBar) {
						handleScroll(event);
					}
					return;
				}
				Shell shell = ((Control) event.widget).getShell();
				if (shell == DataTypeCombo.this.getShell()) {
					handleFocus(SWT.FocusOut);
				}
			}
		};

		int[] comboEvents = { SWT.Dispose, SWT.FocusIn, SWT.Move, SWT.Resize };
		for (int i = 0; i < comboEvents.length; i++)
			this.addListener(comboEvents[i], listener);

		int[] textEvents = { SWT.DefaultSelection, SWT.DragDetect, SWT.KeyDown,
				SWT.KeyUp, SWT.MenuDetect, SWT.Modify, SWT.MouseDown,
				SWT.MouseUp, SWT.MouseDoubleClick, SWT.MouseEnter,
				SWT.MouseExit, SWT.MouseHover, SWT.MouseMove, SWT.MouseWheel,
				SWT.Traverse, SWT.FocusIn, SWT.Verify };
		for (int i = 0; i < textEvents.length; i++)
			text.addListener(textEvents[i], listener);

		int[] arrowEvents = { SWT.DragDetect, SWT.MouseDown, SWT.MouseEnter,
				SWT.MouseExit, SWT.MouseHover, SWT.MouseMove, SWT.MouseUp,
				SWT.MouseWheel, SWT.Selection, SWT.FocusIn };
		for (int i = 0; i < arrowEvents.length; i++)
			arrow.addListener(arrowEvents[i], listener);

		createPopup(null, -1);
		initAccessible();
	}

	/*
	 * Return the lowercase of the first non-'&' character following an '&'
	 * character in the given string. If there are no '&' characters in the
	 * given string, return '\0'.
	 */
	char _findMnemonic(String string) {
		if (string == null)
			return '\0';
		int index = 0;
		int length = string.length();
		do {
			while (index < length && string.charAt(index) != '&')
				index++;
			if (++index >= length)
				return '\0';
			if (string.charAt(index) != '&')
				return Character.toLowerCase(string.charAt(index));
			index++;
		} while (index < length);
		return '\0';
	}

	public void add(String string) {
		checkWidget();
		if (string == null)
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		list.add(string);
	}

	public void add(String string, int index) {
		checkWidget();
		if (string == null)
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		list.add(string, index);
	}

	public void addModifyListener(ModifyListener listener) {
		checkWidget();
		if (listener == null)
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		TypedListener typedListener = new TypedListener(listener);
		addListener(SWT.Modify, typedListener);
	}

	public void addSelectionListener(SelectionListener listener) {
		checkWidget();
		if (listener == null)
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		TypedListener typedListener = new TypedListener(listener);
		addListener(SWT.Selection, typedListener);
		addListener(SWT.DefaultSelection, typedListener);
	}

	public void addVerifyListener(VerifyListener listener) {
		checkWidget();
		if (listener == null)
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		TypedListener typedListener = new TypedListener(listener);
		addListener(SWT.Verify, typedListener);
	}

	void arrowEvent(Event event) {
		switch (event.type) {
		case SWT.FocusIn: {
			handleFocus(SWT.FocusIn);
			break;
		}
		case SWT.DragDetect:
		case SWT.MouseDown:
		case SWT.MouseUp:
		case SWT.MouseMove:
		case SWT.MouseEnter:
		case SWT.MouseExit:
		case SWT.MouseHover: {
			Point pt = getDisplay().map(arrow, this, event.x, event.y);
			event.x = pt.x;
			event.y = pt.y;
			notifyListeners(event.type, event);
			event.type = SWT.None;
			break;
		}
		case SWT.MouseWheel: {
			Point pt = getDisplay().map(arrow, this, event.x, event.y);
			event.x = pt.x;
			event.y = pt.y;
			notifyListeners(SWT.MouseWheel, event);
			event.type = SWT.None;
			if (isDisposed())
				break;
			if (!event.doit)
				break;
			if (event.count != 0) {
				event.doit = false;
				int oldIndex = getSelectionIndex();
				if (event.count > 0) {
					select(Math.max(oldIndex - 1, 0));
				} else {
					select(Math.min(oldIndex + 1, getItemCount() - 1));
				}
				if (oldIndex != getSelectionIndex()) {
					Event e = new Event();
					e.time = event.time;
					e.stateMask = event.stateMask;
					notifyListeners(SWT.Selection, e);
				}
				if (isDisposed())
					break;
			}
			break;
		}
		case SWT.Selection: {
			text.setFocus();
			dropDown(!isDropped());
			break;
		}
		}
	}

	protected void checkSubclass() {
	}

	public void clearSelection() {
		checkWidget();
		text.clearSelection();
		list.deselectAll();
	}

	void comboEvent(Event event) {
		switch (event.type) {
		case SWT.Dispose:
			removeListener(SWT.Dispose, listener);
			notifyListeners(SWT.Dispose, event);
			event.type = SWT.None;

			if (popup != null && !popup.isDisposed()) {
				list.removeListener(SWT.Dispose, listener);
				popup.dispose();
			}
			Shell shell = getShell();
			shell.removeListener(SWT.Deactivate, listener);
			Display display = getDisplay();
			display.removeFilter(SWT.FocusIn, filter);
			popup = null;
			text = null;
			list = null;
			arrow = null;
			this.parentShell = null;
			break;
		case SWT.FocusIn:
			Control focusControl = getDisplay().getFocusControl();
			if (focusControl == arrow || focusControl == list)
				return;
			dropDown(!isDropped());
			if (isDropped()) {
				list.setFocus();
			} else {
				text.setFocus();
			}
			break;
		case SWT.Move:
			dropDown(false);
			break;
		case SWT.Resize:
			internalLayout(false);
			break;
		}
	}

	public Point computeSize(int wHint, int hHint, boolean changed) {
		checkWidget();
		int width = 0, height = 0;
		String[] items = list.getItems();
		GC gc = new GC(text);
		int spacer = gc.stringExtent(" ").x; //$NON-NLS-1$
		int textWidth = gc.stringExtent(text.getText()).x;
		for (int i = 0; i < items.length; i++) {
			textWidth = Math.max(gc.stringExtent(items[i]).x, textWidth);
		}
		gc.dispose();
		Point textSize = text.computeSize(SWT.DEFAULT, SWT.DEFAULT, changed);
		Point arrowSize = arrow.computeSize(SWT.DEFAULT, SWT.DEFAULT, changed);
		Point listSize = list.computeSize(SWT.DEFAULT, SWT.DEFAULT, changed);
		int borderWidth = getBorderWidth();

		height = Math.max(textSize.y, arrowSize.y);
		width = Math.max(
				textWidth + 2 * spacer + arrowSize.x + 2 * borderWidth,
				listSize.x);
		if (wHint != SWT.DEFAULT)
			width = wHint;
		if (hHint != SWT.DEFAULT)
			height = hHint;
		return new Point(width + 2 * borderWidth, height + 2 * borderWidth);
	}

	public void copy() {
		checkWidget();
		text.copy();
	}

	void createPopup(String[] items, int selectionIndex) {
		// create shell and list
		popup = new Shell(getShell(), SWT.NO_TRIM | SWT.ON_TOP);
		int style = getStyle();
		int listStyle = SWT.SINGLE | SWT.V_SCROLL;
		if ((style & SWT.FLAT) != 0)
			listStyle |= SWT.FLAT;
		if ((style & SWT.RIGHT_TO_LEFT) != 0)
			listStyle |= SWT.RIGHT_TO_LEFT;
		if ((style & SWT.LEFT_TO_RIGHT) != 0)
			listStyle |= SWT.LEFT_TO_RIGHT;
		list = new List(popup, listStyle);
		if (font != null)
			list.setFont(font);
		if (foreground != null)
			list.setForeground(foreground);
		if (background != null)
			list.setBackground(background);

		int[] popupEvents = { SWT.Close, SWT.Paint };
		for (int i = 0; i < popupEvents.length; i++)
			popup.addListener(popupEvents[i], listener);
		int[] listEvents = { SWT.MouseUp, SWT.Selection, SWT.Traverse,
				SWT.KeyDown, SWT.KeyUp, SWT.FocusIn, SWT.FocusOut, SWT.Dispose };
		for (int i = 0; i < listEvents.length; i++)
			list.addListener(listEvents[i], listener);

		if (items != null)
			list.setItems(items);
		if (selectionIndex != -1)
			list.setSelection(selectionIndex);
	}

	public void cut() {
		checkWidget();
		text.cut();
	}

	public void deselect(int index) {
		checkWidget();
		if (0 <= index && index < list.getItemCount()
				&& index == list.getSelectionIndex()
				&& text.getText().equals(list.getItem(index))) {
			text.setText(""); //$NON-NLS-1$
			list.deselect(index);
		}
	}

	public void deselectAll() {
		checkWidget();
		text.setText(""); //$NON-NLS-1$
		list.deselectAll();
	}

	void dropDown(boolean drop) {
		if (drop == isDropped())
			return;
		Display display = getDisplay();
		if (!drop) {
			display.removeFilter(SWT.Selection, filter);
			popup.setVisible(false);
			if (!isDisposed() && isFocusControl()) {
				text.setFocus();
			}
			return;
		}
		if (!isVisible())
			return;
		if (getShell() != popup.getParent()) {
			String[] items = list.getItems();
			int selectionIndex = list.getSelectionIndex();
			list.removeListener(SWT.Dispose, listener);
			popup.dispose();
			popup = null;
			list = null;
			createPopup(items, selectionIndex);
		}

		Point size = getSize();
		int itemCount = list.getItemCount();
		itemCount = (itemCount == 0) ? visibleItemCount : Math.min(
				visibleItemCount, itemCount);
		int itemHeight = list.getItemHeight() * itemCount;
		Point listSize = list.computeSize(SWT.DEFAULT, itemHeight, false);
		list.setBounds(1, 1, Math.max(size.x - 2, listSize.x), listSize.y);

		int index = list.getSelectionIndex();
		if (index != -1)
			list.setTopIndex(index);
		Rectangle listRect = list.getBounds();
		Rectangle parentRect = display.map(getParent(), null, getBounds());
		Point comboSize = getSize();
		Rectangle displayRect = getMonitor().getClientArea();
		int width = Math.max(comboSize.x, listRect.width + 2);
		int height = listRect.height + 2;
		int x = parentRect.x;
		int y = parentRect.y + comboSize.y;
		if (y + height > displayRect.y + displayRect.height)
			y = parentRect.y - height;
		if (x + width > displayRect.x + displayRect.width)
			x = displayRect.x + displayRect.width - listRect.width;
		popup.setBounds(x, y, width, height);
		popup.setVisible(true);
		if (isFocusControl())
			list.setFocus();

		/*
		 * Add a filter to listen to scrolling of the parent composite, when the
		 * drop-down is visible. Remove the filter when drop-down is not
		 * visible.
		 */
		display.removeFilter(SWT.Selection, filter);
		display.addFilter(SWT.Selection, filter);
	}

	/*
	 * Return the Label immediately preceding the receiver in the z-order, or
	 * null if none.
	 */
	String getAssociatedLabel() {
		Control[] siblings = getParent().getChildren();
		for (int i = 0; i < siblings.length; i++) {
			if (siblings[i] == this) {
				if (i > 0) {
					Control sibling = siblings[i - 1];
					if (sibling instanceof Label)
						return ((Label) sibling).getText();
					if (sibling instanceof CLabel)
						return ((CLabel) sibling).getText();
				}
				break;
			}
		}
		return null;
	}

	public Control[] getChildren() {
		checkWidget();
		return new Control[0];
	}

	public boolean getEditable() {
		checkWidget();
		return text.getEditable();
	}

	public String getItem(int index) {
		checkWidget();
		return list.getItem(index);
	}

	public int getItemCount() {
		checkWidget();
		return list.getItemCount();
	}

	public int getItemHeight() {
		checkWidget();
		return list.getItemHeight();
	}

	public String[] getItems() {
		checkWidget();
		return list.getItems();
	}

	public boolean getListVisible() {
		checkWidget();
		return isDropped();
	}

	public Menu getMenu() {
		return text.getMenu();
	}

	public Point getSelection() {
		checkWidget();
		return text.getSelection();
	}

	public int getSelectionIndex() {
		checkWidget();
		return list.getSelectionIndex();
	}

	public Shell getShell() {
		checkWidget();
		Shell shell = super.getShell();
		if (shell != this.parentShell) {
			if (this.parentShell != null && !parentShell.isDisposed()) {
				this.parentShell.removeListener(SWT.Deactivate, listener);
			}
			this.parentShell = shell;
		}
		return this.parentShell;
	}

	public int getStyle() {
		int style = super.getStyle();
		style &= ~SWT.READ_ONLY;
		if (!text.getEditable())
			style |= SWT.READ_ONLY;
		return style;
	}

	public String getText() {
		checkWidget();
		return text.getText();
	}

	public int getTextHeight() {
		checkWidget();
		return text.getLineHeight();
	}

	public int getTextLimit() {
		checkWidget();
		return text.getTextLimit();
	}

	public int getVisibleItemCount() {
		checkWidget();
		return visibleItemCount;
	}

	void handleFocus(int type) {
		switch (type) {
		case SWT.FocusIn: {
			if (hasFocus)
				return;
			if (getEditable()) {
				text.setSelection(text.getText().length());
			}
			hasFocus = true;
			Shell shell = getShell();
			shell.removeListener(SWT.Deactivate, listener);
			shell.addListener(SWT.Deactivate, listener);
			Display display = getDisplay();
			display.removeFilter(SWT.FocusIn, filter);
			display.addFilter(SWT.FocusIn, filter);
			Event e = new Event();
			notifyListeners(SWT.FocusIn, e);
			break;
		}
		case SWT.FocusOut: {
			if (!hasFocus)
				return;
			Control focusControl = getDisplay().getFocusControl();
			if (focusControl == arrow || focusControl == list
					|| focusControl == text)
				return;
			hasFocus = false;
			Shell shell = getShell();
			shell.removeListener(SWT.Deactivate, listener);
			Display display = getDisplay();
			display.removeFilter(SWT.FocusIn, filter);
			Event e = new Event();
			notifyListeners(SWT.FocusOut, e);
			break;
		}
		}
	}

	void handleScroll(Event event) {
		ScrollBar scrollBar = (ScrollBar) event.widget;
		Control scrollableParent = scrollBar.getParent();
		if (scrollableParent.equals(list))
			return;
		if (isParentScrolling(scrollableParent))
			dropDown(false);
	}

	public int indexOf(String string) {
		checkWidget();
		if (string == null)
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		return list.indexOf(string);
	}

	public int indexOf(String string, int start) {
		checkWidget();
		if (string == null)
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		return list.indexOf(string, start);
	}

	void initAccessible() {
		AccessibleAdapter accessibleAdapter = new AccessibleAdapter() {
			public void getHelp(AccessibleEvent e) {
				e.result = getToolTipText();
			}

			public void getKeyboardShortcut(AccessibleEvent e) {
				String shortcut = null;
				String text = getAssociatedLabel();
				if (text != null) {
					char mnemonic = _findMnemonic(text);
					if (mnemonic != '\0') {
						shortcut = "Alt+" + mnemonic; //$NON-NLS-1$
					}
				}
				e.result = shortcut;
			}

			public void getName(AccessibleEvent e) {
				String name = null;
				String text = getAssociatedLabel();
				if (text != null) {
					name = stripMnemonic(text);
				}
				e.result = name;
			}
		};
		getAccessible().addAccessibleListener(accessibleAdapter);
		text.getAccessible().addAccessibleListener(accessibleAdapter);
		list.getAccessible().addAccessibleListener(accessibleAdapter);

		arrow.getAccessible().addAccessibleListener(new AccessibleAdapter() {
			public void getHelp(AccessibleEvent e) {
				e.result = getToolTipText();
			}

			public void getKeyboardShortcut(AccessibleEvent e) {
				e.result = "Alt+Down Arrow"; //$NON-NLS-1$
			}

			public void getName(AccessibleEvent e) {
				e.result = isDropped() ? SWT.getMessage("SWT_Close") : SWT.getMessage("SWT_Open"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		});

		getAccessible().addAccessibleTextListener(new AccessibleTextAdapter() {
			public void getCaretOffset(AccessibleTextEvent e) {
				e.offset = text.getCaretPosition();
			}

			public void getSelectionRange(AccessibleTextEvent e) {
				Point sel = text.getSelection();
				e.offset = sel.x;
				e.length = sel.y - sel.x;
			}
		});

		getAccessible().addAccessibleControlListener(
				new AccessibleControlAdapter() {
					public void getChildAtPoint(AccessibleControlEvent e) {
						Point testPoint = toControl(e.x, e.y);
						if (getBounds().contains(testPoint)) {
							e.childID = ACC.CHILDID_SELF;
						}
					}

					public void getChildCount(AccessibleControlEvent e) {
						e.detail = 0;
					}

					public void getLocation(AccessibleControlEvent e) {
						Rectangle location = getBounds();
						Point pt = getParent()
								.toDisplay(location.x, location.y);
						e.x = pt.x;
						e.y = pt.y;
						e.width = location.width;
						e.height = location.height;
					}

					public void getRole(AccessibleControlEvent e) {
						e.detail = ACC.ROLE_COMBOBOX;
					}

					public void getState(AccessibleControlEvent e) {
						e.detail = ACC.STATE_NORMAL;
					}

					public void getValue(AccessibleControlEvent e) {
						e.result = getText();
					}
				});

		text.getAccessible().addAccessibleControlListener(
				new AccessibleControlAdapter() {
					public void getRole(AccessibleControlEvent e) {
						e.detail = text.getEditable() ? ACC.ROLE_TEXT
								: ACC.ROLE_LABEL;
					}
				});

		arrow.getAccessible().addAccessibleControlListener(
				new AccessibleControlAdapter() {
					public void getDefaultAction(AccessibleControlEvent e) {
						e.result = isDropped() ? SWT.getMessage("SWT_Close") : SWT.getMessage("SWT_Open"); //$NON-NLS-1$ //$NON-NLS-2$
					}
				});
	}

	void internalLayout(boolean changed) {
		if (isDropped())
			dropDown(false);
		Rectangle rect = getClientArea();
		int width = rect.width;
		int height = rect.height;
		Point arrowSize = arrow.computeSize(SWT.DEFAULT, height, changed);
		text.setBounds(0, 0, width - arrowSize.x, height);
		arrow.setBounds(width - arrowSize.x, 0, arrowSize.x, arrowSize.y);
	}

	boolean isDropped() {
		return popup.getVisible();
	}

	public boolean isFocusControl() {
		checkWidget();
		if (text.isFocusControl() || arrow.isFocusControl()
				|| list.isFocusControl() || popup.isFocusControl()) {
			return true;
		}
		return super.isFocusControl();
	}

	boolean isParentScrolling(Control scrollableParent) {
		Control parent = this.getParent();
		while (parent != null) {
			if (parent.equals(scrollableParent))
				return true;
			parent = parent.getParent();
		}
		return false;
	}

	void listEvent(Event event) {
		switch (event.type) {
		case SWT.Dispose:
			if (getShell() != popup.getParent()) {
				String[] items = list.getItems();
				int selectionIndex = list.getSelectionIndex();
				popup = null;
				list = null;
				createPopup(items, selectionIndex);
			}
			break;
		case SWT.FocusIn: {
			handleFocus(SWT.FocusIn);
			break;
		}
		case SWT.FocusOut: {
			break;
		}
		case SWT.MouseUp: {
			if (event.button != 1)
				return;
			dropDown(false);
			break;
		}
		case SWT.Selection: {
			int index = list.getSelectionIndex();
			if (index == -1)
				return;
			text.setText(list.getItem(index));
			text.setSelection(text.getText().length());
//			text.selectAll();
			list.setSelection(index);
			Event e = new Event();
			e.time = event.time;
			e.stateMask = event.stateMask;
			e.doit = event.doit;
			notifyListeners(SWT.Selection, e);
			event.doit = e.doit;
			break;
		}
		case SWT.Traverse: {
			switch (event.detail) {
			case SWT.TRAVERSE_RETURN:
			case SWT.TRAVERSE_ESCAPE:
			case SWT.TRAVERSE_ARROW_PREVIOUS:
			case SWT.TRAVERSE_ARROW_NEXT:
				event.doit = false;
				break;
			case SWT.TRAVERSE_TAB_NEXT:
			case SWT.TRAVERSE_TAB_PREVIOUS:
				event.doit = text.traverse(event.detail);
				event.detail = SWT.TRAVERSE_NONE;
				if (event.doit)
					dropDown(false);
				return;
			}
			Event e = new Event();
			e.time = event.time;
			e.detail = event.detail;
			e.doit = event.doit;
			e.character = event.character;
			e.keyCode = event.keyCode;
			e.keyLocation = event.keyLocation;
			notifyListeners(SWT.Traverse, e);
			event.doit = e.doit;
			event.detail = e.detail;
			break;
		}
		case SWT.KeyUp: {
			Event e = new Event();
			e.time = event.time;
			e.character = event.character;
			e.keyCode = event.keyCode;
			e.keyLocation = event.keyLocation;
			e.stateMask = event.stateMask;
			notifyListeners(SWT.KeyUp, e);
			event.doit = e.doit;
			break;
		}
		case SWT.KeyDown: {
			if (event.character == SWT.ESC) {
				// Escape key cancels popup list
				dropDown(false);
			}
			if ((event.stateMask & SWT.ALT) != 0
					&& (event.keyCode == SWT.ARROW_UP || event.keyCode == SWT.ARROW_DOWN)) {
				dropDown(false);
			}
			if (event.character == SWT.CR) {
				// Enter causes default selection
				dropDown(false);
//				Event e = new Event();
//				e.time = event.time;
//				e.stateMask = event.stateMask;
//				notifyListeners(SWT.DefaultSelection, e);
			}
			// At this point the widget may have been disposed.
			// If so, do not continue.
			if (isDisposed())
				break;
			Event e = new Event();
			e.time = event.time;
			e.character = event.character;
			e.keyCode = event.keyCode;
			e.keyLocation = event.keyLocation;
			e.stateMask = event.stateMask;
			notifyListeners(SWT.KeyDown, e);
			event.doit = e.doit;
			break;
		}
		}
	}

	public void paste() {
		checkWidget();
		text.paste();
	}

	void popupEvent(Event event) {
		switch (event.type) {
		case SWT.Paint:
			// draw black rectangle around list
			Rectangle listRect = list.getBounds();
			Color black = getDisplay().getSystemColor(SWT.COLOR_BLACK);
			event.gc.setForeground(black);
			event.gc.drawRectangle(0, 0, listRect.width + 1,
					listRect.height + 1);
			break;
		case SWT.Close:
			event.doit = false;
			dropDown(false);
			break;
		}
	}

	public void redraw() {
		super.redraw();
		text.redraw();
		arrow.redraw();
		if (popup.isVisible())
			list.redraw();
	}

	public void redraw(int x, int y, int width, int height, boolean all) {
		super.redraw(x, y, width, height, true);
	}

	public void remove(int index) {
		checkWidget();
		list.remove(index);
	}

	public void remove(int start, int end) {
		checkWidget();
		list.remove(start, end);
	}

	public void remove(String string) {
		checkWidget();
		if (string == null)
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		list.remove(string);
	}

	public void removeAll() {
		checkWidget();
		text.setText(""); //$NON-NLS-1$
		list.removeAll();
	}

	public void removeModifyListener(ModifyListener listener) {
		checkWidget();
		if (listener == null)
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		removeListener(SWT.Modify, listener);
	}

	public void removeSelectionListener(SelectionListener listener) {
		checkWidget();
		if (listener == null)
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		removeListener(SWT.Selection, listener);
		removeListener(SWT.DefaultSelection, listener);
	}

	public void removeVerifyListener(VerifyListener listener) {
		checkWidget();
		if (listener == null)
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		removeListener(SWT.Verify, listener);
	}

	public void select(int index) {
		checkWidget();
		if (index == -1) {
			list.deselectAll();
			text.setText(""); //$NON-NLS-1$
			return;
		}
		if (0 <= index && index < list.getItemCount()) {
			if (index != getSelectionIndex()) {
				text.setText(list.getItem(index));
				text.selectAll();
				list.select(index);
				list.showSelection();
			}
		}
	}

	public void setBackground(Color color) {
		super.setBackground(color);
		background = color;
		if (text != null)
			text.setBackground(color);
		if (list != null)
			list.setBackground(color);
		if (arrow != null)
			arrow.setBackground(color);
	}

	public void setEditable(boolean editable) {
		checkWidget();
		text.setEditable(editable);
	}

	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		if (popup != null)
			popup.setVisible(false);
		if (text != null)
			text.setEnabled(enabled);
		if (arrow != null)
			arrow.setEnabled(enabled);
	}

	public boolean setFocus() {
		checkWidget();
		if (!isEnabled() || !getVisible())
			return false;
		if (isFocusControl())
			return true;
		return text.setFocus();
	}

	public void setFont(Font font) {
		super.setFont(font);
		this.font = font;
		text.setFont(font);
		list.setFont(font);
		internalLayout(true);
	}

	public void setForeground(Color color) {
		super.setForeground(color);
		foreground = color;
		if (text != null)
			text.setForeground(color);
		if (list != null)
			list.setForeground(color);
		if (arrow != null)
			arrow.setForeground(color);
	}

	public void setItem(int index, String string) {
		checkWidget();
		list.setItem(index, string);
	}

	public void setItems(String[] items) {
		checkWidget();
		list.setItems(items);
		if (!text.getEditable())
			text.setText(""); //$NON-NLS-1$
	}

	public void setLayout(Layout layout) {
		checkWidget();
		return;
	}

	public void setListVisible(boolean visible) {
		checkWidget();
		dropDown(visible);
	}

	public void setMenu(Menu menu) {
		text.setMenu(menu);
	}

	public void setSelection(Point selection) {
		checkWidget();
		if (selection == null) {
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
			return;
		}
		text.setSelection(selection.x, selection.y);
	}

	public void setText(String string) {
		checkWidget();
		if (string == null)
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		int index = list.indexOf(string);
		if (index == -1) {
			list.deselectAll();
			text.setText(string);
			return;
		}
		text.setText(string);
		text.selectAll();
		list.setSelection(index);
		list.showSelection();
	}

	public void setTextLimit(int limit) {
		checkWidget();
		text.setTextLimit(limit);
	}

	public void setToolTipText(String string) {
		checkWidget();
		super.setToolTipText(string);
		arrow.setToolTipText(string);
		text.setToolTipText(string);
	}

	public void setVisible(boolean visible) {
		super.setVisible(visible);
		/*
		 * At this point the widget may have been disposed in a FocusOut event.
		 * If so then do not continue.
		 */
		if (isDisposed())
			return;
		// TEMPORARY CODE
		if (popup == null || popup.isDisposed())
			return;
		if (!visible)
			popup.setVisible(false);
	}

	public void setVisibleItemCount(int count) {
		checkWidget();
		if (count < 0)
			return;
		visibleItemCount = count;
	}

	String stripMnemonic(String string) {
		int index = 0;
		int length = string.length();
		do {
			while ((index < length) && (string.charAt(index) != '&'))
				index++;
			if (++index >= length)
				return string;
			if (string.charAt(index) != '&') {
				return string.substring(0, index - 1)
						+ string.substring(index, length);
			}
			index++;
		} while (index < length);
		return string;
	}

	void textEvent(Event event) {
		switch (event.type) {
		case SWT.FocusIn: {
			handleFocus(SWT.FocusIn);
			break;
		}
		case SWT.DefaultSelection: {
			dropDown(false);
			Event e = new Event();
			e.time = event.time;
			e.stateMask = event.stateMask;
			notifyListeners(SWT.DefaultSelection, e);
			break;
		}
		case SWT.DragDetect:
		case SWT.MouseDoubleClick:
		case SWT.MouseMove:
		case SWT.MouseEnter:
		case SWT.MouseExit:
		case SWT.MouseHover: {
			Point pt = getDisplay().map(text, this, event.x, event.y);
			event.x = pt.x;
			event.y = pt.y;
			notifyListeners(event.type, event);
			event.type = SWT.None;
			break;
		}
		case SWT.KeyDown: {
			Event keyEvent = new Event();
			keyEvent.time = event.time;
			keyEvent.character = event.character;
			keyEvent.keyCode = event.keyCode;
			keyEvent.keyLocation = event.keyLocation;
			keyEvent.stateMask = event.stateMask;
			notifyListeners(SWT.KeyDown, keyEvent);
			if (isDisposed())
				break;
			event.doit = keyEvent.doit;
			if (!event.doit)
				break;
			if (event.keyCode == SWT.ARROW_UP
					|| event.keyCode == SWT.ARROW_DOWN) {
				event.doit = false;
				if ((event.stateMask & SWT.ALT) != 0) {
					boolean dropped = isDropped();
					text.selectAll();
					if (!dropped)
						setFocus();
					dropDown(!dropped);
					break;
				}

				int oldIndex = getSelectionIndex();
				if (event.keyCode == SWT.ARROW_UP) {
					select(Math.max(oldIndex - 1, 0));
				} else {
					select(Math.min(oldIndex + 1, getItemCount() - 1));
				}
				if (oldIndex != getSelectionIndex()) {
					Event e = new Event();
					e.time = event.time;
					e.stateMask = event.stateMask;
					notifyListeners(SWT.Selection, e);
				}
				if (isDisposed())
					break;
			}

			// Further work : Need to add support for incremental search in
			// pop up list as characters typed in text widget
			break;
		}
		case SWT.KeyUp: {
			Event e = new Event();
			e.time = event.time;
			e.character = event.character;
			e.keyCode = event.keyCode;
			e.keyLocation = event.keyLocation;
			e.stateMask = event.stateMask;
			notifyListeners(SWT.KeyUp, e);
			event.doit = e.doit;
			break;
		}
		case SWT.MenuDetect: {
			Event e = new Event();
			e.time = event.time;
			notifyListeners(SWT.MenuDetect, e);
			break;
		}
		case SWT.Modify: {
			list.deselectAll();
			Event e = new Event();
			e.time = event.time;
			notifyListeners(SWT.Modify, e);
			break;
		}
		case SWT.MouseDown: {
			Point pt = getDisplay().map(text, this, event.x, event.y);
			Event mouseEvent = new Event();
			mouseEvent.button = event.button;
			mouseEvent.count = event.count;
			mouseEvent.stateMask = event.stateMask;
			mouseEvent.time = event.time;
			mouseEvent.x = pt.x;
			mouseEvent.y = pt.y;
			notifyListeners(SWT.MouseDown, mouseEvent);
			if (isDisposed())
				break;
			event.doit = mouseEvent.doit;
			if (!event.doit)
				break;
			if (event.button != 1)
				return;
			if (text.getEditable())
				return;
			boolean dropped = isDropped();
			text.selectAll();
			if (!dropped)
				setFocus();
			dropDown(!dropped);
			break;
		}
		case SWT.MouseUp: {
			Point pt = getDisplay().map(text, this, event.x, event.y);
			Event mouseEvent = new Event();
			mouseEvent.button = event.button;
			mouseEvent.count = event.count;
			mouseEvent.stateMask = event.stateMask;
			mouseEvent.time = event.time;
			mouseEvent.x = pt.x;
			mouseEvent.y = pt.y;
			notifyListeners(SWT.MouseUp, mouseEvent);
			if (isDisposed())
				break;
			event.doit = mouseEvent.doit;
			if (!event.doit)
				break;
			if (event.button != 1)
				return;
			if (text.getEditable())
				return;
			text.selectAll();
			break;
		}
		case SWT.MouseWheel: {
			notifyListeners(SWT.MouseWheel, event);
			event.type = SWT.None;
			if (isDisposed())
				break;
			if (!event.doit)
				break;
			if (event.count != 0) {
				event.doit = false;
				int oldIndex = getSelectionIndex();
				if (event.count > 0) {
					select(Math.max(oldIndex - 1, 0));
				} else {
					select(Math.min(oldIndex + 1, getItemCount() - 1));
				}
				if (oldIndex != getSelectionIndex()) {
					Event e = new Event();
					e.time = event.time;
					e.stateMask = event.stateMask;
					notifyListeners(SWT.Selection, e);
				}
				if (isDisposed())
					break;
			}
			break;
		}
		case SWT.Traverse: {
			switch (event.detail) {
			case SWT.TRAVERSE_ARROW_PREVIOUS:
			case SWT.TRAVERSE_ARROW_NEXT:
				// The enter causes default selection and
				// the arrow keys are used to manipulate the list contents so
				// do not use them for traversal.
				event.doit = false;
				break;
			case SWT.TRAVERSE_TAB_PREVIOUS:
				event.doit = traverse(SWT.TRAVERSE_TAB_PREVIOUS);
				event.detail = SWT.TRAVERSE_NONE;
				return;
			}
			Event e = new Event();
			e.time = event.time;
			e.detail = event.detail;
			e.doit = event.doit;
			e.character = event.character;
			e.keyCode = event.keyCode;
			e.keyLocation = event.keyLocation;
			notifyListeners(SWT.Traverse, e);
			event.doit = e.doit;
			event.detail = e.detail;
			break;
		}
		case SWT.Verify: {
			Event e = new Event();
			e.text = event.text;
			e.start = event.start;
			e.end = event.end;
			e.character = event.character;
			e.keyCode = event.keyCode;
			e.keyLocation = event.keyLocation;
			e.stateMask = event.stateMask;
			notifyListeners(SWT.Verify, e);
			event.text = e.text;
			event.doit = e.doit;
			break;
		}
		}
	}

	public boolean traverse(int event) {
		if (event == SWT.TRAVERSE_ARROW_NEXT || event == SWT.TRAVERSE_TAB_NEXT) {
			return text.traverse(event);
		}
		return super.traverse(event);
	}
}
