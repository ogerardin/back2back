package org.ogerardin.b2b.batch;

import lombok.NonNull;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.JobParametersValidator;

import java.util.Objects;

/**
 * A {@link JobParametersValidator} that expects a fixed String value for a given parameter.
 */
public class StaticJobParameterValidator implements JobParametersValidator {
    private final String name;
    private final String expectedValue;

    public StaticJobParameterValidator(@NonNull String name, @NonNull String expectedValue) {
        this.name = name;
        this.expectedValue = expectedValue;
    }

    @Override
    public void validate(JobParameters parameters) throws JobParametersInvalidException {
        String actualValue = parameters.getString(name);
        if (!Objects.equals(expectedValue, actualValue)) {
            throw new JobParametersInvalidException(String.format("Value for key '%s' must be '%s', but was '%s'", name, expectedValue, actualValue));
        }
    }
}
