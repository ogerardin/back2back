package org.ogerardin.update;

import java.net.URL;

public interface Release {

    String getVersion();

    URL getZipDownloadUrl();

    String getDescription();

}
