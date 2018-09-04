package org.ogerardin.update;

import org.apache.maven.artifact.versioning.ComparableVersion;

import java.util.Comparator;

/**
 * Compares version strings using Maven semantics, see
 * http://maven.apache.org/ref/current/maven-artifact/apidocs/org/apache/maven/artifact/versioning/ComparableVersion.html
 */
public class MavenVersionComparator implements Comparator<String> {

    @Override
    public int compare(String o1, String o2) {
        ComparableVersion v1 = new ComparableVersion(o1);
        ComparableVersion v2 = new ComparableVersion(o2);
        int r = v1.compareTo(v2);
        return r;
    }
}
