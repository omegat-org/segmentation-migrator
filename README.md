# Segmentation.conf to segmentation.srx migration tool

This is a small utility to migrate OmegaT segmentation configuration file format from `segmentation.conf` to new standard `segmentation.srx`.

## WARNING

The tool uses a vulnerable Java class, `java.beans.XMLDecoder`, which introduces significant security risks.
Attackers can potentially exploit this vulnerability by crafting a malicious `segmentation.conf` file.

This file could:

1. **Upload Arbitrary Files to the Internet**: The attacker could use the vulnerability to leak sensitive information by uploading files from the local system to the internet.

2. **Execute Arbitrary Commands**: By injecting specially crafted commands into the `segmentation.conf` file, attackers could execute malicious code on the system, potentially compromising the entire environment.

This vulnerability arises because `java.beans.XMLDecoder` does not sufficiently validate or sanitize untrusted input, making it susceptible to deserialization attacks.
It is strongly recommended to avoid using this utility with untrusted data to process configuration files.

The migration tool checks a given file with allowlist of the class names, the property names, and the method names that are acceptable for the `segmentation.conf` a Java object graph file.
Even though checks, there is no perfect for protecting, so please don't use the tool against untrusted segmentation rules file.

## Usage

### Prerequisites

- **Java Runtime (JRE) 21+** is required to run the tool.  
  If you don’t have Java installed, install a recent JDK/JRE from [Adoptium](https://adoptium.net/).

### Locale matters (important)

OmegaT’s legacy segmentation file (`segmentation.conf`) may be **locale-dependent**.  
To avoid conversion errors, run this migration tool using the **same locale that was used when the `segmentation.conf` file was created/edited**.

If you convert the file under a different locale, the migration can **fail** or produce **unexpected results**.

### Building from source

This project is primarily intended for **OmegaT team administrators / advanced users** (not typical end users).
You build the tool yourself using Gradle.

Requirements:
- **JDK 21+**
- (No separate Gradle install is required; the project includes the Gradle Wrapper.)

Build:
```bash
./gradlew clean build
```

This will produce:
- a **fat JAR** under `build/libs/`
- a **ZIP distribution** under `build/distributions/`

### Run (fat JAR)

Run the fat JAR from the directory that contains your OmegaT configuration (commonly `~/.omegat/` or `<translation project>/omegat/`):

### With fatJar file

```bash
cd ~/.omegat/
java -jar <project dir>/build/libs/omegat-segmentation-migrator-fat.jar
```

### Run (ZIP distribution)

Unpack the distribution ZIP and run the launcher script.

```bash
unzip <project dir>/build/distributions/omegat-segmentation-migrator.zip 
cd ~/.omegat/
<unpacked-dir>/omegat-segmentation-migrator/bin/omegat-segmentation-migrator
```

On Windows, use the `.bat` launcher instead:
```cmd
cd %USERPROFILE%/OmegaT
<unpacked-dir>\bin\omegat-segmentation-migrator.bat
```

### When converting custom rule on the translation project

```bash
cd <translation project>/omegat/
<unpacked-dir>/omegat-segmentation-migrator/bin/omegat-segmentation-migrator
```

### Output

After a successful run, you should have a `segmentation.srx` file.
OmegaT **6.1 Beta** and **6.0.x** can use the new standard segmentation configuration file.

## License

OmegaT segmentation migrator – Tool to migrate complex segmentation rule to SRX. 
Copyright (C) 2025–2026 OmegaT project

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
