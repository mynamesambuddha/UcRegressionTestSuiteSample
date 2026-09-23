package uc.mainframe.migration.regression.common;

import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class BuildIdGenerator {

    private static final int BUILD_NUMBER_LENGTH = 5;

    private final Set<String> generatedBuildIds =
            ConcurrentHashMap.newKeySet();

    public String generateBuildId(String systemPrefix) {
        String buildId;

        do {
            buildId = systemPrefix.toUpperCase(Locale.ROOT)
                    + "_"
                    + generateBuildNumber();
        } while (!generatedBuildIds.add(buildId));

        return buildId;
    }

    private String generateBuildNumber() {
        return UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, BUILD_NUMBER_LENGTH)
                .toUpperCase();
    }
}