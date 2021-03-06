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

package org.trypticon.hex.interpreters;

/**
 * Base convenience class for implementing interpreters.
 *
 * @param <V> the value type.
 * @author trejkaz
 */
public abstract class AbstractInterpreter<V extends Value> implements Interpreter<V> {
    private final String name;
    private final Class<V> valueType;

    protected AbstractInterpreter(String name, Class<V> valueType) {
        this.name = name;
        this.valueType = valueType;
    }

    @Override
    public final String getName() {
        return name;
    }

    @Override
    public final Class<V> getType() {
        return valueType;
    }

    @Override
    public final String toString() {
        return getName();
    }
}
