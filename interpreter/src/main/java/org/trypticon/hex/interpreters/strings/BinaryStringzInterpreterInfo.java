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

package org.trypticon.hex.interpreters.strings;

import org.trypticon.hex.interpreters.AbstractInterpreterInfo;
import org.trypticon.hex.interpreters.Interpreter;
import org.trypticon.hex.util.LocalisedName;

import java.util.Map;

/**
 * Information about the null-terminated binary string interpreter.
 *
 * @author trejkaz
 */
public class BinaryStringzInterpreterInfo extends AbstractInterpreterInfo {
    public BinaryStringzInterpreterInfo() {
        super(new LocalisedName("org/trypticon/hex/interpreters/Bundle", "Interpreters.BinaryStringz"));
    }

    @Override
    public Interpreter<BinaryStringValue> create(Map<String, Object> options) {
        return new BinaryStringzInterpreter();
    }
}
