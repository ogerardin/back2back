package org.ogerardin.update;

import lombok.Data;

import java.nio.file.Path;
import java.nio.file.Paths;

@Data
public class UpdateContext {

    public static final String SOURCE_DIR_PROPERTY = "source.dir";
    public static final String TARGET_DIR_PROPERTY = "target.dir";

    private Path sourceDir;
    private Path targetDir;

    public UpdateContext(String[] args) {
        parseSystemProperties();
        parseArgs(args);
    }

    private void parseSystemProperties() {
        this.sourceDir = Paths.get(System.getProperty(SOURCE_DIR_PROPERTY));
        this.targetDir = Paths.get(System.getProperty(TARGET_DIR_PROPERTY));
    }

    public UpdateContext() {
        parseSystemProperties();
    }

    private void parseArgs(String[] args) {
        //TODO extract additional context from given args
    }
}
