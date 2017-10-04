package org.ogerardin.b2b.batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.support.PassThroughItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

//@Configuration
public class DummyBackupJobProvider extends BackupJobBase {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;
    @Autowired
    private StepBuilderFactory stepBuilderFactory;


    @Bean
    @Qualifier("dummy")
    protected Job dummyBackupJob(Step fileProcessingStep)
            throws IOException {
        return jobBuilderFactory.get(DummyBackupJobProvider.class.getSimpleName())
                .incrementer(new RunIdIncrementer())
                .flow(fileProcessingStep)
                .end()
                .build();
    }

    @Bean
    protected Step fileProcessingStep(ItemProcessor<Path, Path> itemProcessor, ItemReader<Path> reader) throws IOException {
        return stepBuilderFactory.get("dummyStep")
                .<Path, Path>chunk(10)
                .reader(reader)
                .processor(itemProcessor)
//                .writer(newWriter())
                .build();
    }

    @Bean
    protected ItemProcessor<Path, Path> itemProcessor() {
        //TODO do something useful
        return new PassThroughItemProcessor<>();
    }

    @Bean
    @StepScope
    protected ItemReader<Path> reader() throws IOException, URISyntaxException {
        URL url = getClass().getResource("/");
        URI uri = url.toURI();
        Path path = Paths.get(uri);
        File file = new File(uri);
        return new FileTreeWalkerItemReader(path);
    }

}
