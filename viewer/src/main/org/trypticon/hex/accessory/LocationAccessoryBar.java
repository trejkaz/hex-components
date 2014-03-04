/*
 * Hex - a hex viewer and annotator
 * Copyright (C) 2009-2013  Trejkaz, Hex Project
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

package org.trypticon.hex.accessory;

import org.trypticon.hex.HexViewer;
import org.trypticon.hex.HexViewerSelectionModel;
import org.trypticon.hex.util.swingsupport.StealthFormattedTextField;

import javax.swing.GroupLayout;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.LayoutStyle;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.ParseException;
import java.util.ResourceBundle;

/**
 * An accessory bar showing the location within the file and allowing changing it.
 *
 * @author trejkaz
 */
public class LocationAccessoryBar extends AccessoryBar {
    private final HexViewer viewer;
    private final HexFormattedTextField offsetField;
    private final HexFormattedTextField lengthField;
    private final Handler handler;

    public LocationAccessoryBar(HexViewer viewer) {
        this.viewer = viewer;
        handler = new Handler();

        ResourceBundle bundle = ResourceBundle.getBundle("org/trypticon/hex/Bundle");
        JLabel offsetLabel = new JLabel(bundle.getString("AccessoryBars.Location.selectedOffset"));
        offsetField = new HexFormattedTextField();
        JLabel lengthLabel = new JLabel(bundle.getString("AccessoryBars.Location.length"));
        lengthField = new HexFormattedTextField();

        offsetLabel.putClientProperty("JComponent.sizeVariant", "small");
        offsetField.putClientProperty("JComponent.sizeVariant", "small");
        lengthLabel.putClientProperty("JComponent.sizeVariant", "small");
        lengthField.putClientProperty("JComponent.sizeVariant", "small");

        GroupLayout layout = new GroupLayout(this);
        setLayout(layout);

        layout.setHorizontalGroup(layout.createSequentialGroup()
                                          .addContainerGap()
                                          .addComponent(offsetLabel)
                                          .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                          .addComponent(offsetField, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                          .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                          .addComponent(lengthLabel)
                                          .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                          .addComponent(lengthField, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                          .addContainerGap());

        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(offsetLabel)
                .addComponent(offsetField)
                .addComponent(lengthLabel)
                .addComponent(lengthField));
    }

    private void binaryChanged() {
        String longestValue;
        try {
            longestValue = offsetField.getFormatter().valueToString(viewer.getBinary().length());
        } catch (ParseException e) {
            throw new RuntimeException("Unexpected error converting to string", e);
        }
        offsetField.setColumns(longestValue.length());
        lengthField.setColumns(longestValue.length());
    }

    private void locationChanged() {
        HexViewerSelectionModel selectionModel = viewer.getSelectionModel();
        long selectionStart = selectionModel.getSelectionStart();
        long selectionEnd = selectionModel.getSelectionEnd();
        offsetField.setValue(selectionStart);
        lengthField.setValue(selectionEnd - selectionStart + 1);
    }

    private void userChangedLocation() {
        long offset = ((Number) offsetField.getValue()).longValue();
        long length = ((Number) lengthField.getValue()).longValue();
        if (offset < 0 || offset > viewer.getBinary().length() ||
            offset + length > viewer.getBinary().length()) {
            return;
        }
        HexViewerSelectionModel selectionModel = viewer.getSelectionModel();
        selectionModel.setCursor(offset + length - 1);
        selectionModel.setCursorAndExtendSelection(offset);
    }

    @Override
    public void addNotify() {
        super.addNotify();
        viewer.addPropertyChangeListener("binary", handler);
        viewer.getSelectionModel().addChangeListener(handler);
        binaryChanged();
        locationChanged();
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        viewer.removePropertyChangeListener("binary", handler);
    }

    private class Handler implements PropertyChangeListener, ChangeListener {
        @Override
        public void propertyChange(PropertyChangeEvent event) {
            switch (event.getPropertyName()) {
                case "binary":
                    binaryChanged();
                    break;
            }
        }

        @Override
        public void stateChanged(ChangeEvent event) {
            locationChanged();
        }
    }

    private class HexFormattedTextField extends StealthFormattedTextField {
        // setFormatter() appears to have no effect. Swing uses its own default number formatter.
        private HexFormattedTextField() {
            super(new AbstractFormatterFactory() {
                @Override
                public AbstractFormatter getFormatter(JFormattedTextField tf) {
                    return new AbstractFormatter() {
                        @Override
                        public Object stringToValue(String text) throws ParseException {
                            text = text.trim();
                            if (text.isEmpty()) {
                                return null;
                            }
                            try {
                                return Long.decode(text);
                            } catch (NumberFormatException e) {
                                throw new ParseException(text, 0);
                            }
                        }

                        @Override
                        public String valueToString(Object value) throws ParseException {
                            if (value == null) {
                                return "";
                            }
                            return "0x" + Long.toString(((Number) value).longValue(), 16).toUpperCase();
                        }
                    };
                }
            });
        }

        @Override
        public void commitEdit() throws ParseException {
            super.commitEdit();
            userChangedLocation();
        }
    }
}
