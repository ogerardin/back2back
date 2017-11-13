package org.ogerardin.b2b.batch.jobs;

import org.ogerardin.b2b.batch.StaticJobParameterValidator;
import org.springframework.batch.core.JobParametersValidator;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.CompositeJobParametersValidator;
import org.springframework.batch.core.job.DefaultJobParametersValidator;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

/**
 * Common superclass for backup jobs.
 */
public abstract class BackupJob {

    @Autowired
    protected JobBuilderFactory jobBuilderFactory;

    @Autowired
    protected StepBuilderFactory stepBuilderFactory;

    private List<JobParametersValidator> validators = new ArrayList<>();

    protected BackupJob() {
        addMandatoryParameter("backupset.id");
    }

    protected JobParametersValidator validator() {
        CompositeJobParametersValidator validator = new CompositeJobParametersValidator();
        validator.setValidators(this.validators);
        return validator;
    }


    protected void addMandatoryParameter(String name) {
        validators.add(new DefaultJobParametersValidator(
                new String[]{name},
                new String[]{}));
    }

    protected void addStaticParameter(String name, String value) {
        validators.add(new StaticJobParameterValidator(name, value));
    }

}
