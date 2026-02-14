/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2008 Alex Buloichik
               2018 Thomas Cordonnier
               2025 Hiroshi MIura
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

package org.omegat.core.segmentation;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * The class with all the segmentation data possible -- rules, languages, etc.
 *
 * @author Maxym Mykhalchuk
 * @author Thomas Cordonnier
 */
public class SRX implements Serializable {

    @Serial
    private static final long serialVersionUID = 2182125877925944613L;

    /**
     * Holds value of property cascade: true, unless we read an SRX where it was
     * set to false.
     */
    private boolean cascade = true;

    /**
     * Getter for property cascade.
     *
     * @return Value of property cascade.
     */
    public boolean isCascade() {
        return this.cascade;
    }

    /**
     * Setter for property cascade.
     *
     * @param cascade
     *            New value of property cascade.
     */
    public void setCascade(boolean cascade) {
        this.cascade = cascade;
    }

    /**
     * Holds value of property segmentSubflows.
     */
    private boolean segmentSubflows = true;

    /**
     * Getter for property segmentSubflows.
     *
     * @return Value of property segmentSubflows.
     */
    public boolean isSegmentSubflows() {

        return this.segmentSubflows;
    }

    /**
     * Setter for property segmentSubflows.
     *
     * @param segmentSubflows
     *            New value of property segmentSubflows.
     */
    public void setSegmentSubflows(boolean segmentSubflows) {

        this.segmentSubflows = segmentSubflows;
    }

    /**
     * Holds value of property includeStartingTags.
     */
    private boolean includeStartingTags;

    /**
     * Getter for property includeStartingTags.
     *
     * @return Value of property includeStartingTags.
     */
    public boolean isIncludeStartingTags() {

        return this.includeStartingTags;
    }

    /**
     * Setter for property includeStartingTags.
     *
     * @param includeStartingTags
     *            New value of property includeStartingTags.
     */
    public void setIncludeStartingTags(boolean includeStartingTags) {
        this.includeStartingTags = includeStartingTags;
    }

    /**
     * Holds value of property includeEndingTags.
     */
    private boolean includeEndingTags = true;

    /**
     * Getter for property includeEndingTags.
     *
     * @return Value of property includeEndingTags.
     */
    public boolean isIncludeEndingTags() {
        return this.includeEndingTags;
    }

    /**
     * Setter for property includeEndingTags.
     *
     * @param includeEndingTags
     *            New value of property includeEndingTags.
     */
    public void setIncludeEndingTags(boolean includeEndingTags) {
        this.includeEndingTags = includeEndingTags;
    }

    /**
     * Holds value of property includeIsolatedTags.
     */
    private boolean includeIsolatedTags;

    /**
     * Getter for property includeIsolatedTags.
     *
     * @return Value of property includeIsolatedTags.
     */
    public boolean isIncludeIsolatedTags() {

        return this.includeIsolatedTags;
    }

    /**
     * Setter for property includeIsolatedTags.
     *
     * @param includeIsolatedTags
     *            New value of property includeIsolatedTags.
     */
    public void setIncludeIsolatedTags(boolean includeIsolatedTags) {

        this.includeIsolatedTags = includeIsolatedTags;
    }

    /**
     * Correspondences between languages and their segmentation rules. Each
     * element is of class {@link MapRule}.
     */
    private List<MapRule> mappingRules = new ArrayList<>();

    /**
     * Returns all mapping rules (of class {@link MapRule}) at once:
     * correspondences between languages and their segmentation rules.
     */
    public List<MapRule> getMappingRules() {
        return mappingRules;
    }

    /**
     * Sets all mapping rules (of class {@link MapRule}) at once:
     * correspondences between languages and their segmentation rules.
     */
    public void setMappingRules(List<MapRule> rules) {
        mappingRules = rules;
    }

    // ////////////////////////////////////////////////////////////////
    // Versioning properties to detect version upgrades
    // and possibly do something if required

    /** Version of OmegaT segmentation support. */
    private String version;

    /** Returns segmentation support version. */
    public String getVersion() {
        return version;
    }

    /** Sets segmentation support version. */
    public void setVersion(String value) {
        version = value;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (includeEndingTags ? 1231 : 1237);
        result = prime * result + (includeIsolatedTags ? 1231 : 1237);
        result = prime * result + (includeStartingTags ? 1231 : 1237);
        result = prime * result + ((mappingRules == null) ? 0 : mappingRules.hashCode());
        result = prime * result + (segmentSubflows ? 1231 : 1237);
        result = prime * result + (cascade ? 1231 : 1237);
        result = prime * result + ((version == null) ? 0 : version.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        SRX other = (SRX) obj;
        if (includeEndingTags != other.includeEndingTags) {
            return false;
        }
        if (includeIsolatedTags != other.includeIsolatedTags) {
            return false;
        }
        if (includeStartingTags != other.includeStartingTags) {
            return false;
        }
        if (mappingRules == null) {
            if (other.mappingRules != null) {
                return false;
            }
        } else if (!mappingRules.equals(other.mappingRules)) {
            return false;
        }
        if (segmentSubflows != other.segmentSubflows) {
            return false;
        }
        if (cascade != other.cascade) {
            return false;
        }
        if (version == null) {
            if (other.version != null) {
                return false;
            }
        } else if (!version.equals(other.version)) {
            return false;
        }
        return true;
    }
}
