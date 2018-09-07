package org.ogerardin.update.jar;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.Attributes;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

public enum JarVersionExtractor {
    ;

    public static String getImplementationVersion(Path jarFile) throws IOException {
        JarInputStream jarStream = new JarInputStream(Files.newInputStream(jarFile));
        Manifest mf = jarStream.getManifest();
        Attributes attributes = mf.getMainAttributes();
        String implVersion = attributes.getValue(Attributes.Name.IMPLEMENTATION_VERSION);
        return implVersion;
    }



}
