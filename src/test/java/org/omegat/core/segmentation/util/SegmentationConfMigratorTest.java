/*
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2024-2026 Hiroshi Miura
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
 */

package org.omegat.core.segmentation.util;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.omegat.core.segmentation.MapRule;
import org.omegat.core.segmentation.SRX;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Aaron Madlon-Kay
 * @author Hiroshi Miura
 */
public class SegmentationConfMigratorTest {

    private static final String SEGMENT_CONF_BASE = "src/test/resources/segmentation/";

    @Rule
    public final TemporaryFolder folder = TemporaryFolder.builder().assureDeletion().build();

    @Test
    public void testSrxMigration() throws Exception {
        File segmentConf = Paths.get(SEGMENT_CONF_BASE, "locale_en", "segmentation.conf").toFile();
        File configDir = folder.newFolder();
        testSrxMigration(segmentConf, configDir, Locale.of("en"), 18);
    }

    @Test
    public void testSrxMigrationJa() throws Exception {
        File segmentConf = Paths.get(SEGMENT_CONF_BASE, "locale_ja", "segmentation.conf").toFile();
        File configDir = folder.newFolder();
        testSrxMigration(segmentConf, configDir, Locale.of("ja"), 18);
    }

    @Test
    public void testSrxMigrationOldDe() throws Exception {
        File segmentConf = Paths.get(SEGMENT_CONF_BASE, "locale_de_54", "segmentation.conf").toFile();
        File configDir = folder.newFolder();
        testSrxMigration(segmentConf, configDir, Locale.of("de"), 18);
    }

    @Test
    public void testSrxMigrationCustomNl() throws Exception {
        File segmentConf = Paths.get(SEGMENT_CONF_BASE, "locale_nl_custom", "segmentation.conf").toFile();
        File configDir = folder.newFolder();
        testSrxMigration(segmentConf, configDir, Locale.of("nl"), 19);
    }

    /**
     * Test SRX writer/reader.
     * <p>
     * Previous versions have a bug when saving segmentation.conf file. It is
     * better to save language property using language code defined in
     * LanguageCode class. Unfortunately, OmegaT 6.0 and before produce a
     * localized language name for the property. The test case here trys reading
     * a segmentation.conf file that is produced by OmegaT in English
     * environment and Japanese environment.
     */
    private void testSrxMigration(File segmentConf, File configDir, Locale locale, int ruleNum) throws IOException {
        File segmentSrx = new File(configDir, "segmentation.srx");
        // load from conf file
        assertTrue(segmentConf.exists());
        assertTrue(segmentConf.isFile());
        ValidationResult result = SegmentationConfMigrator.checkConfigFile(segmentConf.toPath());
        assertTrue(result.isValid());
        SRX srxOrig = SegmentationConfMigrator.convertToSrx(segmentConf.toPath(), segmentSrx.toPath(), locale);
        assertNotNull(srxOrig);
        List<MapRule> mapRuleList = srxOrig.getMappingRules();
        assertNotNull(mapRuleList);
        assertEquals(ruleNum, mapRuleList.size());
        assertTrue(srxOrig.isCascade());
        assertTrue(srxOrig.isSegmentSubflows());
        assertTrue(checkRules(mapRuleList, "JA.*", LanguageCodes.JAPANESE_CODE));
        if (ruleNum > 18) {
            assertTrue(checkRules(mapRuleList, "EN-GB", "IFAF-Engels"));
        }
        assertEquals("2.0", srxOrig.getVersion());
    }

    private boolean checkRules(List<MapRule> mapRuleList, String pattern, String language) {
        return mapRuleList.stream()
                .filter(mapRule -> Objects.equals(language, mapRule.getLanguage()))
                .map(mapRule -> Objects.equals(pattern, mapRule.getPattern()))
                .findFirst()
                .orElse(false);
    }
}
