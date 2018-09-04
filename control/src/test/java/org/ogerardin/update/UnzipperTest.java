package org.ogerardin.update;

import lombok.extern.slf4j.Slf4j;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.hamcrest.io.FileMatchers.aFileWithSize;

@Slf4j
class UnzipperTest {

    private Unzipper unzipper = new Unzipper();

    @Test
    void unzip() throws URISyntaxException, IOException {
        Path zipfile = Paths.get(getClass().getResource("/back2back-dist-1.0-SNAPSHOT.zip").toURI());

        Path tempDirectory = Files.createTempDirectory(getClass().getSimpleName());
        log.info("Unzipping to {} ", tempDirectory);

        unzipper.unzip(zipfile, tempDirectory);

        MatcherAssert.assertThat(tempDirectory.resolve("back2back-bundle-repackaged.jar").toFile(),
                aFileWithSize((33321819)));
    }
}