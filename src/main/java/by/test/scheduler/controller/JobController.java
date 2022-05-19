package by.test.scheduler.controller;

import by.test.scheduler.dto.ClassRequest;
import by.test.scheduler.service.SampleJobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jobrunr.jobs.Job;
import org.jobrunr.jobs.JobId;
import org.jobrunr.scheduling.JobScheduler;
import org.jobrunr.storage.JobNotFoundException;
import org.jobrunr.storage.StorageProvider;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
public class JobController {

    private final JobScheduler jobScheduler;

    private final SampleJobService sampleJobService;

    private final StorageProvider storageProvider;

    @GetMapping("/run-once")
    public Job runJob(
            @RequestParam(value = "name", defaultValue = "Test") String name) {
        UUID jobId = UUID.randomUUID();
        jobScheduler.enqueue(jobId, () -> sampleJobService.execute(name));
        return storageProvider.getJobById(jobId);
    }

    @GetMapping("/schedule")
    public Job scheduleJob(
            @RequestParam(value = "name") String name,
            @RequestParam(value = "when", defaultValue = "PT1H") String when) {
        JobId jobId = jobScheduler.schedule(Instant.now().plus(Duration.parse(when)), () -> sampleJobService.execute(name));
        return storageProvider.getJobById(jobId);
    }

    @GetMapping("/schedule-recurrently")
    public String scheduleRecurrentlyJob(
            @RequestParam(value = "id", required = false) String id,
            @RequestParam(value = "name") String name,
            @RequestParam(value = "cron", defaultValue = "0 * * * *") String cronExp) {
        if (id == null) return jobScheduler.scheduleRecurrently(cronExp, () -> sampleJobService.execute(name));
        try {
            return jobScheduler.scheduleRecurrently(id, cronExp, () -> sampleJobService.execute(name));
        } catch (JobNotFoundException e) {
            log.error("Job with id {} not found", id);
            return "Job with id " + " not found";
        }
    }

    @PostMapping("/create-job")
    public String createJob(@RequestBody ClassRequest request) {
        boolean created = sampleJobService.createJobClass(request.getContent(), request.getName());
        return created ? "Class with name " + request.getName() + " was created" : "Class creation error";
    }

    @DeleteMapping("/remove-job")
    public String remove(@RequestParam(value = "id") String id,
                         @RequestParam(value = "name") String name) {
        storageProvider.deleteRecurringJob(id);
        sampleJobService.deleteJobClass(name);
        return "Removed job with id " + id;
    }

}