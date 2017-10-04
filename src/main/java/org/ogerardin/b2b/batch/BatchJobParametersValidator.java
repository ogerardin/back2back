package org.ogerardin.b2b.batch;

import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.job.DefaultJobParametersValidator;

import java.util.Map;

class BatchJobParametersValidator extends DefaultJobParametersValidator {

    private final Map<String, Object> staticKeys;

    public BatchJobParametersValidator(String[] requiredKeys, String[] optionalKeys, Map<String, Object> staticKeys) {
        super(requiredKeys, optionalKeys);
        this.staticKeys = staticKeys;
    }

    @Override
    public void validate(JobParameters parameters) throws JobParametersInvalidException {
        super.validate(parameters);
        validateStatickeys(parameters);
    }

    private void validateStatickeys(JobParameters parameters) throws JobParametersInvalidException {
        for (Map.Entry<String, Object> entry : staticKeys.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (! parameters.getString(key).equals(value)) {
                throw new JobParametersInvalidException("value for key '" + key + "' must be '" + value + "'");
            }
        }
    }
}
