package org.ogerardin.update.channel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.ogerardin.update.Release;
import org.ogerardin.update.ReleaseChannel;

import java.net.MalformedURLException;
import java.net.URL;

@Data
@EqualsAndHashCode(callSuper = true)
public class GithubReleaseChannel extends GithubChannel implements ReleaseChannel {

    public GithubReleaseChannel(String owner, String repo) {
        super(owner, repo);
    }

    @Override
    public Release[] getReleases() {

        URL baseUrl = getBaseUrl();

        Release[] result = restTemplate.getForObject(baseUrl.toString(), GithubRelease[].class);

        return result;
    }

    private URL getBaseUrl()  {
        try {
            return new URL("https", API_HOST,
                    String.format("/repos/%s/%s/releases", getOwner(), getRepo()));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GithubRelease implements Release {
        String id;
        String tag_name;
        String zipball_url;
        String body;

        @Override
        public String getVersion() {
            return getTag_name();
        }

        @Override
        public URL getZipDownloadUrl() {
            try {
                return new URL(getZipball_url());
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public String getDescription() {
            return getBody();
        }
    }
}
