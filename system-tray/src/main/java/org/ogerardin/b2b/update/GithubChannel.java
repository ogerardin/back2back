package org.ogerardin.b2b.update;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Data
public class GithubChannel implements Channel {

    private final String owner;
    private final String repo;

    @Override
    public List<Release> getReleases() {
        RestTemplate restTemplate = new RestTemplate();
        MappingJackson2HttpMessageConverter jsonConcerter = new MappingJackson2HttpMessageConverter();
        restTemplate.setMessageConverters(Arrays.asList(
                jsonConcerter
        ));

        URL baseUrl = getBaseUrl();

        GithubRelease[] result = restTemplate.getForObject(baseUrl.toString(), GithubRelease[].class);

        return Collections.emptyList();
    }

    private URL getBaseUrl()  {
        try {
            return new URL("https", "api.github.com",
                    String.format("/repos/%s/%s/releases", getOwner(), getRepo()));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GithubRelease {
        String id;
        String tag_name;
        String zipball_url;
    }
}
