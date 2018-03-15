package org.ogerardin.b2b.api;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/jobs")
public class JobsController {

    @Autowired
    JobExplorer jobExplorer;

    @GetMapping
    public List<String> names() {
        return jobExplorer.getJobNames();
    }

    @GetMapping("/{name}/running")
    public Set<JobExecution> running(@PathVariable String name) {
        return jobExplorer.findRunningJobExecutions(name);
    }

    @GetMapping("/{name}/instances")
    public List<JobInstance> instances(@PathVariable String name) {
        List<JobInstance> jobInstances = jobExplorer.getJobInstances(name, 0, 100);
        return jobInstances;
    }

    @GetMapping("/{name}/executions")
    public List<JobExecution> executions(@PathVariable String name) {
        List<JobInstance> jobInstances = jobExplorer.getJobInstances(name, 0, 100);

        List<JobExecution> jobExecutions = new ArrayList<>();
        for (JobInstance jobInstance : jobInstances) {
            jobExecutions.addAll(jobExplorer.getJobExecutions(jobInstance));
        }

        return jobExecutions;
    }

}
