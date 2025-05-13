package org.omegat.core.segmentation;

import org.omegat.util.ValidationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SegmentationConfMigrator {

    private static final Logger LOGGER = LoggerFactory.getLogger(SegmentationConfMigrator.class);

    public static void main(String[] args) {
        String targetDir = ".";
        Path confFilePath = Paths.get(targetDir).resolve(SRX.CONF_SENTSEG);
        Path srxFilePath = Paths.get(targetDir).resolve(SRX.SRX_SENTSEG);
        checkConfigFile(confFilePath);
        convertToSrx(confFilePath, srxFilePath);
    }

    private static void checkConfigFile(Path configPath) {
        if (!configPath.toFile().exists()) {
            LOGGER.error("File " + SRX.CONF_SENTSEG + " is not found!");
            System.exit(1);
        }
        SegmentationConfValidator validator = new SegmentationConfValidator(configPath);
        ValidationResult validationResult = validator.validate();
        if (!validationResult.isValid()) {
            LOGGER.error(validationResult.getErrorMessage());
            System.exit(1);
        }
    }

    private static void convertToSrx(Path configPath, Path srxFilePath) {
        try {
            if (srxFilePath.toFile().exists()) {
                Files.delete(srxFilePath);
            }
            File srxParent = srxFilePath.getParent().toFile();
            SRX srx = SRX.loadConfFile(configPath.toFile(), srxParent);
            SRX.saveToSrx(srx, srxParent);
        } catch (Exception e) {
            LOGGER.error("Error occurred during conversion!", e);
            System.exit(1);
        }
    }
}
