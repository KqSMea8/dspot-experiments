package com.baeldung.binding;


import Level.INFO;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import java.util.List;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;


/**
 * Created by madhumita.g on 01-08-2018.
 */
@RunWith(MockitoJUnitRunner.class)
public class AnimalUnitTest {
    @Mock
    private Appender mockAppender;

    @Captor
    private ArgumentCaptor<LoggingEvent> captorLoggingEvent;

    @Test
    public void whenCalledWithoutParameters_shouldCallFunctionMakeNoiseWithoutParameters() {
        Animal animal = new Animal();
        animal.makeNoise();
        Mockito.verify(mockAppender).doAppend(captorLoggingEvent.capture());
        final LoggingEvent loggingEvent = captorLoggingEvent.getValue();
        Assert.assertThat(loggingEvent.getLevel(), CoreMatchers.is(INFO));
        Assert.assertThat(loggingEvent.getFormattedMessage(), CoreMatchers.is("generic animal noise"));
    }

    @Test
    public void whenCalledWithParameters_shouldCallFunctionMakeNoiseWithParameters() {
        Animal animal = new Animal();
        int testValue = 3;
        animal.makeNoise(testValue);
        Mockito.verify(mockAppender, Mockito.times(3)).doAppend(captorLoggingEvent.capture());
        final List<LoggingEvent> loggingEvents = captorLoggingEvent.getAllValues();
        for (LoggingEvent loggingEvent : loggingEvents) {
            Assert.assertThat(loggingEvent.getLevel(), CoreMatchers.is(INFO));
            Assert.assertThat(loggingEvent.getFormattedMessage(), CoreMatchers.is(("generic animal noise countdown " + testValue)));
            testValue--;
        }
    }
}
