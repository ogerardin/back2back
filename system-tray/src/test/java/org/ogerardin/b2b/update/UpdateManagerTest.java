package org.ogerardin.b2b.update;

import org.junit.Test;

import static org.junit.Assert.*;

public class UpdateManagerTest {

    @Test
    public void getAvailableUpdate() {
        GithubChannel channel = new GithubChannel("edvin", "fxlauncher");

        UpdateManager updateManager = new UpdateManager(channel, "v1.0.19");

        Release availableUpdate = updateManager.getAvailableUpdate();
    }
}