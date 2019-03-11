/**
 * The Alluxio Open Foundation licenses this work under the Apache License, version 2.0
 * (the "License"). You may not use this work except in compliance with the License, which is
 * available at www.apache.org/licenses/LICENSE-2.0
 *
 * This software is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied, as more fully set forth in the License.
 *
 * See the NOTICE file distributed with this work for information regarding copyright ownership.
 */
package alluxio.server.ft.journal;


import DeployMode.ZOOKEEPER_HA;
import PortCoordination.JOURNAL_STOP_MULTI_MASTER;
import PortCoordination.JOURNAL_STOP_SINGLE_MASTER;
import PropertyKey.MASTER_JOURNAL_CHECKPOINT_PERIOD_ENTRIES;
import PropertyKey.MASTER_JOURNAL_LOG_SIZE_BYTES_MAX;
import PropertyKey.MASTER_JOURNAL_TAILER_SHUTDOWN_QUIET_WAIT_TIME_MS;
import PropertyKey.USER_RPC_RETRY_MAX_SLEEP_MS;
import PropertyKey.ZOOKEEPER_SESSION_TIMEOUT;
import alluxio.AlluxioURI;
import alluxio.AuthenticatedUserRule;
import alluxio.ConfigurationRule;
import alluxio.Constants;
import alluxio.SystemPropertyRule;
import alluxio.client.file.FileSystem;
import alluxio.client.file.FileSystemContext;
import alluxio.conf.ServerConfiguration;
import alluxio.master.LocalAlluxioCluster;
import alluxio.master.MultiMasterLocalAlluxioCluster;
import alluxio.multi.process.MultiProcessCluster;
import alluxio.testutils.BaseIntegrationTest;
import alluxio.underfs.UnderFileSystemConfiguration;
import alluxio.underfs.UnderFileSystemFactory;
import alluxio.util.CommonUtils;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;


/**
 * Test master journal for cluster terminating. Assert that test can replay the log and reproduce
 * the correct state. Test both the single master and multi masters.
 */
@Ignore
public class JournalShutdownIntegrationTest extends BaseIntegrationTest {
    @ClassRule
    public static SystemPropertyRule sDisableHdfsCacheRule = new SystemPropertyRule("fs.hdfs.impl.disable.cache", "true");

    @Rule
    public AuthenticatedUserRule mAuthenticatedUser = new AuthenticatedUserRule("test", ServerConfiguration.global());

    @Rule
    public ConfigurationRule mConfigRule = new ConfigurationRule(new ImmutableMap.Builder<alluxio.conf.PropertyKey, String>().put(MASTER_JOURNAL_TAILER_SHUTDOWN_QUIET_WAIT_TIME_MS, "100").put(MASTER_JOURNAL_CHECKPOINT_PERIOD_ENTRIES, "2").put(MASTER_JOURNAL_LOG_SIZE_BYTES_MAX, "32").put(USER_RPC_RETRY_MAX_SLEEP_MS, "1sec").build(), ServerConfiguration.global());

    private static final long SHUTDOWN_TIME_MS = 15 * (Constants.SECOND_MS);

    private static final String TEST_FILE_DIR = "/files/";

    private static final int TEST_NUM_MASTERS = 3;

    private static final long TEST_TIME_MS = Constants.SECOND_MS;

    private JournalShutdownIntegrationTest.ClientThread mCreateFileThread;

    /**
     * Executor for running client threads.
     */
    private ExecutorService mExecutorsForClient;

    private FileSystemContext mFsContext;

    @Test
    public void singleMasterJournalStopIntegration() throws Exception {
        MultiProcessCluster cluster = MultiProcessCluster.newBuilder(JOURNAL_STOP_SINGLE_MASTER).setClusterName("singleMasterJournalStopIntegration").setNumWorkers(0).setNumMasters(1).build();
        try {
            cluster.start();
            FileSystem fs = cluster.getFileSystemClient();
            runCreateFileThread(fs);
            cluster.waitForAndKillPrimaryMaster((10 * (Constants.SECOND_MS)));
            awaitClientTermination();
            cluster.startMaster(0);
            int actualFiles = fs.listStatus(new AlluxioURI(JournalShutdownIntegrationTest.TEST_FILE_DIR)).size();
            int successFiles = mCreateFileThread.getSuccessNum();
            Assert.assertTrue(String.format("successFiles: %s, actualFiles: %s", successFiles, actualFiles), ((successFiles == actualFiles) || ((successFiles + 1) == actualFiles)));
            cluster.notifySuccess();
        } finally {
            cluster.destroy();
        }
    }

    /* We use the external cluster for this test due to flakiness issues when running in a single JVM. */
    @Test
    public void multiMasterJournalStopIntegration() throws Exception {
        MultiProcessCluster cluster = // Cannot go lower than 2x the tick time. Curator testing cluster tick time is 3s and
        // cannot be overridden until later versions of Curator.
        MultiProcessCluster.newBuilder(JOURNAL_STOP_MULTI_MASTER).setClusterName("multiMasterJournalStopIntegration").setNumWorkers(0).setNumMasters(JournalShutdownIntegrationTest.TEST_NUM_MASTERS).setDeployMode(ZOOKEEPER_HA).addProperty(ZOOKEEPER_SESSION_TIMEOUT, "6s").build();
        try {
            cluster.start();
            FileSystem fs = cluster.getFileSystemClient();
            runCreateFileThread(fs);
            for (int i = 0; i < (JournalShutdownIntegrationTest.TEST_NUM_MASTERS); i++) {
                cluster.waitForAndKillPrimaryMaster((30 * (Constants.SECOND_MS)));
            }
            awaitClientTermination();
            cluster.startMaster(0);
            int actualFiles = fs.listStatus(new AlluxioURI(JournalShutdownIntegrationTest.TEST_FILE_DIR)).size();
            int successFiles = mCreateFileThread.getSuccessNum();
            Assert.assertTrue(String.format("successFiles: %s, actualFiles: %s", successFiles, actualFiles), ((successFiles == actualFiles) || ((successFiles + 1) == actualFiles)));
            cluster.notifySuccess();
        } finally {
            cluster.destroy();
        }
    }

    @Test
    public void singleMasterMountUnmountJournal() throws Exception {
        LocalAlluxioCluster cluster = setupSingleMasterCluster();
        UnderFileSystemFactory factory = mountUnmount(cluster.getClient());
        // Shutdown the cluster
        cluster.stopFS();
        CommonUtils.sleepMs(JournalShutdownIntegrationTest.TEST_TIME_MS);
        awaitClientTermination();
        // Fail the creation of UFS
        Mockito.doThrow(new RuntimeException()).when(factory).create(ArgumentMatchers.anyString(), ArgumentMatchers.any(UnderFileSystemConfiguration.class), ServerConfiguration.global());
        createFsMasterFromJournal();
    }

    @Test
    public void multiMasterMountUnmountJournal() throws Exception {
        MultiMasterLocalAlluxioCluster cluster = setupMultiMasterCluster();
        UnderFileSystemFactory factory = mountUnmount(cluster.getClient());
        // Kill the leader one by one.
        for (int kills = 0; kills < (JournalShutdownIntegrationTest.TEST_NUM_MASTERS); kills++) {
            cluster.waitForNewMaster((120 * (Constants.SECOND_MS)));
            Assert.assertTrue(cluster.stopLeader());
        }
        // Shutdown the cluster
        cluster.stopFS();
        CommonUtils.sleepMs(JournalShutdownIntegrationTest.TEST_TIME_MS);
        awaitClientTermination();
        // Fail the creation of UFS
        Mockito.doThrow(new RuntimeException()).when(factory).create(ArgumentMatchers.anyString(), ArgumentMatchers.any(UnderFileSystemConfiguration.class), ServerConfiguration.global());
        createFsMasterFromJournal();
    }

    /**
     * Hold a client and keep creating files.
     */
    class ClientThread implements Runnable {
        /**
         * The number of successfully created files.
         */
        private int mSuccessNum = 0;

        private final int mOpType;// 0: create file


        private final FileSystem mFileSystem;

        /**
         * Constructs the client thread.
         *
         * @param opType
         * 		the create operation type
         * @param fs
         * 		a file system client to use for creating files
         */
        public ClientThread(int opType, FileSystem fs) {
            mOpType = opType;
            mFileSystem = fs;
        }

        /**
         * Gets the number of files which are successfully created.
         *
         * @return the number of files successfully created
         */
        public int getSuccessNum() {
            return mSuccessNum;
        }

        /**
         * Keep creating files until something crashes or fail to create. Record how many files are
         * created successfully.
         */
        @Override
        public void run() {
            try {
                // This infinity loop will be broken if something crashes or fail to create. This is
                // expected since the master will shutdown at a certain time.
                while (!(Thread.interrupted())) {
                    if ((mOpType) == 0) {
                        try {
                            mFileSystem.createFile(new AlluxioURI(((JournalShutdownIntegrationTest.TEST_FILE_DIR) + (mSuccessNum)))).close();
                        } catch (IOException e) {
                            break;
                        }
                    } else
                        if ((mOpType) == 1) {
                            // TODO(gene): Add this back when there is new RawTable client API.
                            // if (mFileSystem.createRawTable(new AlluxioURI(TEST_TABLE_DIR + mSuccessNum), 1) ==
                            // -1) {
                            // break;
                            // }
                        }

                    // The create operation may succeed at the master side but still returns false due to the
                    // shutdown. So the mSuccessNum may be less than the actual success number.
                    (mSuccessNum)++;
                    CommonUtils.sleepMs(100);
                } 
            } catch (Exception e) {
                // Something crashed. Stop the thread.
            }
        }
    }
}
