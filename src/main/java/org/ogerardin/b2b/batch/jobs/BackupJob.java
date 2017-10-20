package org.ogerardin.b2b.batch.jobs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
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

    private static final Log logger = LogFactory.getLog(BackupJob.class);

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

    private class StaticJobParameterValidator implements JobParametersValidator {
        private final String name;
        private final String expectedValue;

        public StaticJobParameterValidator(String name, String expectedValue) {
            this.name = name;
            this.expectedValue = expectedValue;
        }

        @Override
        public void validate(JobParameters parameters) throws JobParametersInvalidException {
            String actualValue = parameters.getString(name);
            if (!actualValue.equals(expectedValue)) {
                throw new JobParametersInvalidException(String.format("Value for key '%s' must be '%s', but was '%s'", name, expectedValue, actualValue));
            }
        }
    }
}
