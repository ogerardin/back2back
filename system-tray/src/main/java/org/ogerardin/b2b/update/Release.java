package org.ogerardin.b2b.update;

import lombok.Data;

import java.net.URL;

@Data
public class Release {

    String version;

    URL zipUrl;
}
