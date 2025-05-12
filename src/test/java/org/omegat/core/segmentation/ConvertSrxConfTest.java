package org.omegat.core.segmentation;

import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.fail;

public class ConvertSrxConfTest {
    private static final String MALFORMED_SEGMENT_CONF = "src/test/resources/segmentation/malformed/segmentation.conf";

    @Test
    public void test() {
        Pattern pattern = Pattern.compile("Class name '[\\w.]+' in file [\\w/.]+ is not from the expected package\\.");
        Path segmentconf = Paths.get(MALFORMED_SEGMENT_CONF);
        try {
            ConvertSrxConf.checkFile(segmentconf);
            fail();
        } catch (Exception e) {
            // expected
            Matcher m = pattern.matcher(e.getMessage());
            if (!m.find()) {
                fail("Unexpected error message: " + e.getMessage());
            }
        }
    }
}
