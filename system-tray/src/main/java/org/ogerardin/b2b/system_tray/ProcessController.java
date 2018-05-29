package org.ogerardin.b2b.system_tray;

import java.io.IOException;

public interface ProcessController {

    boolean isRunning() throws IOException;

    void stop();

    void start() throws IOException;

    void restart() throws IOException;
}
