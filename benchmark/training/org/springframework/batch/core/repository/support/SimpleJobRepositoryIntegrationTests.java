/**
 * Copyright 2008-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.batch.core.repository.support;


import BatchStatus.FAILED;
import java.util.Arrays;
import java.util.Date;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.job.JobSupport;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.step.StepSupport;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;


/**
 * Repository tests using JDBC DAOs (rather than mocks).
 *
 * @author Robert Kasanicky
 * @author Dimitrios Liapis
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/org/springframework/batch/core/repository/dao/sql-dao-test.xml")
public class SimpleJobRepositoryIntegrationTests {
    @Autowired
    private SimpleJobRepository jobRepository;

    private JobSupport job = new JobSupport("SimpleJobRepositoryIntegrationTestsJob");

    private JobParameters jobParameters = new JobParameters();

    /* Create two job executions for same job+parameters tuple. Check both
    executions belong to the same job instance and job.
     */
    @Transactional
    @Test
    public void testCreateAndFind() throws Exception {
        job.setRestartable(true);
        JobParametersBuilder builder = new JobParametersBuilder();
        builder.addString("stringKey", "stringValue").addLong("longKey", 1L).addDouble("doubleKey", 1.1).addDate("dateKey", new Date(1L));
        JobParameters jobParams = builder.toJobParameters();
        JobExecution firstExecution = jobRepository.createJobExecution(job.getName(), jobParams);
        firstExecution.setStartTime(new Date());
        Assert.assertNotNull(firstExecution.getLastUpdated());
        Assert.assertEquals(job.getName(), firstExecution.getJobInstance().getJobName());
        jobRepository.update(firstExecution);
        firstExecution.setEndTime(new Date());
        jobRepository.update(firstExecution);
        JobExecution secondExecution = jobRepository.createJobExecution(job.getName(), jobParams);
        Assert.assertEquals(firstExecution.getJobInstance(), secondExecution.getJobInstance());
        Assert.assertEquals(job.getName(), secondExecution.getJobInstance().getJobName());
    }

    /* Create two job executions for same job+parameters tuple. Check both
    executions belong to the same job instance and job.
     */
    @Transactional
    @Test
    public void testCreateAndFindWithNoStartDate() throws Exception {
        job.setRestartable(true);
        JobExecution firstExecution = jobRepository.createJobExecution(job.getName(), jobParameters);
        firstExecution.setStartTime(new Date(0));
        firstExecution.setEndTime(new Date(1));
        jobRepository.update(firstExecution);
        JobExecution secondExecution = jobRepository.createJobExecution(job.getName(), jobParameters);
        Assert.assertEquals(firstExecution.getJobInstance(), secondExecution.getJobInstance());
        Assert.assertEquals(job.getName(), secondExecution.getJobInstance().getJobName());
    }

    /* Save multiple StepExecutions for the same step and check the returned
    count and last execution are correct.
     */
    @Transactional
    @Test
    public void testGetStepExecutionCountAndLastStepExecution() throws Exception {
        job.setRestartable(true);
        StepSupport step = new StepSupport("restartedStep");
        // first execution
        JobExecution firstJobExec = jobRepository.createJobExecution(job.getName(), jobParameters);
        StepExecution firstStepExec = new StepExecution(step.getName(), firstJobExec);
        jobRepository.add(firstStepExec);
        Assert.assertEquals(1, jobRepository.getStepExecutionCount(firstJobExec.getJobInstance(), step.getName()));
        Assert.assertEquals(firstStepExec, jobRepository.getLastStepExecution(firstJobExec.getJobInstance(), step.getName()));
        // first execution failed
        firstJobExec.setStartTime(new Date(4));
        firstStepExec.setStartTime(new Date(5));
        firstStepExec.setStatus(FAILED);
        firstStepExec.setEndTime(new Date(6));
        jobRepository.update(firstStepExec);
        firstJobExec.setStatus(FAILED);
        firstJobExec.setEndTime(new Date(7));
        jobRepository.update(firstJobExec);
        // second execution
        JobExecution secondJobExec = jobRepository.createJobExecution(job.getName(), jobParameters);
        StepExecution secondStepExec = new StepExecution(step.getName(), secondJobExec);
        jobRepository.add(secondStepExec);
        Assert.assertEquals(2, jobRepository.getStepExecutionCount(secondJobExec.getJobInstance(), step.getName()));
        Assert.assertEquals(secondStepExec, jobRepository.getLastStepExecution(secondJobExec.getJobInstance(), step.getName()));
    }

    /* Save execution context and retrieve it. */
    @Transactional
    @Test
    public void testSaveExecutionContext() throws Exception {
        @SuppressWarnings("serial")
        ExecutionContext ctx = new ExecutionContext() {
            {
                putLong("crashedPosition", 7);
            }
        };
        JobExecution jobExec = jobRepository.createJobExecution(job.getName(), jobParameters);
        jobExec.setStartTime(new Date(0));
        jobExec.setExecutionContext(ctx);
        Step step = new StepSupport("step1");
        StepExecution stepExec = new StepExecution(step.getName(), jobExec);
        stepExec.setExecutionContext(ctx);
        jobRepository.add(stepExec);
        StepExecution retrievedStepExec = jobRepository.getLastStepExecution(jobExec.getJobInstance(), step.getName());
        Assert.assertEquals(stepExec, retrievedStepExec);
        Assert.assertEquals(ctx, retrievedStepExec.getExecutionContext());
        // JobExecution retrievedJobExec =
        // jobRepository.getLastJobExecution(jobExec.getJobInstance());
        // assertEquals(jobExec, retrievedJobExec);
        // assertEquals(ctx, retrievedJobExec.getExecutionContext());
    }

    /* If JobExecution is already running, exception will be thrown in attempt
    to create new execution.
     */
    @Transactional
    @Test
    public void testOnlyOneJobExecutionAllowedRunning() throws Exception {
        job.setRestartable(true);
        JobExecution jobExecution = jobRepository.createJobExecution(job.getName(), jobParameters);
        // simulating a running job execution
        jobExecution.setStartTime(new Date());
        jobRepository.update(jobExecution);
        try {
            jobRepository.createJobExecution(job.getName(), jobParameters);
            Assert.fail();
        } catch (JobExecutionAlreadyRunningException e) {
            // expected
        }
    }

    @Transactional
    @Test
    public void testGetLastJobExecution() throws Exception {
        JobExecution jobExecution = jobRepository.createJobExecution(job.getName(), jobParameters);
        jobExecution.setStatus(FAILED);
        jobExecution.setEndTime(new Date());
        jobRepository.update(jobExecution);
        Thread.sleep(10);
        jobExecution = jobRepository.createJobExecution(job.getName(), jobParameters);
        StepExecution stepExecution = new StepExecution("step1", jobExecution);
        jobRepository.add(stepExecution);
        jobExecution.addStepExecutions(Arrays.asList(stepExecution));
        Assert.assertEquals(jobExecution, jobRepository.getLastJobExecution(job.getName(), jobParameters));
        Assert.assertEquals(stepExecution, jobExecution.getStepExecutions().iterator().next());
    }
}
