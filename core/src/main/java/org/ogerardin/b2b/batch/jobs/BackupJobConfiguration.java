package org.ogerardin.b2b.batch.jobs;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.ogerardin.b2b.B2BProperties;
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
 * Common superclass for backup job configuration beans
 */
public abstract class BackupJobConfiguration {

    protected static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
    protected JobBuilderFactory jobBuilderFactory;

    @Autowired
    protected StepBuilderFactory stepBuilderFactory;

    @Autowired
    protected B2BProperties properties;

    private List<JobParametersValidator> validators = new ArrayList<>();

    protected BackupJobConfiguration() {
        addMandatoryParameter("backupset.id");
    }

    JobParametersValidator getValidator() {
        CompositeJobParametersValidator validator = new CompositeJobParametersValidator();
        validator.setValidators(this.validators);
        return validator;
    }


    /**
     * Add a validator for this job configuration that requires the presence of a given parameter (with any value)
     */
    void addMandatoryParameter(String name) {
        validators.add(new DefaultJobParametersValidator(
                new String[]{name},
                new String[]{}));
    }

    /**
     * Add a validator for this job configuration that requires the presence of a given parameter with a given value
     */
    void addStaticParameter(String name, String value) {
        validators.add(new StaticJobParameterValidator(name, value));
    }

}
