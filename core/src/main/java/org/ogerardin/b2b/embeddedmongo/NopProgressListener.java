package org.ogerardin.b2b.embeddedmongo;

import de.flapdoodle.embed.process.io.progress.IProgressListener;

/**
 * A {{@link IProgressListener}} that does nothing
 */
class NopProgressListener implements IProgressListener {
    @Override
    public void progress(String label, int percent) {
    }

    @Override
    public void done(String label) {
    }

    @Override
    public void start(String label) {
    }

    @Override
    public void info(String label, String message) {
    }
}
