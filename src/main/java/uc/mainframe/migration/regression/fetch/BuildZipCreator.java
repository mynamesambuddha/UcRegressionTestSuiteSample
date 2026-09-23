package uc.mainframe.migration.regression.fetch;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.springframework.stereotype.Component;

@Component
public class BuildZipCreator {

    public byte[] createZip(Path buildFolder) throws IOException {

        ByteArrayOutputStream byteOutputStream =
                new ByteArrayOutputStream();

        try (ZipOutputStream zipOutputStream =
                     new ZipOutputStream(byteOutputStream);

             Stream<Path> files = Files.list(buildFolder)) {

            for (Path file : files
                    .filter(Files::isRegularFile)
                    .filter(path -> path
                            .getFileName()
                            .toString()
                            .endsWith(".xml"))
                    .toList()) {

                ZipEntry zipEntry =
                        new ZipEntry(
                                file.getFileName().toString());

                zipOutputStream.putNextEntry(zipEntry);
                Files.copy(file, zipOutputStream);
                zipOutputStream.closeEntry();
            }
        }

        return byteOutputStream.toByteArray();
    }
}