package org.omegat.core.segmentation;

import org.omegat.util.ValidationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class ConvertSrxConf {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConvertSrxConf.class);

    private static final Set<String> ALLOWED_TAGS = Set.of("java", "void", "object", "string", "boolean", "array");
    private static final Map<String, Set<String>> ALLOWED_ATTRIBUTES = Map.of(
            "java", Set.of("version", "class"),
            "object", Set.of("class", "id"),
            "void", Set.of("method", "property", "index"),
            "array", Set.of("class", "length", "id"));
    private static final Set<String> IGNORE_VALUE_TAGS = Set.of("string", "boolean", "id", "version", "index");
    private static final Map<String, Set<String>> ALLOWED_ATTRIBUTE_VALUES = Map.of(
            "property", Set.of("rules", "mappingRules", "language", "pattern", "afterbreak", "beforebreak", "breakRule", "version"),
            "class", Set.of("java.util.ArrayList", "java.beans.XMLDecoder", "org.omegat.core.segmentation.SRX",
                    "org.omegat.core.segmentation.MapRule", "org.omegat.core.segmentation.Rule"),
            "method", Set.of("add"));

    public static void main(String[] args) {
        Path confFilePath = Paths.get(".").resolve(SRX.CONF_SENTSEG);
        if (!confFilePath.toFile().exists()) {
            LOGGER.error("File is not found!");
            System.exit(1);
        }

        ValidationResult validationResult = checkFile(confFilePath);
        if (!validationResult.isValid()) {
            LOGGER.error(validationResult.getErrorMessage());
            System.exit(1);
        }

        try {
            Path srxFilePath = Paths.get(".").resolve(SRX.SRX_SENTSEG);
            if (srxFilePath.toFile().exists()) {
                Files.delete(srxFilePath);
            }
            SRX srx = SRX.loadConfFile(confFilePath.toFile(), srxFilePath.getParent().toFile());
            SRX.saveToSrx(srx, srxFilePath.getParent().toFile());
        } catch (Exception e) {
            LOGGER.error("Error occurred during conversion!", e);
            System.exit(1);
        }
    }

    static ValidationResult checkFile(Path path) {
        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        xmlInputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        xmlInputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        try (FileInputStream xmlStream = new FileInputStream(path.toFile())) {
             XMLEventReader reader = xmlInputFactory.createXMLEventReader(xmlStream);

             while (reader.hasNext()) {
                 XMLEvent event = reader.nextEvent();
                 if (event.isStartElement()) {
                     StartElement element = event.asStartElement();
                     String tagname = element.getName().getLocalPart();
                     if (!ALLOWED_TAGS.contains(tagname)) {
                         return ValidationResult.failure("Unexpected element: " + tagname);
                     }
                     Iterator<Attribute> attributes = element.getAttributes();
                     while (attributes.hasNext()) {
                         Attribute attr = attributes.next();
                         String attrName = attr.getName().getLocalPart();
                         String attrValue = attr.getValue();
                         if (!ALLOWED_ATTRIBUTES.getOrDefault(tagname, Set.of()).contains(attrName)) {
                             return ValidationResult.failure("Unexpected attribute: " + tagname + " " + attrName);
                         }
                         if (IGNORE_VALUE_TAGS.contains(attrName)) {
                             continue;
                         }
                         if (!ALLOWED_ATTRIBUTE_VALUES.getOrDefault(attrName, Set.of()).contains(attrValue)) {
                             return ValidationResult.failure("Unexpected attribute value: " + tagname + " "
                                     + attrName + " " + attrValue);
                         }
                     }
                 }
             }
            return ValidationResult.success();
        } catch (IOException ex) {
            LOGGER.error("Error occurred during file processing!", ex);
            return ValidationResult.failure("Error occurred during file processing!");
        } catch (IllegalStateException | XMLStreamException e) {
            return ValidationResult.failure(e.getMessage());
        }
    }
}
