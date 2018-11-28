package org.ogerardin.b2b.batch.jobs;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.ogerardin.b2b.batch.jobs.support.LocalFileInfo;
import org.ogerardin.b2b.hash.HashProvider;
import org.springframework.batch.item.ItemProcessor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;


@Data
@Slf4j
class HashingItemProcessor implements ItemProcessor<LocalFileInfo, LocalFileInfo> {

    private final HashProvider hashProvider;

    public HashingItemProcessor(HashProvider hashProvider) {
        this.hashProvider = hashProvider;
    }

    @Override
    public LocalFileInfo process(LocalFileInfo item) {
        Path file = item.getPath();
        try {
            String computedHash = hashProvider.hexHash(Files.newInputStream(file));
            item.getHashes().put(hashProvider.name(), computedHash);
        } catch (IOException e) {
            log.error("Failed to compute hash for " + file, e);
            return null;
        }
    return item;
    }

}
