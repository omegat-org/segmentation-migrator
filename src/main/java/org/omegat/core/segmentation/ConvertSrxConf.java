package org.omegat.core.segmentation;

import org.omegat.util.ValidationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ConvertSrxConf {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConvertSrxConf.class);

    public static void main(String[] args) {
        Path confFilePath = Paths.get(".").resolve(SRX.CONF_SENTSEG);
        checkConfigFile(confFilePath);
        convertToSrx(confFilePath);
    }

    private static void checkConfigFile(Path configPath) {
        if (!configPath.toFile().exists()) {
            LOGGER.error("File is not found!");
            System.exit(1);
        }
        SegmentationConfValidator validator = new SegmentationConfValidator(configPath);
        ValidationResult validationResult = validator.validate();
        if (!validationResult.isValid()) {
            LOGGER.error(validationResult.getErrorMessage());
            System.exit(1);
        }
    }

    private static void convertToSrx(Path configPath) {
        try {
            Path srxFilePath = Paths.get(".").resolve(SRX.SRX_SENTSEG);
            if (srxFilePath.toFile().exists()) {
                Files.delete(srxFilePath);
            }
            SRX srx = SRX.loadConfFile(configPath.toFile(), srxFilePath.getParent().toFile());
            SRX.saveToSrx(srx, srxFilePath.getParent().toFile());
        } catch (Exception e) {
            LOGGER.error("Error occurred during conversion!", e);
            System.exit(1);
        }
    }
}
