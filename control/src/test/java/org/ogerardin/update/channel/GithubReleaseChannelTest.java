package org.ogerardin.update.channel;

import lombok.extern.slf4j.Slf4j;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.ogerardin.update.Release;

import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;

@Slf4j
class GithubReleaseChannelTest {

    @Test
    void getReleases1() {

        GithubReleaseChannel channel = new GithubReleaseChannel("edvin", "fxlauncher");
        log.debug("Getting releases from {}", channel);

        Release[] releases = channel.getReleases();
        Arrays.stream(releases).forEach(r -> log.debug("release {}", r));

        assertThat(releases.length, Matchers.greaterThan(0));
    }

    @Test
    void getReleases2() {

        GithubReleaseChannel channel = new GithubReleaseChannel("ogerardin", "back2back");
        log.debug("Getting releases from {}", channel);

        Release[] releases = channel.getReleases();
        Arrays.stream(releases).forEach(r -> log.debug("release {}", r));

//        assertThat(releases.length, Matchers.greaterThan(0));
    }
}