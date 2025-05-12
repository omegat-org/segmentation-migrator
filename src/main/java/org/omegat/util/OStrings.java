/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               Home page: https://www.omegat.org/
               Support center: https://omegat.org/support

 This file is part of OmegaT.

 OmegaT is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 OmegaT is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <https://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.util;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Localizable strings.
 * <p>
 * Please don't add any new strings here, use <code>getString</code> method.
 * This class still has so many strings for legacy reasons only.
 *
 * @author Keith Godfrey
 * @author Maxym Mykhalchuk
 * @author Henry Pijffers (henry.pijffers@saxnot.com)
 */
public final class OStrings {

    private OStrings() {
    }

    private static final String BASENAME = "org/omegat/Bundle";

    /** Resource bundle that contains all the strings */
    private static ResourceBundle bundle = ResourceBundle.getBundle(BASENAME);

    /**
     * Returns resource bundle.
     */
    public static ResourceBundle getResourceBundle() {
        return bundle;
    }

    /**
     * Loads resources with the specified locale.
     * @param locale Locale to load.
     */
    public static void loadBundle(Locale locale) {
        bundle = ResourceBundle.getBundle(BASENAME, locale);
    }

    /** Returns a localized String for a key */
    public static String getString(String key) {
        return bundle.getString(key);
    }
}
