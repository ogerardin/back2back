package org.ogerardin.b2b.update;

import org.junit.jupiter.api.Test;

class GithubReleaseChannelTest {

    @Test
    void getReleases() {
        GithubChannel channel = new GithubChannel("edvin", "fxlauncher");

        channel.getReleases();
    }
}