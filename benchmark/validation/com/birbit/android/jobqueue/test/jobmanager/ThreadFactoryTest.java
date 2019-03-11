package com.birbit.android.jobqueue.test.jobmanager;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.birbit.android.jobqueue.BuildConfig;
import com.birbit.android.jobqueue.CancelReason;
import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.JobManager;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;
import java.util.UUID;
import java.util.concurrent.ThreadFactory;
import org.hamcrest.MatcherAssert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;


@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class ThreadFactoryTest extends JobManagerTestBase {
    private Throwable error;

    @Test
    public void testThreadFactory() throws Throwable {
        final JobManager jobManager = createJobManager(timer(mockTimer).threadFactory(new ThreadFactory() {
            @Override
            public Thread newThread(@NonNull
            Runnable r) {
                return new ThreadFactoryTest.DummyThread(r);
            }
        }));
        final Job job = new ThreadFactoryTest.CheckWorkerJob();
        waitUntilAJobIsDone(jobManager, new JobManagerTestBase.WaitUntilCallback() {
            @Override
            public void run() {
                jobManager.addJob(job);
            }

            @Override
            public void assertJob(Job job) {
            }
        });
        if ((error) != null) {
            throw error;
        }
    }

    static class DummyThread extends Thread {
        public DummyThread(Runnable runnable) {
            super(runnable, ("dummy-worker-" + (UUID.randomUUID().toString())));
        }
    }

    class CheckWorkerJob extends Job {
        protected CheckWorkerJob() {
            super(new Params(1));
        }

        @Override
        public void onAdded() {
        }

        @Override
        public void onRun() throws Throwable {
            try {
                MatcherAssert.assertThat("Worker thread should be an instance of DummyThread", ((Thread.currentThread()) instanceof ThreadFactoryTest.DummyThread));
            } catch (Throwable e) {
                error = e;
            }
        }

        @Override
        protected void onCancel(@CancelReason
        int cancelReason, @Nullable
        Throwable throwable) {
        }

        @Override
        protected RetryConstraint shouldReRunOnThrowable(@NonNull
        Throwable throwable, int runCount, int maxRunCount) {
            return RetryConstraint.CANCEL;
        }
    }
}
