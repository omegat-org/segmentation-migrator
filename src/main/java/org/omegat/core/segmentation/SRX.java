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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlFactory;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.fasterxml.jackson.module.jakarta.xmlbind.JakartaXmlBindAnnotationModule;
import gen.core.segmentation.Languagemap;
import gen.core.segmentation.Languagerule;
import gen.core.segmentation.ObjectFactory;
import gen.core.segmentation.Srx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLInputFactory;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The class with all the segmentation data possible -- rules, languages, etc.
 * It loads and saves its data from/to the SRX file.
 * <p>
 * When creating an SRX object with the default constructor, you get an empty
 * SRX without any rules. Please do not use default constructor, unless you
 * know what you are doing.
 *
 * @author Maxym Mykhalchuk
 * @author Thomas Cordonnier
 */
public class SRX implements Serializable {

    private static final long serialVersionUID = 2182125877925944613L;

    private static final Logger LOGGER = LoggerFactory.getLogger(SRX.class);

    public static final String CONF_SENTSEG = "segmentation.conf";
    public static final String SRX_SENTSEG = "segmentation.srx";
    private static final XmlMapper mapper;

    static {
        final XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        xmlInputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
        xmlInputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
        xmlInputFactory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, Boolean.FALSE);
        XmlFactory xmlFactory = new XmlFactory(xmlInputFactory);
        mapper = XmlMapper.builder(xmlFactory)
                .defaultUseWrapper(false)
                .enable(MapperFeature.USE_WRAPPER_NAME_AS_PROPERTY_NAME)
                .enable(SerializationFeature.INDENT_OUTPUT)
                .enable(ToXmlGenerator.Feature.WRITE_XML_DECLARATION)
                .defaultPropertyInclusion(JsonInclude.Value.construct(JsonInclude.Include.NON_EMPTY,
                        JsonInclude.Include.NON_EMPTY))
                .addModule(new JakartaXmlBindAnnotationModule())
                .build();
    }

    /**
     * Saves segmentation rules into specified directory.
     *
     * @param srx
     *            OmegaT object to be written; if null, means that we want to
     *            delete the file
     * @param outDir
     *            where to put the file. The file name is forced to
     *            {@link #SRX_SENTSEG} and will be in standard SRX format.
     */
    public static void saveToSrx(SRX srx, File outDir) throws IOException {
        File outFile = new File(outDir, SRX_SENTSEG);

        ObjectFactory factory = new ObjectFactory();
        Srx jaxbObject = factory.createSrx();
        jaxbObject.setVersion("2.0");
        jaxbObject.setHeader(factory.createHeader());
        jaxbObject.getHeader().setSegmentsubflows(srx.segmentSubflows ? "yes" : "no");
        jaxbObject.getHeader().setCascade(srx.cascade ? "yes" : "no");
        jaxbObject.setBody(factory.createBody());
        jaxbObject.getBody().setMaprules(factory.createMaprules());
        jaxbObject.getBody().setLanguagerules(factory.createLanguagerules());
        for (MapRule mr : srx.getMappingRules()) {
            Languagemap map = new Languagemap();
            String pattern = mr.getPattern();
            // we use standard name
            String language = LanguageCodes.getLanguageCodeByPattern(pattern);
            if (language == null) {
                language = LanguageCodes.getLanguageCodeByName(mr.getLanguage());
            }
            if (language == null) {
                language = mr.getLanguage();
            }
            map.setLanguagerulename(language);
            map.setLanguagepattern(pattern);
            jaxbObject.getBody().getMaprules().getLanguagemap().add(map);
            Languagerule lr = new Languagerule();
            lr.setLanguagerulename(language);
            jaxbObject.getBody().getLanguagerules().getLanguagerule().add(lr);
            for (Rule rule : mr.getRules()) {
                gen.core.segmentation.Rule jaxbRule = factory.createRule();
                lr.getRule().add(jaxbRule);
                jaxbRule.setBreak(rule.isBreakRule() ? "yes" : "no");
                if (rule.getBeforebreak() != null) {
                    jaxbRule.setBeforebreak(factory.createBeforebreak());
                    jaxbRule.getBeforebreak().setContent(rule.getBeforebreak());
                }
                if (rule.getAfterbreak() != null) {
                    jaxbRule.setAfterbreak(factory.createAfterbreak());
                    jaxbRule.getAfterbreak().setContent(rule.getAfterbreak());
                }
            }
        }

        try (FileOutputStream fos = new FileOutputStream(outFile)) {
            mapper.writerWithDefaultPrettyPrinter().writeValue(fos, jaxbObject);
        } catch (DatabindException e) {
            throw new IOException(e);
        }
    }

    static SRX loadSrxFile(URI rulesUri) {
        try (InputStream inputStream = Files.newInputStream(Paths.get(rulesUri))) {
            return loadSrxInputStream(inputStream);
        } catch (Exception e) {
            LOGGER.error("Error loading segmentation rules from file: " + rulesUri, e);
        }
        return null;
    }

    private static SRX loadSrxInputStream(InputStream io) throws IOException {
        Srx srx = mapper.readValue(io, Srx.class);
        final Map<String, List<Rule>> mapping = new HashMap<>();
        List<Languagerule> languageRuleList = srx.getBody().getLanguagerules().getLanguagerule();
        for (Languagerule languagerule : languageRuleList) {
            mapping.put(languagerule.getLanguagerulename(),
                    languagerule.getRule().stream().map(Rule::new).toList());
        }
        SRX res = new SRX();
        res.setSegmentSubflows(!"no".equalsIgnoreCase(srx.getHeader().getSegmentsubflows()));
        res.setCascade(!"no".equalsIgnoreCase(srx.getHeader().getCascade()));
        res.setVersion(srx.getVersion());
        res.setMappingRules(srx.getBody().getMaprules().getLanguagemap().stream()
                .map(languagemap -> new MapRule(languagemap, mapping.get(languagemap.getLanguagerulename())))
                .toList());
        return res;
    }

    public static SRX getDefault() {
        SRX srx = null;
        try {
            srx = loadSrxInputStream(SRX.class.getResourceAsStream("defaultRules.srx"));
            srx.includeEndingTags = true;
            srx.segmentSubflows = true;
        } catch (IOException e) {
            LOGGER.error("Unrecoverable error occurred!", e);
            System.exit(1);
        }
        return srx;
    }

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
