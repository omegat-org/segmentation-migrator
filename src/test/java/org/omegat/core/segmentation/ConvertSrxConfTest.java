package org.omegat.core.segmentation;

import org.junit.Test;
import org.omegat.util.ValidationResult;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ConvertSrxConfTest {
    private static final String MALFORMED_SEGMENT_CONF = "src/test/resources/segmentation/malformed/segmentation.conf";

    @Test
    public void testMalformedSegmentConf() {
        Pattern pattern = Pattern.compile("Class name '[\\w.]+' in file [\\w/.]+ is not from the expected package\\.");
        Path segmentconf = Paths.get(MALFORMED_SEGMENT_CONF);
        ValidationResult result = ConvertSrxConf.checkFile(segmentconf);
        assertFalse(result.isValid());
        Matcher m = pattern.matcher(result.getErrorMessage());
        if (!m.find()) {
            fail("Unexpected error message: " + result.getErrorMessage());
        }
    }

    @Test
    public void testSegmentionConf() {
        Path segmentconf = Paths.get("src/test/resources/segmentation/locale_de_54/segmentation.conf");
        ValidationResult result = ConvertSrxConf.checkFile(segmentconf);
        assertTrue(result.isValid());
    }
}
