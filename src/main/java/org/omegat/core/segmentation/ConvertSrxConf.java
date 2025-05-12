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
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConvertSrxConf {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConvertSrxConf.class);

    public static void main(String[] args) {
        Path confFilePath = Paths.get(".").resolve(SRX.CONF_SENTSEG);
        try {
            checkFile(confFilePath);
        } catch (Exception e) {
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
        if (!path.toFile().exists()) {
            System.exit(1);
        }
        Pattern pattern = Pattern.compile("<\\s*(object|java).*class=\"([\\w.]+)\"");
        if (!checkFileContent(path, StandardCharsets.UTF_8.newDecoder(),
                (p, chars) -> {
                    Matcher matcher = pattern.matcher(chars);
                    while (matcher.find()) {
                        String tagName = matcher.group(1);
                        String className = matcher.group(2);
                        if ("java".equals(tagName) && "java.beans.XMLDecoder".equals(className)) {
                            continue;
                        }
                        if ("object".equals(tagName) && className.equals("java.util.ArrayList")) {
                            continue;
                        }
                        if ("object".equals(tagName) && !className.startsWith("org.omegat.core.segmentation")) {
                            throw new RuntimeException(
                                    String.format("Class name '%s' in file %s is not from the expected package.",
                                            className, p));
                        }
                    }
                })) {
            throw new Exception("Malformed segmentation.conf file is detected!");
        }
    }

    private static boolean checkFileContent(Path p, CharsetDecoder decoder,
                                        BiConsumer<Path, CharSequence> consumer) {
        try {
            byte[] bytes = Files.readAllBytes(p);
            CharBuffer chars = decoder.decode(ByteBuffer.wrap(bytes));
            for (int i = 0; i < chars.limit(); i++) {
                int c = chars.charAt(i);
                if (c == 0x202e) {
                    // found Right-to-left-override: unicode 202e
                    LOGGER.error("File contains Right-to-Left-Override (RLTO) character: {}", p);
                    return false;
                }
                if (c == 0x0009 || c == 0x00a0 || c == 0x00ad || c == 0x034f || c == 0x061c
                        || c >= 0x2000 && c < 0x200b || c == 0x2028 || c >= 0x205f && c <= 0x206f
                        || c == 0x2800 || c == 0x3000 || c == 0x3164) {
                    LOGGER.error("File contains invisible character: {}", p);
                    return false;
                }
            }
            chars.clear();
            consumer.accept(p, chars);
            return true;
        } catch (IOException ex) {
            LOGGER.error("Error occurred during file processing!", ex);
        }
        return false;
    }
}
