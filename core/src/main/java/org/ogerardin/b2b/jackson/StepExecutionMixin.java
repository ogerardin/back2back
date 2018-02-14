package org.ogerardin.b2b.jackson;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.batch.core.JobExecution;

public abstract class StepExecutionMixin  {
    // ignore jobExecution to avoir circular reference
    @JsonIgnore
    JobExecution jobExecution;

}
