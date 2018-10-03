package org.ogerardin.update;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.ogerardin.update.channel.GithubReleaseChannel;

import java.util.Comparator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class UpdateManagerTest {

    @Test
    // for some reason access to github API from Travis CI gives a 403 error
    @DisabledIfEnvironmentVariable(named = "TRAVIS", matches = "true")
    public void getAvailableUpdateGithub() {
        GithubReleaseChannel channel = new GithubReleaseChannel("edvin", "fxlauncher");

        String currentVersion = "v1.0.0";
        UpdateManager updateManager = UpdateManager.builder()
                .releaseChannel(channel)
                .currentVersion(currentVersion)
                .build();

        Release availableUpdate = updateManager.checkForUpdate();

        assertThat(availableUpdate, not(nullValue()));

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

        UpdateManager updateManager = UpdateManager.builder()
                .releaseChannel(mockedChannel)
                .currentVersion("1.2.1")
                .build();

        Release availableUpdate = updateManager.checkForUpdate();

        assertThat(availableUpdate.getVersion(), is(r3.getVersion()));
    }


}