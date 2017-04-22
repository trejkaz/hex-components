/*
 * Hex - a hex viewer and annotator
 * Copyright (C) 2009-2014,2017  Trejkaz, Hex Project
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

package org.trypticon.hex.anno;

import java.util.EventListener;

/**
 * Listener for changes in an annotation collection.
 *
 * @author trejkaz
 */
public interface AnnotationCollectionListener extends EventListener {

    /**
     * Called when an annotation is added.
     *
     * @param event the event.
     */
    void annotationsAdded(AnnotationCollectionEvent event);

    /**
     * Called when an annotation is removed.
     *
     * @param event the event.
     */
    void annotationsRemoved(AnnotationCollectionEvent event);

    /**
     * Called when annotations have changed in some way but are still the same annotations.
     *
     * @param event the event.
     */
    void annotationsChanged(AnnotationCollectionEvent event);

}
