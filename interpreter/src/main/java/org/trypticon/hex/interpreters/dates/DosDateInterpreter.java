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

package org.trypticon.hex.interpreters.dates;

import org.trypticon.hex.interpreters.AbstractFixedLengthInterpreter;
import org.trypticon.hex.binary.Binary;
import org.trypticon.hex.interpreters.primitives.IntBitField;
import org.trypticon.hex.interpreters.primitives.LittleEndian;

import javax.annotation.Nonnull;

/**
 * Interpreter for MS-DOS date values, which are used in a number of other formats.
 *
 * @author trejkaz
 */
public class DosDateInterpreter extends AbstractFixedLengthInterpreter<Date> {
    public DosDateInterpreter() {
        super("dos_date", Date.class, 2);
    }

    @Nonnull
    @Override
    public Date interpret(@Nonnull Binary binary, long position) {
        return new DosDate(LittleEndian.getShort(binary, position));
    }

    private static class DosDate extends AbstractDate {
        private static final IntBitField day = IntBitField.lowest(5);
        private static final IntBitField month = day.next(4);
        private static final IntBitField year = month.next(7);
        private static final int YEAR_OFFSET = 1980;

        private final short value;

        private DosDate(short value) {
            this.value = value;
        }

        @Override
        public int getYear() {
            return year.evaluate(value) + YEAR_OFFSET;
        }

        @Override
        public int getMonth() {
            return month.evaluate(value);
        }

        @Override
        public int getDay() {
            return day.evaluate(value);
        }

        @Override
        public long length() {
            return 2;
        }
    }
}
