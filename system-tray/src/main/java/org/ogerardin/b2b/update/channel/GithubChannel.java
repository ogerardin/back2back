package org.ogerardin.b2b.update.channel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.web.client.RestTemplate;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
public class GithubChannel extends AbstractHttpChannel {

    public static final String API_HOST = "api.github.com";

    protected final String owner;
    protected final String repo;
    protected final RestTemplate restTemplate;

    @Override
    protected String getApiHost() {
        return GithubChannel.API_HOST;
    }
}
