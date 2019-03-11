/**
 * Licensed to CRATE Technology GmbH ("Crate") under one or more contributor
 * license agreements.  See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.  Crate licenses
 * this file to you under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.  You may
 * obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * However, if you have executed another commercial license agreement
 * with Crate these terms will supersede the license and you may use the
 * software solely pursuant to the terms of the relevant commercial agreement.
 */
package io.crate.integrationtests;


import ESIntegTestCase.ClusterScope;
import JobsLogService.STATS_ENABLED_SETTING;
import JobsLogService.STATS_JOBS_LOG_SIZE_SETTING;
import io.crate.action.sql.SQLOperations;
import io.crate.action.sql.Session;
import io.crate.execution.engine.collect.stats.JobsLogService;
import io.crate.execution.engine.collect.stats.JobsLogs;
import io.crate.expression.reference.sys.job.JobContextLog;
import io.crate.testing.UseHashJoins;
import io.crate.testing.UseJdbc;
import io.crate.testing.UseRandomizedSchema;
import io.crate.testing.UseSemiJoins;
import org.hamcrest.core.Is;
import org.junit.Test;


// Avoid set session stmt to interfere with tests
// Avoid set session stmt to interfere with tests
// Avoid set session stmt to interfere with tests
@ClusterScope(numDataNodes = 2, numClientNodes = 0, supportsDedicatedMasters = false)
@UseRandomizedSchema(random = false)
@UseSemiJoins(0)
@UseHashJoins(1)
public class JobLogIntegrationTest extends SQLTransportIntegrationTest {
    // SET extra_float_digits = 3 gets added to the jobs_log
    @Test
    @UseJdbc(0)
    public void testJobLogWithEnabledAndDisabledStats() throws Exception {
        String setStmt = "set global transient stats.jobs_log_size=1";
        execute(setStmt);
        // We record the statements in the log **after** we notify the result receivers (see {@link JobsLogsUpdateListener usage).
        // So it might happen that the "set global ..." statement execution is returned to this test but the recording
        // in the log is done AFTER the execution of the below "select name from sys.cluster" statement (because async
        // programming is evil like that). And then this test will fail and people will spend days and days to figure
        // out what's going on.
        // So let's just wait for the "set global ... " statement to be recorded here and then move on with our test.
        assertBusy(() -> {
            boolean setStmtFound = false;
            for (JobsLogService jobsLogService : internalCluster().getDataNodeInstances(.class)) {
                // each node must have received the new jobs_log_size setting change instruction
                assertThat(jobsLogService.jobsLogSize(), is(1));
                JobsLogs jobsLogs = jobsLogService.get();
                Iterator<JobContextLog> iterator = jobsLogs.jobsLog().iterator();
                if (iterator.hasNext()) {
                    if (iterator.next().statement().equalsIgnoreCase(setStmt)) {
                        setStmtFound = true;
                    }
                }
            }
            // at least one node must have the set statement logged
            assertThat(setStmtFound, is(true));
        });
        // Each node can hold only 1 query (the latest one) so in total we should always see 2 queries in
        // the jobs_log. We make sure that we hit both nodes with 2 queries each and then assert that
        // only the latest queries are found in the log.
        for (SQLOperations sqlOperations : internalCluster().getDataNodeInstances(SQLOperations.class)) {
            Session session = sqlOperations.newSystemSession();
            execute("select name from sys.cluster", null, session);
        }
        assertJobLogOnNodesHaveOnlyStatement("select name from sys.cluster");
        for (SQLOperations sqlOperations : internalCluster().getDataNodeInstances(SQLOperations.class)) {
            Session session = sqlOperations.newSystemSession();
            execute("select id from sys.cluster", null, session);
        }
        assertJobLogOnNodesHaveOnlyStatement("select id from sys.cluster");
        execute("set global transient stats.enabled = false");
        for (JobsLogService jobsLogService : internalCluster().getDataNodeInstances(JobsLogService.class)) {
            assertBusy(() -> assertThat(jobsLogService.isEnabled(), is(false)));
        }
        execute("select * from sys.jobs_log");
        assertThat(response.rowCount(), Is.is(0L));
    }

    @Test
    public void testSetSingleStatement() throws Exception {
        execute("select settings['stats']['jobs_log_size'] from sys.cluster");
        assertThat(response.rowCount(), Is.is(1L));
        assertThat(response.rows()[0][0], Is.is(STATS_JOBS_LOG_SIZE_SETTING.getDefault()));
        execute("set global persistent stats.enabled= true, stats.jobs_log_size=7");
        assertThat(response.rowCount(), Is.is(1L));
        execute("select settings['stats']['jobs_log_size'] from sys.cluster");
        assertThat(response.rowCount(), Is.is(1L));
        assertThat(response.rows()[0][0], Is.is(7));
        execute("reset global stats.jobs_log_size");
        assertThat(response.rowCount(), Is.is(1L));
        waitNoPendingTasksOnAll();
        execute("select settings['stats']['enabled'], settings['stats']['jobs_log_size'] from sys.cluster");
        assertThat(response.rowCount(), Is.is(1L));
        assertThat(response.rows()[0][0], Is.is(STATS_ENABLED_SETTING.getDefault()));
        assertThat(response.rows()[0][1], Is.is(STATS_JOBS_LOG_SIZE_SETTING.getDefault()));
    }

    @Test
    public void testEmptyJobsInLog() throws Exception {
        // Setup data
        execute("create table characters (id int primary key, name string)");
        sqlExecutor.ensureYellowOrGreen();
        execute("set global transient stats.enabled = true");
        execute("insert into characters (id, name) values (1, 'sysjobstest')");
        execute("refresh table characters");
        execute("delete from characters where id = 1");
        // make sure everything is deleted (nothing changed in whole class lifecycle cluster state)
        assertThat(response.rowCount(), Is.is(1L));
        execute("refresh table characters");
        execute("select * from sys.jobs_log where stmt like 'insert into%' or stmt like 'delete%'");
        assertThat(response.rowCount(), Is.is(2L));
    }
}
