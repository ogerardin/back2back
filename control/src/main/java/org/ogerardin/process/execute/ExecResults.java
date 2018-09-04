package org.ogerardin.process.execute;

import lombok.Data;

import java.util.List;

@Data
public class ExecResults {
    private final Process process;
    private final List<String> outputLines;
    private final List<String> errorLines;

    public int getExitValue() {
        return process.exitValue();
    }
}
