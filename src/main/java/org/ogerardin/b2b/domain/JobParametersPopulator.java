package org.ogerardin.b2b.domain;

import org.springframework.batch.core.JobParameter;

import java.util.Map;

public interface JobParametersPopulator {

    void populateParams(Map<String, JobParameter> params);
}
