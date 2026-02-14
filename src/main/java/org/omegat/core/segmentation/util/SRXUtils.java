/*
 * OmegaT - Computer Assisted Translation (CAT) tool
 *          with fuzzy matching, translation memory, keyword search,
 *          glossaries, and translation leveraging into updated projects.
 *
 * Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
 *               2008 Alex Buloichik
 *               2018 Thomas Cordonnier
 *               2025-2026 Hiroshi MIura
 *               Home page: https://www.omegat.org/
 *               Support center: https://omegat.org/support
 *
 * This file is part of OmegaT.
 *
 * OmegaT is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OmegaT is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.omegat.core.segmentation.util;

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
import org.jspecify.annotations.Nullable;
import org.omegat.core.segmentation.MapRule;
import org.omegat.core.segmentation.Rule;
import org.omegat.core.segmentation.SRX;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLInputFactory;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Utility class for SRX segmentation rules.
 *
 * @author Maxym Mykhalchuk
 * @author Thomas Cordonnier
 * @author Hiroshi Miura
 */
public class SRXUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(SRXUtils.class);

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

    public static SRX getDefault() throws IOException {
        SRX srx = loadSrxInputStream(Objects.requireNonNull(
                SRXUtils.class.getResourceAsStream("/org/omegat/core/segmentation/defaultRules.srx")));
        srx.setIncludeEndingTags(true);
        srx.setSegmentSubflows(true);
        return srx;
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
        jaxbObject.getHeader().setSegmentsubflows(srx.isSegmentSubflows() ? "yes" : "no");
        jaxbObject.getHeader().setCascade(srx.isCascade() ? "yes" : "no");
        jaxbObject.setBody(factory.createBody());
        jaxbObject.getBody().setMaprules(factory.createMaprules());
        jaxbObject.getBody().setLanguagerules(factory.createLanguagerules());
        for (MapRule mr : srx.getMappingRules()) {
            Languagemap map = new Languagemap();
            String pattern = mr.getPattern();
            // we use standard name
            String language = LanguageCodes.getInstance().getStandardNameFromMapRule(mr);
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

    public static @Nullable SRX loadSrxFile(URI rulesUri) {
        try (InputStream inputStream = Files.newInputStream(Paths.get(rulesUri))) {
            return loadSrxInputStream(inputStream);
        } catch (Exception e) {
            LOGGER.error("Error loading segmentation rules from file: " + rulesUri, e);
        }
        return null;
    }

    static SRX loadSrxInputStream(InputStream io) throws IOException {
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
}
