package org.ogerardin.update.channel;

import lombok.extern.slf4j.Slf4j;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.ogerardin.update.Release;

import java.net.MalformedURLException;
import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;

@Slf4j
class WebScrapingChannelTest {

    @Test
    void getReleases1() throws MalformedURLException {
        WebScrapingChannel channel = new WebScrapingChannel("https://www.python.org/downloads",
                "//li[span[@class='release-number']]",
                "substring-after(span[@class='release-number']/a, 'Python ')",
                //FIXME this URL is the URL of a page where the actual download link is
                "span[@class='release-download']/a/@href",
                //FIXME the actual description is on the download page...
                "span[@class='release-enhancements']/a"
                );

        Release[] releases = channel.getReleases();
        Arrays.stream(releases).forEach(r -> log.debug("release {}", r));

        assertThat(releases.length, Matchers.greaterThan(0));
    }

    @Test
    void getReleases2() throws MalformedURLException {
        WebScrapingChannel channel = new WebScrapingChannel("https://www.python.org/ftp/python/",
                //FIXME we should have a way to exclude links that are not actual releaes (such as doc, README, ...)
                "//a",
                ".",
                //FIXME this URL is the URL of a page where the actual download link is
                "@href",
                "."
                );

        Release[] releases = channel.getReleases();
        Arrays.stream(releases).forEach(r -> log.debug("release {}", r));

        assertThat(releases.length, Matchers.greaterThan(0));
    }

/*
    public static void main(String[] args) {
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("proxy", 8080));

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setProxy(proxy);

        RestTemplate restTemplate = new RestTemplate(requestFactory);

        restTemplate.setMessageConverters(Arrays.asList(
                new StringHttpMessageConverter()
        ));

        String html = restTemplate.getForObject("https://www.python.org/downloads/", String.class);

    }
*/
}