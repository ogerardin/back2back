package org.ogerardin.b2b.update;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GithubChannelTest {

    @Test
    void getReleases() {
        GithubChannel channel = new GithubChannel("edvin", "fxlauncher");

        channel.getReleases();
    }
}