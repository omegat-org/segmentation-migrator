package org.omegat.core.segmentation;

import org.junit.Test;
import org.omegat.util.ValidationResult;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SegmentationConfMigratorTest {
    private static final String MALFORMED0 = "src/test/resources/segmentation/malformed0/segmentation.conf";
    private static final String MALFORMED1 = "src/test/resources/segmentation/malformed1/segmentation.conf";

    @Test
    public void testMalformedSegmentConf0() {
        Path segmentconf = Paths.get(MALFORMED0);
        SegmentationConfValidator validator = new SegmentationConfValidator(segmentconf);
        ValidationResult result = validator.validate();
        assertFalse(result.isValid());
        assertTrue(result.getErrorMessage().contains("java.lang.ProcessBuilder"));
    }

    @Test
    public void testMalformedSegmentConf1() {
        Path segmentconf = Paths.get(MALFORMED1);
        SegmentationConfValidator validator = new SegmentationConfValidator(segmentconf);
        ValidationResult result = validator.validate();
        assertFalse(result.isValid());
        assertTrue(result.getErrorMessage().contains("java.beans.XMLDecoder"));
    }

    @Test
    public void testSegmentionConf() {
        Path segmentconf = Paths.get("src/test/resources/segmentation/locale_de_54/segmentation.conf");
        SegmentationConfValidator validator = new SegmentationConfValidator(segmentconf);
        ValidationResult result = validator.validate();
        assertTrue(result.isValid());
    }
}
