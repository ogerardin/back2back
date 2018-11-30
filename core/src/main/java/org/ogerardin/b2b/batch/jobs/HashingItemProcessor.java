package org.ogerardin.b2b.batch.jobs;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.ogerardin.b2b.batch.jobs.support.LocalFileInfo;
import org.ogerardin.b2b.hash.HashProvider;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;


/**
 * An {@link ItemProcessor} that computes file hash and stores it into the {@link LocalFileInfo} for further use
 */
@Component
@Data
@Slf4j
class HashingItemProcessor implements ItemProcessor<LocalFileInfo, LocalFileInfo> {

    private final HashProvider hashProvider;

    public HashingItemProcessor(@Qualifier("javaMD5Calculator") HashProvider hashProvider) {
        this.hashProvider = hashProvider;
    }

    @Override
    public LocalFileInfo process(@NonNull LocalFileInfo item) {
        Path file = item.getPath();
//        log.debug("hashing {} using {}", file, hashProvider);
        try {
            InputStream inputStream = Files.newInputStream(file);
            String computedHash = hashProvider.hexHash(inputStream);
            item.getHashes().put(hashProvider.name(), computedHash);
        } catch (IOException e) {
            log.error("Failed to compute hash for " + file, e);
            return null;
        }
        return item;
    }

}
