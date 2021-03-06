/*
 * Hex - a hex viewer and annotator
 * Copyright (C) 2009-2014,2016-2017,2021  Hakanai, Hex Project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.trypticon.hex.plaf;

import org.trypticon.hex.HexViewer;
import org.trypticon.hex.binary.Binary;
import org.trypticon.hex.renderer.CellRenderer;
import org.trypticon.hex.util.DerivedColor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JScrollBar;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.plaf.BorderUIResource;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.UIResource;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.SystemColor;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.geom.Line2D;
import java.util.function.Supplier;

/**
 * Basic user interface for the hex viewer.
 *
 * @author trejkaz
 */
public class BasicHexViewerUI extends HexViewerUI {
    @Nullable
    MouseAdapter mouseAdapter;

    @Nullable
    FocusListener focusAdapter;

    /**
     * Factory method called by Swing to construct the instance.
     *
     * @param component the component the UI is being created for.
     * @return the UI.
     */
    @SuppressWarnings("unused") // Called reflectively.
    public static ComponentUI createUI(JComponent component) {
        return new BasicHexViewerUI();
    }

    private int computeCharWidth(HexViewer viewer) {
        return viewer.getFontMetrics(viewer.getFont()).charWidth('D');
    }

    @Override
    public Rectangle modelToView(HexViewer viewer, long pos) {
        int bytesPerRow = viewer.getBytesPerRow();
        int charWidth = computeCharWidth(viewer);
        int rowHeight = viewer.getRowHeight();

        long bytesY = (pos / bytesPerRow);
        int bytesX = (int) (pos - bytesPerRow * bytesY);

        int xFixed = bytesX * (3 * charWidth);
        long yFixed = bytesY * rowHeight;

        // Now adjust for the margins again...

        return new Rectangle(xFixed + 3 + viewer.getOffsetColumnDigits() + 1 + charWidth,
                             (int) (yFixed - viewer.getFirstVisibleRow()),
                             3 * charWidth,
                             rowHeight);
    }

    @Override
    public long viewToModel(HexViewer viewer, Point point) {
        int charWidth = computeCharWidth(viewer);
        int rowHeight = viewer.getRowHeight();

        Binary binary = viewer.getBinary();
        if (binary == null) {
            return 0L;
        }

        long binaryLength = binary.length();
        int bytesPerRow = viewer.getBytesPerRow();
        int maxBytesX = bytesPerRow - 1;
        long maxBytesY = binaryLength / bytesPerRow;

        // Threshold for detecting that the user clicked in the ASCII column is half way between the two columns.
        int hexLeftX = (3 + viewer.getOffsetColumnDigits() + 1 + 1) * charWidth;
        int hexRightX = hexLeftX + 3 * charWidth * bytesPerRow;
        int asciiLeftX = hexRightX + 2 * charWidth;

        int leftX;
        int cellWidth;

        if (point.x < (hexRightX + asciiLeftX) / 2) {
            // Clicked on the hex side.
            leftX = hexLeftX;
            cellWidth = 3 * charWidth;
        } else {
            // Clicked on the ASCII side.
            leftX = asciiLeftX;
            cellWidth = charWidth;
        }

        int xFixed = point.x - leftX;
        long yFixed = point.y;

        int bytesX = xFixed / cellWidth;
        long bytesY = (yFixed / rowHeight) + viewer.getFirstVisibleRow();

        bytesX = Math.max(0, Math.min(maxBytesX, bytesX));
        bytesY = Math.max(0, Math.min(maxBytesY, bytesY));

        long pos = bytesY * bytesPerRow + bytesX;
        assert pos >= 0;
        if (pos >= binaryLength) {
            pos = binaryLength - 1;
        }

        return pos;
    }

    @Override
    public Dimension getPreferredSize(JComponent c) {
        HexViewer viewer = (HexViewer) c;

        // Size of just the data the viewport.
        Dimension size = getPreferredViewportWidth(viewer, viewer.getPreferredVisibleRowCount());

        // Plus the insets of the viewport.
        Insets viewportInsets = getViewportBorderInsets(viewer);
        if (viewportInsets != null) {
            size.width += (viewportInsets.left + viewportInsets.right);
            size.height += (viewportInsets.top + viewportInsets.bottom);
        }

        // Plus the vertical scroll bar on the right. The horizontal scroll bar won't
        // contribute size because at our preferred size, it doesn't appear.
        JScrollBar scrollBar = getVerticalScrollBar(c);
        if (scrollBar != null) {
            size.width += scrollBar.getPreferredSize().width;
        }

        // Plus the insets of the component.
        Insets insets = c.getInsets();
        size.width += (insets.left + insets.right);
        size.height += (insets.top + insets.bottom);

        return size;
    }

    private Dimension getPreferredViewportWidth(HexViewer viewer, int numRows) {
        FontMetrics metrics = viewer.getFontMetrics(viewer.getFont());
        int bytesPerRow = viewer.getBytesPerRow();
        int offsetColumnDigits = viewer.getOffsetColumnDigits();

        int width = metrics.charWidth('D') *
                (3 + offsetColumnDigits + 1 + 1 + (bytesPerRow * 3) + 2 + bytesPerRow + 3);

        int height = metrics.getHeight() * (numRows + 2);

        return new Dimension(width, height);
    }

    @Nullable
    private Insets getViewportBorderInsets(HexViewer viewer) {
        Border border = viewer.getViewportBorder();
        if (border != null) {
            return border.getBorderInsets(viewer);
        } else {
            return null;
        }
    }

    @Nullable
    private JScrollBar getVerticalScrollBar(JComponent c) {
        for (Component component : c.getComponents()) {
            if (component instanceof JScrollBar &&
                    ((JScrollBar) component).getOrientation() == JScrollBar.VERTICAL) {
                return (JScrollBar) component;
            }
        }
        return null;
    }

    @Nullable
    private JScrollBar getHorizontalScrollBar(JComponent c) {
        for (Component component : c.getComponents()) {
            if (component instanceof JScrollBar &&
                    ((JScrollBar) component).getOrientation() == JScrollBar.HORIZONTAL) {
                return (JScrollBar) component;
            }
        }
        return null;
    }

    /**
     * Paints the component.  If the component is opaque, paints the background colour.  The rest of the painting is
     * delegated to {@link #paintViewportBorder(java.awt.Graphics2D, org.trypticon.hex.HexViewer)} and
     * {@link #paintHex(java.awt.Graphics2D, org.trypticon.hex.HexViewer)}.
     *
     * @param g the graphics context.
     * @param c the component.
     */
    @Override
    public void paint(Graphics g, JComponent c) {
        if (c.isOpaque()) {
            g.setColor(c.getBackground());
            Rectangle clip = g.getClipBounds();
            g.fillRect(0, 0, clip.width, clip.height);
        }

        HexViewer viewer = (HexViewer) c;

        // Taking a copy because we want to clip the borders out.
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            Rectangle viewportContent = new Rectangle(0, 0, c.getWidth(), c.getHeight());

            Insets viewportInsets = getViewportBorderInsets(viewer);
            if (viewportInsets != null) {
                viewportContent.x += viewportInsets.left;
                viewportContent.y += viewportInsets.top;
                viewportContent.width -= (viewportInsets.left + viewportInsets.right);
                viewportContent.height -= (viewportInsets.top + viewportInsets.bottom);
            }

            Insets insets = viewer.getInsets();
            if (insets != null) {
                viewportContent.x += insets.left;
                viewportContent.y += insets.top;
                viewportContent.width -= (insets.left + insets.right);
                viewportContent.height -= (insets.top + insets.bottom);
            }

            JScrollBar verticalScrollBar = getVerticalScrollBar(viewer);
            if (verticalScrollBar != null) {
                viewportContent.width -= verticalScrollBar.getWidth();
            }

            JScrollBar horizontalScrollBar = getHorizontalScrollBar(viewer);
            if (horizontalScrollBar != null) {
                viewportContent.height -= horizontalScrollBar.getHeight();
            }

            g2.clipRect(viewportContent.x, viewportContent.y, viewportContent.width, viewportContent.height);

            g2.translate(-viewer.getHorizontalOffset(), 0);

            paintHex(g2, viewer);
        } finally {
            g2.dispose();
        }

        paintViewportBorder((Graphics2D) g, viewer);
    }

    /**
     * Paints the border around the viewport.
     *
     * @param g the graphics context.
     * @param viewer the viewer.
     */
    protected void paintViewportBorder(Graphics2D g, HexViewer viewer) {
        JScrollBar scrollBar = getVerticalScrollBar(viewer);
        if (scrollBar == null) {
            return;
        }
        Border border = viewer.getViewportBorder();
        if (border != null) {
            border.paintBorder(viewer, g, 0, 0, viewer.getWidth() - scrollBar.getWidth(), viewer.getHeight());
        }
    }

    /**
     * Paints the hex display.
     *
     * @param g the graphics context.
     * @param viewer the viewer.
     */
    protected void paintHex(Graphics2D g, HexViewer viewer) {

        Binary binary = viewer.getBinary();
        if (binary == null) {
            return;
        }

        g.setFont(viewer.getFont());
        Rectangle clipBounds = g.getClipBounds();

        if (viewer.isOpaque()) {
            g.setColor(viewer.getBackground());
            g.fillRect(clipBounds.x, clipBounds.y, clipBounds.width, clipBounds.height);
        }

        int bytesPerRow = viewer.getBytesPerRow();

        // Width computations
        int charWidth = computeCharWidth(viewer);
        int hexColWidth = charWidth * 3;
        int addressLineX = (3 + viewer.getOffsetColumnDigits() + 1) * charWidth;
        int firstDataColumnX = addressLineX + charWidth;
        int firstAsciiColumnX = firstDataColumnX +
                (bytesPerRow * hexColWidth) + 2 * charWidth;

        // Height computations
        int rowHeight = viewer.getRowHeight();
        long firstVisibleRow = viewer.getFirstVisibleRow();
        int visibleRowCount = viewer.getVisibleRowCount();
        long lastModelRow = viewer.getRowCount() - 1;

        int y = 0;
        long position = firstVisibleRow * bytesPerRow;

        CellRenderer renderer = viewer.getCellRenderer();
        long cursor = viewer.getSelectionModel().getCursor();
        long selectionStart = viewer.getSelectionModel().getSelectionStart();
        long selectionEnd = viewer.getSelectionModel().getSelectionEnd();

        long cursorRow = cursor / bytesPerRow;

        for (int viewRow = 0; viewRow < visibleRowCount; viewRow++) {
            long modelRow = firstVisibleRow + viewRow;

            // Skipping rows with no binary on them.
            if (modelRow >= 0 && modelRow <= lastModelRow) {
                // Background highlight for the row the cursor is on.
                if (modelRow == cursorRow) {
                    g.setColor(viewer.getCursorRowBackground());
                    g.fillRect(0, viewRow * rowHeight, viewer.getWidth(), rowHeight);
                }

                int rowDataLength = (int) Math.min(bytesPerRow, binary.length() - position);

                paintRow(viewer, g, position, rowDataLength, selectionStart, selectionEnd, modelRow == cursorRow, cursor,
                         hexColWidth, charWidth, rowHeight, y,
                         addressLineX, firstDataColumnX, firstAsciiColumnX,
                         renderer);
            }

            position += bytesPerRow;
            y += rowHeight;
        }

        // Address divider line.
        g.setColor(viewer.getOffsetForeground());
        g.setStroke(new BasicStroke(1.0f));
        g.draw(new Line2D.Float(addressLineX, 0,
                                addressLineX, rowHeight * (visibleRowCount + 1)));
    }

    // Painting a row is split out to give IDEA a bit of a help with the inspection.

    private void paintRow(HexViewer viewer, Graphics2D g, long position, int rowDataLength,
                          long selectionStart, long selectionEnd, boolean onCursorRow, long cursor,
                          int hexColWidth, int asciiColWidth, int rowHeight, int y,
                          int addressLineX, int firstDataColumnX, int firstAsciiColumnX,
                          CellRenderer renderer) {

        Component comp;

        Graphics g2 = g.create();
        try {
            // Row offset
            comp = renderer.getRendererComponent(viewer, false, onCursorRow, false,
                                                 position, CellRenderer.ROW_OFFSET);
            comp.setBounds(asciiColWidth, y, addressLineX - asciiColWidth*2, rowHeight);
            g2.translate(asciiColWidth, y);
            comp.paint(g2);
            g2.translate(-asciiColWidth, -y);

            // Hex digits for this row
            int hexX = firstDataColumnX;
            int asciiX = firstAsciiColumnX;
            for (int i = 0; i < rowDataLength; i++) {

                boolean insideSelection = selectionStart <= position && selectionEnd >= position;
                boolean atCursor = position == cursor;

                // Hex column
                comp = renderer.getRendererComponent(viewer, insideSelection, onCursorRow, atCursor,
                                                     position, CellRenderer.HEX);
                comp.setBounds(hexX, y, hexColWidth, rowHeight);
                g2.translate(hexX, y);
                comp.paint(g2);
                g2.translate(-hexX, -y);

                // ASCII column
                comp = renderer.getRendererComponent(viewer, insideSelection, onCursorRow, atCursor,
                                                     position, CellRenderer.ASCII);
                comp.setBounds(asciiX, y, asciiColWidth, rowHeight);
                g2.translate(asciiX, y);
                comp.paint(g2);
                g2.translate(-asciiX, -y);

                position++;
                hexX += hexColWidth;
                asciiX += asciiColWidth;
            }
        } finally {
            g2.dispose();
        }
    }

    @Override
    public void installUI(JComponent c) {
        installDefaults((HexViewer) c);
        installKeyboardActions((HexViewer) c);
        installListeners((HexViewer) c);
    }

    @Override
    public void uninstallUI(JComponent c) {
        uninstallListeners((HexViewer) c);
    }

    protected void installDefaults(HexViewer viewer) {
        Color background = viewer.getBackground();
        if (background == null || background instanceof ColorUIResource) {
            viewer.setBackground(getColour("background", SystemColor.text));
        }

        Color foreground = viewer.getForeground();
        if (foreground == null || background instanceof ColorUIResource) {
            viewer.setForeground(getColour("foreground", SystemColor.textText));
        }

        Color selectionBackground = viewer.getSelectionBackground();
        if (selectionBackground == null || selectionBackground instanceof ColorUIResource) {
            selectionBackground = getColour("selectionBackground", SystemColor.textHighlight);
            viewer.setSelectionBackground(selectionBackground);
        }

        Color selectionForeground = viewer.getSelectionForeground();
        if (selectionForeground == null || selectionForeground instanceof ColorUIResource) {
            viewer.setSelectionForeground(getColour("selectionForeground", SystemColor.textHighlightText));
        }

        Color cursorBackground = viewer.getCursorBackground();
        if (cursorBackground == null || cursorBackground instanceof ColorUIResource) {
            Color colour = UIManager.getColor("HexViewer.cursorBackground");
            if (colour == null) {
                colour = new DerivedColor(selectionBackground, 1.25f, -0.125f, 1.0f);
            }
            viewer.setCursorBackground(colour);
        }

        Color cursorForeground = viewer.getCursorForeground();
        if (cursorForeground == null || cursorForeground instanceof ColorUIResource) {
            Color colour = UIManager.getColor("HexViewer.cursorForeground");
            if (colour == null) {
                colour = selectionForeground;
            }
            viewer.setCursorForeground(colour);
        }

        Color cursorRowBackground = viewer.getCursorRowBackground();
        if (cursorRowBackground == null || cursorRowBackground instanceof ColorUIResource) {
            Color colour = UIManager.getColor("HexViewer.cursorRowBackground");
            if (colour == null) {
                colour = new DerivedColor(selectionBackground, 1.0f, 0.0f, 0.25f);
            }
            viewer.setCursorRowBackground(colour);
        }

        Color offsetForeground = viewer.getOffsetForeground();
        if (offsetForeground == null || offsetForeground instanceof ColorUIResource) {
            Color colour = UIManager.getColor("HexViewer.offsetForeground");
            if (colour == null) {
                colour = new DerivedColor(viewer.getForeground(), 1.0f, 0.0f, 0.6f);
            }
            viewer.setOffsetForeground(colour);
        }

        Font font = viewer.getFont();
        if (font == null || font instanceof FontUIResource) {
            font = UIManager.getFont("HexViewer.font");
            if (font == null) {
                font = UIManager.getFont("TextArea.font");
            }
            viewer.setFont(font);
        }

        LookAndFeel.installBorder(viewer, "ScrollPane.border");

        Border viewportBorder = viewer.getViewportBorder();
        if (viewportBorder == null || viewportBorder instanceof UIResource) {
            Border border = UIManager.getBorder("ScrollPane.viewportBorder");
            if (border == null) {
                border = new BorderUIResource(BorderFactory.createEmptyBorder());
            }
            viewer.setViewportBorder(border);
        }
    }

    @Nonnull
    private Color getColour(String suffix, Color lastResort) {
        Color colour = UIManager.getColor("HexViewer." + suffix);
        if (colour == null) {
            colour = UIManager.getColor("TextArea." + suffix);
        }
        if (colour == null) {
            colour = lastResort;
        }
        return colour;
    }

    protected void installKeyboardActions(HexViewer viewer) {
        SwingUtilities.replaceUIInputMap(viewer, JComponent.WHEN_FOCUSED,
                                         (InputMap) UIManager.get("HexViewer.focusInputMap"));

        SwingUtilities.replaceUIActionMap(viewer, createActionMap());
    }

    protected ActionMap createActionMap() {
        ActionMap actions = new ActionMap();
        actions.put("cursorDown", new CursorDownAction());
        actions.put("cursorPageDown", new CursorPageDownAction());
        actions.put("cursorUp", new CursorUpAction());
        actions.put("cursorPageUp", new CursorPageUpAction());
        actions.put("cursorLeft", new CursorLeftAction());
        actions.put("cursorLineStart", new CursorLineStartAction());
        actions.put("cursorRight", new CursorRightAction());
        actions.put("cursorLineEnd", new CursorLineEndAction());
        actions.put("cursorHome", new CursorHomeAction());
        actions.put("cursorEnd", new CursorEndAction());
        actions.put("selectionDown", new SelectionDownAction());
        actions.put("selectionPageDown", new SelectionPageDownAction());
        actions.put("selectionUp", new SelectionUpAction());
        actions.put("selectionPageUp", new SelectionPageUpAction());
        actions.put("selectionLeft", new SelectionLeftAction());
        actions.put("selectionLineStart", new SelectionLineStartAction());
        actions.put("selectionRight", new SelectionRightAction());
        actions.put("selectionLineEnd", new SelectionLineEndAction());
        actions.put("selectionHome", new SelectionHomeAction());
        actions.put("selectionEnd", new SelectionEndAction());
        actions.put("selectAll", new SelectAllAction());

        actions.put(TransferHandler.getCutAction().getValue(Action.NAME),
                    TransferHandler.getCutAction());
        actions.put(TransferHandler.getCopyAction().getValue(Action.NAME),
                    TransferHandler.getCopyAction());
        actions.put(TransferHandler.getPasteAction().getValue(Action.NAME),
                    TransferHandler.getPasteAction());
        return actions;
    }

    protected void installListeners(HexViewer viewer) {
        mouseAdapter = new BasicMouseAdapter();
        viewer.addMouseListener(mouseAdapter);
        viewer.addMouseMotionListener(mouseAdapter);
        viewer.addMouseWheelListener(mouseAdapter);

        focusAdapter = new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                e.getComponent().repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                e.getComponent().repaint();
            }
        };
        viewer.addFocusListener(focusAdapter);
    }

    protected void uninstallListeners(HexViewer viewer) {
        if (mouseAdapter != null) {
            viewer.removeMouseListener(mouseAdapter);
            viewer.addMouseMotionListener(mouseAdapter);
            viewer.addMouseWheelListener(mouseAdapter);
            mouseAdapter = null;
        }

        if (focusAdapter != null) {
            viewer.removeFocusListener(focusAdapter);
            focusAdapter = null;
        }
    }

}
