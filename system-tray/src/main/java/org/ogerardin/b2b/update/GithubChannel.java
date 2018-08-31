package org.ogerardin.b2b.update;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.github.markusbernhardt.proxy.ProxySearch;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.net.*;
import java.util.Arrays;
import java.util.List;

@Data
@AllArgsConstructor
public class GithubChannel implements ReleaseChannel {

    public static final String API_HOST = "api.github.com";
    private final String owner;
    private final String repo;

    private final RestTemplate restTemplate;

    public GithubChannel(String owner, String repo) {
        this(owner, repo, getDefaultRestTemplate());
    }

    private static RestTemplate getDefaultRestTemplate() {
        Proxy proxy = detectProxy();

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        if (proxy != null) {
            requestFactory.setProxy(proxy);
        }
        RestTemplate restTemplate = new RestTemplate(requestFactory);

        MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter();
        restTemplate.setMessageConverters(Arrays.asList(
                jsonConverter
        ));
        return restTemplate;
    }

    private static Proxy detectProxy() {
        ProxySearch proxySearch = ProxySearch.getDefaultProxySearch();
        ProxySelector proxySelector = proxySearch.getProxySelector();
        if (proxySelector != null) {
            try {
                URL url = new URL("https", API_HOST, "/");
                List<Proxy> proxies = proxySelector.select(url.toURI());
                return proxies.get(0);
            } catch (URISyntaxException | MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
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
