package org.ogerardin.b2b.update;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Data
@AllArgsConstructor
public class UpdateManager {

    private static final MavenVersionComparator DEFAULT_VERSION_COMPARATOR = new MavenVersionComparator();

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

    public void update(Release release) throws IOException {
        Path tempDirectory = Files.createTempDirectory(this.getClass().getSimpleName());
        Downloader downloader = new Downloader(tempDirectory);

        Path downloadedFile = downloader.download(release.getZipDownloadUrl());

        ZipFile zipFile = new ZipFile(downloadedFile.toFile());
        Enumeration<? extends ZipEntry> entries = zipFile.entries();


    }

    private int compareVersion(Release r1, Release r2) {
        return versionComparator.compare(r1.getVersion(), r2.getVersion());
    }
}
