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
public class GithubTagChannel extends GithubChannel implements ReleaseChannel {

    public GithubTagChannel(String owner, String repo) {
        super(owner, repo);
    }

    @Override
    public Release[] getReleases() {

        URL baseUrl = getBaseUrl();

        Release[] result = restTemplate.getForObject(baseUrl.toString(), GithubTag[].class);

        return result;
    }

    private URL getBaseUrl()  {
        try {
            return new URL("https", API_HOST,
                    String.format("/repos/%s/%s/tags", getOwner(), getRepo()));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GithubTag implements Release {
        String name;
        String zipball_url;

        @Override
        public String getVersion() {
            return getName();
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
            return "Tag " + name;
        }
    }
}
