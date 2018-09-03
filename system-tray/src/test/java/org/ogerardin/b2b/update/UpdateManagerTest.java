package org.ogerardin.b2b.update;

import org.junit.Test;
import org.ogerardin.b2b.update.channel.GithubReleaseChannel;

import java.util.Comparator;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class UpdateManagerTest {

    @Test
    public void getAvailableUpdateGithub() {
        GithubReleaseChannel channel = new GithubReleaseChannel("edvin", "fxlauncher");

        String currentVersion = "v1.0.19";
        UpdateManager updateManager = new UpdateManager(channel, currentVersion);

        Release availableUpdate = updateManager.checkForUpdate();

        Comparator<String> versionComparator = updateManager.getVersionComparator();

        assertTrue(versionComparator.compare(currentVersion, availableUpdate.getVersion()) < 0);
    }

    @Test
    public void getAvailableUpdateMock() {
        Release r1 = mock(Release.class);
        when(r1.getVersion()).thenReturn("1.1.0");

        Release r2 = mock(Release.class);
        when(r2.getVersion()).thenReturn("1.2.0");

        Release r3 = mock(Release.class);
        when(r3.getVersion()).thenReturn("1.3.0");

        ReleaseChannel mockedChannel = mock(ReleaseChannel.class);
        when(mockedChannel.getReleases()).thenReturn(new Release[] { r1, r2, r3});

        UpdateManager updateManager = new UpdateManager(mockedChannel, "1.2.1");

        Release availableUpdate = updateManager.checkForUpdate();

        assertThat(availableUpdate.getVersion(), is(r3.getVersion()));
    }
}