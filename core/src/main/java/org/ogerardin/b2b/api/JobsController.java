package org.ogerardin.b2b.api;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/jobs")
public class JobsController {

    @Autowired
    JobExplorer jobExplorer;

    @GetMapping
    public List<String> names() {
        return jobExplorer.getJobNames().stream()
                .distinct()
                .collect(Collectors.toList());
    }

    @GetMapping("/{name}/running")
    public Set<JobExecution> running(@PathVariable String name) {
        return jobExplorer.findRunningJobExecutions(name);
    }

    @GetMapping("/{name}")
    public List<JobInstance> instances(@PathVariable String name,
                                       @RequestParam(required = false, defaultValue = "0") int start,
                                       @RequestParam(required = false, defaultValue = "100") int count
                                       ) {
        List<JobInstance> jobInstances = jobExplorer.getJobInstances(name, start, count);
        return jobInstances;
    }

    @GetMapping("/{name}/executions")
    public List<JobExecution> executions(@PathVariable String name,
                                         @RequestParam(required = false, defaultValue = "0") int start,
                                         @RequestParam(required = false, defaultValue = "100") int count
                                         ) {
        List<JobInstance> jobInstances = jobExplorer.getJobInstances(name, start, count);

        List<JobExecution> jobExecutions = new ArrayList<>();
        for (JobInstance jobInstance : jobInstances) {
            jobExecutions.addAll(jobExplorer.getJobExecutions(jobInstance));
        }

        return jobExecutions;
    }



}
