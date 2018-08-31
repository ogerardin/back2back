package org.ogerardin.b2b.update;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Arrays;
import java.util.Comparator;

@Data
@AllArgsConstructor
public class UpdateManager {

    public static final MavenVersionComparator DEFAULT_VERSION_COMPARATOR = new MavenVersionComparator();

    private final ReleaseChannel releaseChannel;

    private final String currentVersion;

    private Comparator<String> versionComparator = DEFAULT_VERSION_COMPARATOR;

    public UpdateManager(ReleaseChannel releaseChannel, String currentVersion) {
        this(releaseChannel, currentVersion, DEFAULT_VERSION_COMPARATOR);
    }

    public Release getAvailableUpdate() {
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

    private int compareVersion(Release r1, Release r2) {
        return versionComparator.compare(r1.getVersion(), r2.getVersion());
    }
}
