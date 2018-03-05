package org.ogerardin.b2b.jackson;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.batch.core.JobExecution;

/**
 * A Jackson mixin class to enable serialization of {@link org.springframework.batch.core.StepExecution} without
 * triggering a {@link StackOverflowError}
 */
public abstract class StepExecutionMixin  {
    // ignore jobExecution to avoid circular reference
    @JsonIgnore
    JobExecution jobExecution;

}
