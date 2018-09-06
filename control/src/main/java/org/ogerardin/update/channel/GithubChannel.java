package org.ogerardin.update.channel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
public class GithubChannel extends AbstractHttpChannel {

    public static final String API_HOST = "api.github.com";

    protected final String owner;
    protected final String repo;

}
