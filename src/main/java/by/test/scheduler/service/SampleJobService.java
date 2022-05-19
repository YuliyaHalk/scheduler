package by.test.scheduler.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jobrunr.jobs.annotations.Job;
import org.jobrunr.storage.StorageProvider;
import org.springframework.stereotype.Service;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
@Slf4j
@Service
@RequiredArgsConstructor
public class SampleJobService {

    private final StorageProvider storageProvider;

    @Job(name = "The sample job with name %0")
    public void execute(String name) {
        log.info("The job {} has started", name);
        executeJob(name);
        log.info("The job {} has finished", name);
    }

    public void deleteJobClass(String name) {
        File file = new File("/java/test/" + name + ".java");
        file.deleteOnExit();
        File classFile = new File("/java/test/" + name + ".class");
        classFile.deleteOnExit();
    }

    public boolean createJobClass(String classCode, String name) {
        File root = new File("/java");
        File sourceFile = new File(root, name + ".java");
        try {
            Files.write(sourceFile.toPath(), classCode.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            log.error("Error during creating job file");
        }

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        int result = compiler.run(null, null, null, sourceFile.getPath());
        return result == 0;
    }

    private void executeJob(String jobType) {
        File root = new File("/java");
        try {
            URLClassLoader classLoader = URLClassLoader.newInstance(new URL[] { root.toURI().toURL() });
            Class<?> cls = Class.forName("test." + jobType, true, classLoader);
            Object instance = cls.newInstance();
            cls.getMethod("execute", null).invoke(instance);
        } catch (MalformedURLException | InvocationTargetException | InstantiationException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            log.error("No class with name {}.java exists", jobType);
        } catch (IllegalAccessException e) {
            log.error("Method execute should be public");
        } catch (NoSuchMethodException e) {
            log.error("Method execute wasn't found in class {}.java", jobType);
        }
    }

}