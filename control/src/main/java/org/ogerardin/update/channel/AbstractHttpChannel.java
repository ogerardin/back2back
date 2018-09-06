package org.ogerardin.update.channel;

import com.github.markusbernhardt.proxy.ProxySearch;
import lombok.Data;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.net.*;
import java.util.Arrays;
import java.util.List;

@Data
public abstract class AbstractHttpChannel {

    protected final RestTemplate restTemplate;

    public AbstractHttpChannel() {
        this.restTemplate = getDefaultRestTemplate();
    }

    protected static RestTemplate getDefaultRestTemplate() {
        Proxy proxy = detectProxy();

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        if (proxy != null) {
            requestFactory.setProxy(proxy);
        }
        RestTemplate restTemplate = new RestTemplate(requestFactory);

        restTemplate.setMessageConverters(Arrays.asList(
                new MappingJackson2HttpMessageConverter(),
                new StringHttpMessageConverter()
        ));
        return restTemplate;
    }

    private static Proxy detectProxy() {
        ProxySearch proxySearch = ProxySearch.getDefaultProxySearch();
        ProxySelector proxySelector = proxySearch.getProxySelector();
        if (proxySelector != null) {
            try {
                URL url = new URL("https://google.com");
                List<Proxy> proxies = proxySelector.select(url.toURI());
                return proxies.get(0);
            } catch (URISyntaxException | MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

}
