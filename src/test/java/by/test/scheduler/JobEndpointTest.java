package by.test.scheduler;

import org.jobrunr.jobs.states.StateName;
import org.jobrunr.storage.PageRequest;
import org.jobrunr.storage.StorageProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.DEFINED_PORT;

@SpringBootTest(webEnvironment = DEFINED_PORT)
public class JobEndpointTest {

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    StorageProvider storageProvider;

    @Test
    @DisplayName("Test job enqueued.")
    public void givenEndpoint_whenJobEnqueued_thenJobIsProcessedWithin30Seconds() {
        runJobViaRest("from-test");
        await()
                .atMost(30, TimeUnit.SECONDS)
                .until(() -> storageProvider.getJobs(StateName.SUCCEEDED, PageRequest.ascOnUpdatedAt(1)).size() == 1);
    }

    @Test
    @DisplayName("Test job scheduled.")
    public void givenEndpoint_whenJobScheduled_thenJobIsScheduled() {
        String response = scheduleJobViaRest("from-test", Duration.ofHours(3));
        assertTrue(response.contains("jobSignature"));

        await()
                .atMost(30, TimeUnit.SECONDS)
                .until(() -> storageProvider.getJobs(StateName.SCHEDULED, PageRequest.ascOnUpdatedAt(1)).size() == 1);
    }

    private String runJobViaRest(String input) {
        return restTemplate.getForObject(
                "http://localhost:8080/run-once?name=" + input,
                String.class);
    }

    private String scheduleJobViaRest(String input, Duration duration) {
        return restTemplate.getForObject(
                "http://localhost:8080/schedule?name=" + input
                        + "&when=" + duration.toString(),
                String.class);
    }
}
