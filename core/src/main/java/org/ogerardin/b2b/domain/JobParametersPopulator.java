package org.ogerardin.b2b.domain;

import org.springframework.batch.core.JobParameter;

import java.util.Map;

/**
 * Classes that implement this interface have the ability to provide one or more {@link JobParameter}s to a Spring
 * batch job's parameters.
 */
public interface JobParametersPopulator {

    void populateParams(Map<String, JobParameter> params);
}
