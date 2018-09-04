package org.ogerardin.update;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.ogerardin.process.execute.ProcessExecutor;
import org.ogerardin.process.control.ControlHelper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;

@Data
@AllArgsConstructor
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

        ProcessExecutor processExecutor = ProcessExecutor.builder()
                .cmdarray(ControlHelper.buildJavaCommand(updater))
                .build();
        processExecutor.performExec();

    }

    private int compareVersion(Release r1, Release r2) {
        return versionComparator.compare(r1.getVersion(), r2.getVersion());
    }
}
