package org.omegat.core.segmentation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ConvertSrxConf {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConvertSrxConf.class);

    public static void main(String[] args) {
        Path confFilePath = Paths.get(".").resolve(SRX.CONF_SENTSEG);
        Path srxFilePath = Paths.get(".").resolve(SRX.SRX_SENTSEG);
        if (!confFilePath.toFile().exists()) {
            System.exit(1);
        }
        try {
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
}
