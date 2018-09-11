package org.ogerardin.process.execute;

import lombok.Builder;
import lombok.Singular;
import lombok.ToString;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Constructs a String[] suitable for invoking the java command with {@link Runtime#exec(String[])}.
 */
@ToString
@Builder
public class JavaCommandLine {

    private static final String PATH_SEPARATOR = System.getProperty("path.separator");

    @Builder.Default
    private String javaCommand = "java";
    private String className;
    private Path jarFile;
    @Builder.Default
    private List<ImmutablePair<String,String>> properties = new ArrayList<>();
    @Builder.Default
    private List<Path> classSearchPath = new ArrayList<>();
    @Singular
    private List<String> args;

    public String[] getCommand() {
        if (className == null && jarFile == null) {
            throw new IllegalStateException("className or jarFile must be used");
        }
        if (className != null && jarFile != null) {
            throw new IllegalStateException("className and jarFile can't be used together");
        }

        String[] cmdArray = {javaCommand};
        if (jarFile != null) {
            cmdArray = ArrayUtils.addAll(cmdArray,"-jar", jarFile.toString());
        } else {
            cmdArray = ArrayUtils.addAll(cmdArray,className);
        }

        // append classpath (if specified)
        if (! classSearchPath.isEmpty()) {
            //TODO handle quoting
            String cp = classSearchPath.stream()
                    .map(Path::toString)
                    .collect(Collectors.joining(PATH_SEPARATOR));
            cmdArray = ArrayUtils.addAll(cmdArray, "-cp", cp);
        }

        // append system properties
        for (ImmutablePair<String, String> property : properties) {
            String def = String.format("-D%s=%s", property.left, property.right);
            cmdArray = ArrayUtils.addAll(cmdArray, def);
        }

        // append args
        cmdArray = ArrayUtils.addAll(cmdArray, args.toArray(new String[0]));

        return cmdArray;
    }


    // Lombok generates this builder automatically, we just want to provide some shortcuts
    public static class JavaCommandLineBuilder {
        public JavaCommandLineBuilder property(String property, String value) {
            if (properties == null) {
                properties = new ArrayList<>();
            }
            properties.add(new ImmutablePair<>(property, value));
            return this;
        }
        public JavaCommandLineBuilder classpathItem(Path path) {
            if (classSearchPath == null) {
                classSearchPath = new ArrayList<>();
            }
            classSearchPath.add(path);
            return this;
        }
        public JavaCommandLineBuilder classpathItem(String item) {
            return classpathItem(Paths.get(item));
        }
    }
}
