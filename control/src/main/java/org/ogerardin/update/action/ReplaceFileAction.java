package org.ogerardin.update.action;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.ogerardin.update.UpdateAction;
import org.ogerardin.update.UpdateException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Data
@AllArgsConstructor
public class ReplaceFileAction implements UpdateAction {

    private final Path file;

    public ReplaceFileAction(String filename) {
        this(Paths.get(filename));
    }

    @Override
    public void perform(UpdateContext context) throws UpdateException {
        Path source = context.getSourceDir().resolve(file);
        Path target = context.getTargetDir().resolve(file);

        if (!Files.exists(target)) {
            throw new UpdateException(String.format("Target file does not exist: %s", target));
        }
        if (!Files.isWritable(target)) {
            throw new UpdateException(String.format("Target file is not writable: %s", target));
        }

        try {
            Files.copy(source, target);
        } catch (IOException e) {
            throw new UpdateException("Exception while performing copy", e);
        }
    }
}
