package org.ogerardin.test;

import com.sun.jna.Platform;
import org.junit.Assume;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class PlatformFilter implements TestRule {

    private final int osType;

    public PlatformFilter(int osType) {
        this.osType = osType;
    }

    @Override
    public Statement apply(Statement base, Description description) {
        if (Platform.getOSType() != osType) {
            return new IgnoreStatement();
        }
        return base;
    }

    private static class IgnoreStatement extends Statement {
        IgnoreStatement() {
        }

        @Override
        public void evaluate() {
            Assume.assumeTrue( "Ignored by " + PlatformFilter.class, false );
        }
    }
}
