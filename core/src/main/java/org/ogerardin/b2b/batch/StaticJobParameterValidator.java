package org.ogerardin.b2b.batch;

import lombok.NonNull;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.JobParametersValidator;

import java.text.MessageFormat;
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
            throw new JobParametersInvalidException(
                    MessageFormat.format("Value for key ''{0}'' must be ''{1}'', but was ''{2}''",
                            name, expectedValue, actualValue));
        }
    }
}
