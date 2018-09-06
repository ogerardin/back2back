package org.ogerardin.update;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.ogerardin.process.execute.ExecResults;
import org.ogerardin.process.execute.ProcessExecutor;
import org.ogerardin.process.control.ControlHelper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;

@Data
@AllArgsConstructor
@Slf4j
public class UpdateManager {

    private static final Comparator<String> DEFAULT_VERSION_COMPARATOR = new MavenVersionComparator();
    private static final Unzipper UNZIPPER = new Unzipper();

    private final ReleaseChannel releaseChannel;
    private final String currentVersion;
    private Comparator<String> versionComparator = DEFAULT_VERSION_COMPARATOR;

    public UpdateManager(ReleaseChannel releaseChannel, String currentVersion) {
        this(releaseChannel, currentVersion, DEFAULT_VERSION_COMPARATOR);
    }

    public Release checkForUpdate() {
        Release[] releases = releaseChannel.getReleases();

        Release latest = Arrays.stream(releases)
                .max(this::compareVersion)
                .orElse(null);

        if (latest != null && versionComparator.compare(latest.getVersion(), currentVersion) > 0) {
            // we have a more recent version
            return latest;
        }
        return null;
    }

    public void update(Release release) throws IOException, UpdateException, InterruptedException {
        // download zip
        Path tempDirectory = Files.createTempDirectory(this.getClass().getSimpleName());
        Downloader downloader = new Downloader(tempDirectory);

        Path downloadedFile = downloader.download(release.getZipDownloadUrl());

        // unzip
        Path outputFolder = downloadedFile.getParent().resolve("extracted");
        UNZIPPER.unzip(downloadedFile, outputFolder);

        // invoke updater jar
        Path updater = outputFolder.resolve("updater.jar");
        if (! Files.exists(updater)) {
            throw new UpdateException("updater not found in downloaded artefact " + downloadedFile.getFileName());
        }

        // we pass the folder where the artefact has been unzipped as argument; the rest is up to the updater.
        String arg1 = outputFolder.normalize().toAbsolutePath().toString();
        String[] javaCommand = ControlHelper.buildJavaCommand(updater, arg1);
        ProcessExecutor processExecutor = ProcessExecutor.builder()
                .cmdarray(javaCommand)
                .build();
        ExecResults execResults = processExecutor.performExec();

        if (execResults.getExitValue() != 0) {
            throw new UpdateException(String.format("Update failed with status %d", execResults.getExitValue()));
        }

    }

    private int compareVersion(Release r1, Release r2) {
        return versionComparator.compare(r1.getVersion(), r2.getVersion());
    }
}
