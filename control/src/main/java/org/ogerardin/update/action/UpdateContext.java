package org.ogerardin.update.action;

import lombok.Data;

import java.nio.file.Path;
import java.nio.file.Paths;

@Data
public class UpdateContext {

    private Path sourceDir;
    private Path targetDir;

    public UpdateContext(String[] args) {
        this.sourceDir = Paths.get(System.getProperty("source.dir"));
        this.targetDir = Paths.get(System.getProperty("target.dir"));

        parseArgs(args);
    }

    private void parseArgs(String[] args) {
        //TODO extract additional context from given args
    }
}
