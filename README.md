# Segmentation.conf to segmentation.srx migration tool

This is a small utility to migrate OmegaT segmentation configuration file format from `segmentation.conf` to new standard `segmentation.srx`.

## WARNING

The utility is utilizing a vulnerable Java class, `java.beans.XMLDecoder`, which introduces significant security risks.
Attackers can potentially exploit this vulnerability by crafting a malicious `segmentation.conf` file.

This file could:

1. **Upload Arbitrary Files to the Internet**: The attacker could use the vulnerability to leak sensitive information by uploading files from the local system to the internet.

2. **Execute Arbitrary Commands**: By injecting specially crafted commands into the `segmentation.conf` file, attackers could execute malicious code on the system, potentially compromising the entire environment.

This vulnerability arises because `java.beans.XMLDecoder` does not sufficiently validate or sanitize untrusted input, making it susceptible to deserialization attacks.
It is strongly recommended to avoid using this utility with untrusted data to process configuration files.

The migration tool checks a given file with allowlist of the class names, the property names, and the method names that are acceptable for the `segmentation.conf` a Java object graph file.
Even though checks, there is no perfect for protecting, so please don't use the tool against untrusted segmentation rules file.

## Usage

### With fatJar file

```generic
cd ~/.omegat/
java -jar ~/Downloads/omegat-segmentation-migrator-fat.jar
```

### With zip distribution

```Generic
cd ~/Downloads/
unzip omegat-segmentation-migrator.zip 
cd ~/.omegat/
~/Downloads/omegat-segmentation-migrator/bin/omegat-segmentation-migrator.bat
```

Once you got `segmentation.srx` file, OmegaT 6.1 Beta and 6.0.x will use new standard segmentation configuration file.

## License

GPL-3
