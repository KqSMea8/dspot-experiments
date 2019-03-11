/**
 * Copyright 2017 ThoughtWorks, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.thoughtworks.go.plugin.api.logging;


import com.thoughtworks.go.plugin.internal.api.LoggingService;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;


public class LoggerTest {
    @Test
    public void shouldLogMessageWithException() {
        LoggingService loggingService = Mockito.mock(LoggingService.class);
        Logger.initialize(loggingService);
        Logger logger = Logger.getLoggerFor(this.getClass());
        RuntimeException exception = new RuntimeException("error");
        logger.error("message", exception);
        Mockito.verify(loggingService).error(ArgumentMatchers.anyString(), ArgumentMatchers.eq(this.getClass().getName()), ArgumentMatchers.eq("message"), ArgumentMatchers.eq(exception));
    }
}
