package uc.mainframe.migration.regression.common;

import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.stereotype.Component;

@Component
public class BuildFolderCreator {

    private static final String RESULTS_FOLDER_NAME = "UCResults";

    private final BuildIdGenerator buildIdGenerator;

    public BuildFolderCreator(
            BuildIdGenerator buildIdGenerator) {
        this.buildIdGenerator = buildIdGenerator;
    }

    public Path createBuildFolder(String systemPrefix)
            throws Exception {

        Path resultsFolder = Path.of(
                System.getProperty("user.home"),
                RESULTS_FOLDER_NAME);

        Files.createDirectories(resultsFolder);

        while (true) {
            String buildId =
                    buildIdGenerator.generateBuildId(systemPrefix);

            Path buildFolder =
                    resultsFolder.resolve(buildId);

            try {
                Path createdBuildFolder =
                        Files.createDirectory(buildFolder);

                System.out.println(
                        "Build folder created: "
                                + createdBuildFolder);

                return createdBuildFolder;

            } catch (FileAlreadyExistsException exception) {
               throw new Exception(exception);
            }
        }
    }
}