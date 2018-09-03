package org.ogerardin.b2b.update;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.ogerardin.b2b.update.channel.GithubReleaseChannel;

import java.util.Arrays;

@Slf4j
class GithubReleaseChannelTest {

    @Test
    void getReleases() {

        GithubReleaseChannel channel = new GithubReleaseChannel("edvin", "fxlauncher");
        log.debug("Getting releases from {}", channel);

        Release[] releases = channel.getReleases();

        Arrays.stream(releases)
            .forEach(release -> log.debug(release.getVersion()));
    }
}