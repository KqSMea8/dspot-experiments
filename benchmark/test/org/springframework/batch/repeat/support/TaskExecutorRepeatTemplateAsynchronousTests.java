/**
 * Copyright 2006-2007 the original author or authors.
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
package org.springframework.batch.repeat.support;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatCallback;
import org.springframework.batch.repeat.RepeatContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.batch.repeat.callback.NestedRepeatCallback;
import org.springframework.batch.repeat.exception.ExceptionHandler;
import org.springframework.batch.repeat.policy.SimpleCompletionPolicy;
import org.springframework.core.task.SimpleAsyncTaskExecutor;


public class TaskExecutorRepeatTemplateAsynchronousTests extends AbstractTradeBatchTests {
    RepeatTemplate template = getRepeatTemplate();

    int count = 0;

    @Test
    public void testEarlyCompletionWithException() throws Exception {
        TaskExecutorRepeatTemplate template = new TaskExecutorRepeatTemplate();
        SimpleAsyncTaskExecutor taskExecutor = new SimpleAsyncTaskExecutor();
        template.setCompletionPolicy(new SimpleCompletionPolicy(20));
        taskExecutor.setConcurrencyLimit(2);
        template.setTaskExecutor(taskExecutor);
        try {
            template.iterate(new RepeatCallback() {
                @Override
                public RepeatStatus doInIteration(RepeatContext context) throws Exception {
                    (count)++;
                    throw new IllegalStateException("foo!");
                }
            });
            Assert.fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            Assert.assertEquals("foo!", e.getMessage());
        }
        Assert.assertTrue(("Too few attempts: " + (count)), ((count) >= 1));
        Assert.assertTrue(("Too many attempts: " + (count)), ((count) <= 10));
    }

    @Test
    public void testExceptionHandlerSwallowsException() throws Exception {
        TaskExecutorRepeatTemplate template = new TaskExecutorRepeatTemplate();
        SimpleAsyncTaskExecutor taskExecutor = new SimpleAsyncTaskExecutor();
        template.setCompletionPolicy(new SimpleCompletionPolicy(4));
        taskExecutor.setConcurrencyLimit(2);
        template.setTaskExecutor(taskExecutor);
        template.setExceptionHandler(new ExceptionHandler() {
            @Override
            public void handleException(RepeatContext context, Throwable throwable) throws Throwable {
                (count)++;
            }
        });
        template.iterate(new RepeatCallback() {
            @Override
            public RepeatStatus doInIteration(RepeatContext context) throws Exception {
                throw new IllegalStateException("foo!");
            }
        });
        Assert.assertTrue(("Too few attempts: " + (count)), ((count) >= 1));
        Assert.assertTrue(("Too many attempts: " + (count)), ((count) <= 10));
    }

    @Test
    public void testNestedSession() throws Exception {
        RepeatTemplate outer = getRepeatTemplate();
        RepeatTemplate inner = new RepeatTemplate();
        outer.iterate(new NestedRepeatCallback(inner, new RepeatCallback() {
            @Override
            public RepeatStatus doInIteration(RepeatContext context) throws Exception {
                (count)++;
                Assert.assertNotNull(context);
                Assert.assertNotSame("Nested batch should have new session", context, context.getParent());
                Assert.assertSame(context, RepeatSynchronizationManager.getContext());
                return RepeatStatus.FINISHED;
            }
        }) {
            @Override
            public RepeatStatus doInIteration(RepeatContext context) throws Exception {
                (count)++;
                Assert.assertNotNull(context);
                Assert.assertSame(context, RepeatSynchronizationManager.getContext());
                return super.doInIteration(context);
            }
        });
        Assert.assertTrue(("Too few attempts: " + (count)), ((count) >= 1));
        Assert.assertTrue(("Too many attempts: " + (count)), ((count) <= 10));
    }

    /**
     * Run a batch with a single template that itself has an async task
     * executor. The result is a batch that runs in multiple threads (up to the
     * throttle limit of the template).
     *
     * @throws Exception
     * 		
     */
    @Test
    public void testMultiThreadAsynchronousExecution() throws Exception {
        final String threadName = Thread.currentThread().getName();
        final Set<String> threadNames = new HashSet<>();
        final RepeatCallback callback = new RepeatCallback() {
            @Override
            public RepeatStatus doInIteration(RepeatContext context) throws Exception {
                Assert.assertNotSame(threadName, Thread.currentThread().getName());
                threadNames.add(Thread.currentThread().getName());
                Thread.sleep(100);
                Trade item = read();
                if (item != null) {
                    processor.write(Collections.singletonList(item));
                }
                return RepeatStatus.continueIf((item != null));
            }
        };
        template.iterate(callback);
        // Shouldn't be necessary to wait:
        // Thread.sleep(500);
        Assert.assertEquals(AbstractTradeBatchTests.NUMBER_OF_ITEMS, processor.count);
        Assert.assertTrue(((threadNames.size()) > 1));
    }

    @Test
    public void testThrottleLimit() throws Exception {
        int throttleLimit = 600;
        TaskExecutorRepeatTemplate template = new TaskExecutorRepeatTemplate();
        SimpleAsyncTaskExecutor taskExecutor = new SimpleAsyncTaskExecutor();
        taskExecutor.setConcurrencyLimit(300);
        template.setTaskExecutor(taskExecutor);
        template.setThrottleLimit(throttleLimit);
        final String threadName = Thread.currentThread().getName();
        final Set<String> threadNames = new HashSet<>();
        final List<String> items = new ArrayList<>();
        final RepeatCallback callback = new RepeatCallback() {
            @Override
            public RepeatStatus doInIteration(RepeatContext context) throws Exception {
                Assert.assertNotSame(threadName, Thread.currentThread().getName());
                Trade item = read();
                threadNames.add((((Thread.currentThread().getName()) + " : ") + item));
                items.add(("" + item));
                if (item != null) {
                    processor.write(Collections.singletonList(item));
                    // Do some more I/O
                    for (int i = 0; i < 10; i++) {
                        AbstractTradeBatchTests.TradeItemReader provider = new AbstractTradeBatchTests.TradeItemReader(resource);
                        provider.open(new ExecutionContext());
                        while ((provider.read()) != null)
                            continue;

                        close();
                    }
                }
                return RepeatStatus.continueIf((item != null));
            }
        };
        template.iterate(callback);
        // Shouldn't be necessary to wait:
        // Thread.sleep(500);
        Assert.assertEquals(AbstractTradeBatchTests.NUMBER_OF_ITEMS, processor.count);
        Assert.assertTrue(((threadNames.size()) > 1));
        int frequency = Collections.frequency(items, "null");
        // System.err.println("Frequency: "+frequency);
        Assert.assertTrue((frequency <= throttleLimit));
    }

    /**
     * Wrap an otherwise synchronous batch in a callback to an asynchronous
     * template.
     *
     * @throws Exception
     * 		
     */
    @Test
    public void testSingleThreadAsynchronousExecution() throws Exception {
        TaskExecutorRepeatTemplate jobTemplate = new TaskExecutorRepeatTemplate();
        final RepeatTemplate stepTemplate = new RepeatTemplate();
        SimpleAsyncTaskExecutor taskExecutor = new SimpleAsyncTaskExecutor();
        taskExecutor.setConcurrencyLimit(2);
        jobTemplate.setTaskExecutor(taskExecutor);
        final String threadName = Thread.currentThread().getName();
        final Set<String> threadNames = new HashSet<>();
        final RepeatCallback stepCallback = new ItemReaderRepeatCallback<Trade>(provider, processor) {
            @Override
            public RepeatStatus doInIteration(RepeatContext context) throws Exception {
                Assert.assertNotSame(threadName, Thread.currentThread().getName());
                threadNames.add(Thread.currentThread().getName());
                Thread.sleep(100);
                AbstractTradeBatchTests.TradeItemReader provider = new AbstractTradeBatchTests.TradeItemReader(resource);
                provider.open(new ExecutionContext());
                while ((provider.read()) != null);
                return super.doInIteration(context);
            }
        };
        RepeatCallback jobCallback = new RepeatCallback() {
            @Override
            public RepeatStatus doInIteration(RepeatContext context) throws Exception {
                stepTemplate.iterate(stepCallback);
                return RepeatStatus.FINISHED;
            }
        };
        jobTemplate.iterate(jobCallback);
        // Shouldn't be necessary to wait:
        // Thread.sleep(500);
        Assert.assertEquals(AbstractTradeBatchTests.NUMBER_OF_ITEMS, processor.count);
        // Because of the throttling and queueing internally to a TaskExecutor,
        // more than one thread will be used - the number used is the
        // concurrency limit in the task executor, plus 1.
        // System.err.println(threadNames);
        Assert.assertTrue(((threadNames.size()) >= 1));
    }
}
