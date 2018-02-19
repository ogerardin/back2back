package org.ogerardin.b2b.batch.jobs;

import org.ogerardin.b2b.batch.StaticJobParameterValidator;
import org.springframework.batch.core.JobParametersValidator;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.CompositeJobParametersValidator;
import org.springframework.batch.core.job.DefaultJobParametersValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

import java.util.ArrayList;
import java.util.List;

/**
 * Common superclass for backup job configuration beans
 */
public abstract class BackupJobConfiguration {

    @Autowired
    protected JobBuilderFactory jobBuilderFactory;

    @Autowired
    protected StepBuilderFactory stepBuilderFactory;

    private List<JobParametersValidator> validators = new ArrayList<>();

    protected BackupJobConfiguration() {
        addMandatoryParameter("backupset.id");
    }

    protected JobParametersValidator getValidator() {
        CompositeJobParametersValidator validator = new CompositeJobParametersValidator();
        validator.setValidators(this.validators);
        return validator;
    }


    /**
     * Add a validator for this job configuration that requires the presence of a given parameter (with any value)
     */
    protected void addMandatoryParameter(String name) {
        validators.add(new DefaultJobParametersValidator(
                new String[]{name},
                new String[]{}));
    }

    /**
     * Add a validator for this job configuration that requires the presence of a given parameter with a given value
     */
    protected void addStaticParameter(String name, String value) {
        validators.add(new StaticJobParameterValidator(name, value));
    }


    /**
     * Provides a {@link Step} that computes the size of the current backup batch
     */
    @Bean
    @JobScope
    protected Step computeBatchSizeStep(
            ComputeBatchSizeTasklet computeBatchSizeTasklet,
            ComputeBatchSizeExecutionListener computeBatchSizeExecutionListener
    ) {
        return stepBuilderFactory
                .get("computeBatchSizeStep")
                .tasklet(computeBatchSizeTasklet)
                .listener(computeBatchSizeExecutionListener)
                .build();
    }


    /** Provides an instance of {@link ComputeBatchSizeTasklet} for the current job */
    @Bean
    @JobScope
    protected ComputeBatchSizeTasklet computeBatchSizeTasklet(
            BackupJobContext backupJobContext
    ) {
        return new ComputeBatchSizeTasklet(backupJobContext);
    }


}
