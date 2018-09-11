package org.ogerardin.update;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.ogerardin.process.execute.ExecResults;
import org.ogerardin.process.execute.JavaCommandLine;
import org.ogerardin.process.execute.ProcessExecutor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;

@Data
@AllArgsConstructor
@Builder
@ToString
@Slf4j
public class UpdateManager {

    private static final Comparator<String> DEFAULT_VERSION_COMPARATOR = new MavenVersionComparator();
    private static final Unzipper UNZIPPER = new Unzipper();
    private static final Downloader DOWNLOADER = new Downloader();

    @Builder.Default
    private final Comparator<String> versionComparator = DEFAULT_VERSION_COMPARATOR;
    @NonNull
    private final ReleaseChannel releaseChannel;
    @NonNull
    private final String currentVersion;
    /** The home directory of the current installation to be updated */
    private final Path homeDir;
    /** The subdirectory used to extract the downloaded archive */
    @Builder.Default
    private String extractDir = "extracted";
    /** the name of the jar file that contains the updater */
    @Builder.Default
    private String updaterJar = "updater.jar";

    /**
     * Queries the configured {@link #releaseChannel} to find if there is a newer version than the current version.
     * Version comparison is performed using the configured {@link #versionComparator}
     *
     * @return the {@link Release} with the highest version superior to the current version, null if there is none.
     */
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

    /**
     * Attenpts to update the current installation in {@link #homeDir} with the specified {@link Release}.
     * The steps are:
     *  1) the release archive is downloaded to a temporary folder
     *  2) the archive is then extracted into a subfolder ({@link #extractDir}, default "extracted")
     *  3) The updater jar is invoked (name {@link #updaterJar}, default "updater.jar)
     */
    public void update(Release release) throws IOException, UpdateException, InterruptedException {
        // download zip
        Path downloadDir = Files.createTempDirectory(this.getClass().getSimpleName());
        Path downloadedFile = DOWNLOADER.downloadFile(release.getZipDownloadUrl(), downloadDir);

        // unzip
        Path extractDir = downloadDir.resolve(this.extractDir);
        UNZIPPER.unzipFile(downloadedFile, extractDir);

        // invoke updater jar
        Path updater = extractDir.resolve(updaterJar);
        if (! Files.exists(updater)) {
            throw new UpdateException("updater not found in downloaded artefact " + downloadedFile.getFileName());
        }

        // the update context is passed as Java system properties on the command line
        String extractDirValue = extractDir.normalize().toAbsolutePath().toString();
        String homeDirValue = homeDir.normalize().toAbsolutePath().toString();
        String[] updateCommand = JavaCommandLine.builder()
                .jarFile(updater)
                .property(new ImmutablePair<>(UpdateContext.SOURCE_DIR_PROPERTY, extractDirValue))
                .property(new ImmutablePair<>(UpdateContext.TARGET_DIR_PROPERTY, homeDirValue))
                .build()
                .getCommand();

        ProcessExecutor processExecutor = ProcessExecutor.builder()
                .cmdarray(updateCommand)
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

