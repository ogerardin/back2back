package org.ogerardin.processcontrol;

import java.nio.file.Path;

/**
 * A specialization of {@link NativeProcessController} that deals specifically with executing Java code.
 */
public enum JavaProcessControllerHelper {
    ;

    public static NativeProcessController buildJarProcessController(Path jarFile) {
        return NativeProcessController.builder()
                .command(buildJavaCommand(jarFile))
                .build();
    }

    private static String[] buildJavaCommand(Path jarFile) {
        return new String[] {
                "java",
                "-jar", jarFile.toAbsolutePath().toString()
        };
    }
}
