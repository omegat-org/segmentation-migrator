package org.omegat.core.segmentation;

import org.junit.Test;
import org.omegat.util.ValidationResult;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ConvertSrxConfTest {
    private static final String MALFORMED_SEGMENT_CONF = "src/test/resources/segmentation/malformed/segmentation.conf";

    @Test
    public void testMalformedSegmentConf() {
        Path segmentconf = Paths.get(MALFORMED_SEGMENT_CONF);
        ValidationResult result = ConvertSrxConf.checkFile(segmentconf);
        assertFalse(result.isValid());
        assertTrue(result.getErrorMessage().contains("java.lang.ProcessBuilder"));
    }

    @Test
    public void testSegmentionConf() {
        Path segmentconf = Paths.get("src/test/resources/segmentation/locale_de_54/segmentation.conf");
        ValidationResult result = ConvertSrxConf.checkFile(segmentconf);
        assertTrue(result.isValid());
    }
}
