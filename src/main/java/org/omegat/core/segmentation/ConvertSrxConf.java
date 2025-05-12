package org.omegat.core.segmentation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConvertSrxConf {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConvertSrxConf.class);

    private static final String EXPECTED_PACKAGE = "org.omegat.core.segmentation";
    private static final Pattern CLASS_TAG_PATTERN = Pattern.compile("<\\s*(object|java).*class=\"([\\w.]+)\"");
    private static final Pattern PROPERTY_TAG_PATTERN = Pattern.compile("<\\s*void.*property=\"([\\w]+)\"");
    private static final Pattern METHOD_TAG_PATTERN = Pattern.compile("<\\s*void.*method=\"([\\w]+)\"");

    private static final List<String> ALLOWED_CLASSES = List.of("java.util.ArrayList");
    private static final List<String> ALLOWED_PROPERTIES = List.of(
            "rules", "mappingRules", "language", "pattern",
            "afterbreak", "beforebreak", "breakRule", "version"
    );
    private static final List<String> ALLOWED_METHODS = List.of("add");

    public static void main(String[] args) {
        Path confFilePath = Paths.get(".").resolve(SRX.CONF_SENTSEG);
        if (!confFilePath.toFile().exists()) {
            LOGGER.error("File is not found!");
            System.exit(1);
        }

        try {
            checkFile(confFilePath);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            System.exit(1);
        }
        try {
            Path srxFilePath = Paths.get(".").resolve(SRX.SRX_SENTSEG);
            if (srxFilePath.toFile().exists()) {
                Files.delete(srxFilePath);
            }
            SRX result = SRX.loadConfFile(confFilePath.toFile(), srxFilePath.getParent().toFile());
            SRX.saveToSrx(result, srxFilePath.getParent().toFile());
        } catch (Exception e) {
            LOGGER.error("Error occurred during conversion!", e);
            System.exit(1);
        }
    }

    static void checkFile(Path path) throws Exception {
        LOGGER.info("Checking file: {}", path);
        if (!checkFileContent(path, StandardCharsets.UTF_8.newDecoder(),
                (p, chars) -> {
                    Matcher propertyMatcher = PROPERTY_TAG_PATTERN.matcher(chars);
                    while (propertyMatcher.find()) {
                        String propertyName = propertyMatcher.group(1);
                        if (!ALLOWED_PROPERTIES.contains(propertyName)) {
                            throw new RuntimeException(
                                    String.format("Property name '%s' in file %s is not from the expected.",
                                            propertyName, p));
                        }
                    }

                    Matcher classMatcher = CLASS_TAG_PATTERN.matcher(chars);
                    while (classMatcher.find()) {
                        String tagName = classMatcher.group(1);
                        String className = classMatcher.group(2);
                        if ((!"java".equals(tagName) || !"java.beans.XMLDecoder".equals(className)) &&
                                (!"object".equals(tagName) || !ALLOWED_CLASSES.contains(className)) &&
                                "object".equals(tagName) && !className.startsWith(EXPECTED_PACKAGE)) {
                                throw new RuntimeException(
                                        String.format("Class name '%s' in file %s is not from the expected package.",
                                                className, p));
                        }
                    }

                    Matcher methodMatcher = METHOD_TAG_PATTERN.matcher(chars);
                    while (methodMatcher.find()) {
                        String methodName = methodMatcher.group(1);
                        if (!ALLOWED_METHODS.contains(methodName)) {
                            throw new RuntimeException(
                                    String.format("Method name '%s' in file %s is not from the expected.",
                                            methodName, p));
                        }
                    }
                })) {
            throw new Exception("Malformed segmentation.conf file is detected!");
        }
    }

    private static boolean checkFileContent(Path path, CharsetDecoder decoder,
                                        BiConsumer<Path, CharSequence> consumer) {
        try {
            byte[] bytes = Files.readAllBytes(path);
            CharBuffer chars = decoder.decode(ByteBuffer.wrap(bytes));
            consumer.accept(path, chars);
            return true;
        } catch (IOException ex) {
            LOGGER.error("Error occurred during file processing!", ex);
        }
        return false;
    }
}
