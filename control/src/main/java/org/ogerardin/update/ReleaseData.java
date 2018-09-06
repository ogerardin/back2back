package org.ogerardin.update;

import lombok.Data;

import java.net.URL;

@Data
public class ReleaseData implements Release {
    private final String version;
    private final URL zipDownloadUrl;
    private final String description;
}
