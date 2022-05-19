# scheduler
The app allows to create custom job types and run or schedule job execution.

Dashboard is accessible by url http://localhost:8000/dashboard/overview on local env

To create custom job use
POST /create-job 
  request body consist of job name and job class content.
  Example: 
    {
     "name": "First",
     "content": "package test; public class First { static { System.out.println(\"hello\"); } public void execute() { System.out.println(\"world\"); } "
    }
    IMPORTANT: name of job and class name should be the same. Class should contain public void execute() method
    
To run job
GET /run-once param:name(job name) 

To schedule job
GET /schedule param:name(job name), when(duration as string. f.e. "PT1H")

To schedule reccurent job execution
GET /schedule-recurrently param:id(job id, not required), name(job name), cron(cron expression as string. f.e. "0 * * * *")

To remove reccurently runned job 
DELETE /remove-job param:id(job id), name(job name)

Threads count can be changed in application.properties file 
org.jobrunr.background-job-server.worker-count=3
