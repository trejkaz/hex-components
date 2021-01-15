/*
 * Hex - a hex viewer and annotator
 * Copyright (C) 2009-2014,2016-2017  Trejkaz, Hex Project
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

apply(plugin = "java-platform")

dependencies {
    constraints {
        "api"("com.google.code.findbugs:jsr305:3.0.2")

        val icu4jVersion = "60.1"
        "api"("com.ibm.icu:icu4j:$icu4jVersion")
        "api"("com.ibm.icu:icu4j-charset:$icu4jVersion")

        val swingXVersion = "1.6.6-SNAPSHOT"
        "api"("org.swinglabs.swingx:swingx-action:$swingXVersion")
        "api"("org.swinglabs.swingx:swingx-common:$swingXVersion")
        "api"("org.swinglabs.swingx:swingx-core:$swingXVersion")
        "api"("org.swinglabs.swingx:swingx-painters:$swingXVersion")
        "api"("org.swinglabs.swingx:swingx-plaf:$swingXVersion")

        "api"("junit:junit:4.13")

        "api"("org.hamcrest:hamcrest:2.2")

        val jmockVersion = "2.12.0"
        "api"("org.jmock:jmock:$jmockVersion")
        "api"("org.jmock:jmock-junit4:$jmockVersion")
    }
}