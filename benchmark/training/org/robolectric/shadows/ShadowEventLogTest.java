package org.robolectric.shadows;


import EventLog.Event;
import android.os.Build.VERSION_CODES;
import android.util.EventLog;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.ArrayList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;


/**
 * Test ShadowEventLog
 */
@RunWith(AndroidJUnit4.class)
public class ShadowEventLogTest {
    private static final String TEST_STRING1 = "hello";

    private static final String TEST_STRING2 = "world";

    private static final int TEST_INT = 123;

    private static final long TEST_LONG = 456L;

    private static final float TEST_FLOAT = 0.789F;

    private static final int TEST_TAG = 1;

    private static final int TEST_PROCESS_ID = 2;

    private static final int TEST_THREAD_ID = 3;

    private static final long TEST_TIME_NANOS = 3L;

    @Test
    public void testAddEvent_testStringLog() throws Exception {
        EventLog.Event event = new ShadowEventLog.EventBuilder(ShadowEventLogTest.TEST_TAG, ShadowEventLogTest.TEST_STRING1).setProcessId(ShadowEventLogTest.TEST_PROCESS_ID).setThreadId(ShadowEventLogTest.TEST_THREAD_ID).setTimeNanos(ShadowEventLogTest.TEST_TIME_NANOS).build();
        ShadowEventLog.addEvent(event);
        ArrayList<EventLog.Event> events = new ArrayList<>();
        EventLog.readEvents(new int[]{ ShadowEventLogTest.TEST_TAG }, events);
        assertThat(events).hasSize(1);
        assertThat(events.get(0).getTag()).isEqualTo(ShadowEventLogTest.TEST_TAG);
        assertThat(events.get(0).getProcessId()).isEqualTo(ShadowEventLogTest.TEST_PROCESS_ID);
        assertThat(events.get(0).getThreadId()).isEqualTo(ShadowEventLogTest.TEST_THREAD_ID);
        assertThat(events.get(0).getTimeNanos()).isEqualTo(ShadowEventLogTest.TEST_TIME_NANOS);
        assertThat(((String) (events.get(0).getData()))).isEqualTo(ShadowEventLogTest.TEST_STRING1);
    }

    @Test
    public void testAddEvent_testIntLog() throws Exception {
        EventLog.Event event = new ShadowEventLog.EventBuilder(ShadowEventLogTest.TEST_TAG, ShadowEventLogTest.TEST_INT).setProcessId(ShadowEventLogTest.TEST_PROCESS_ID).setThreadId(ShadowEventLogTest.TEST_THREAD_ID).setTimeNanos(ShadowEventLogTest.TEST_TIME_NANOS).build();
        ShadowEventLog.addEvent(event);
        ArrayList<EventLog.Event> events = new ArrayList<>();
        EventLog.readEvents(new int[]{ ShadowEventLogTest.TEST_TAG }, events);
        assertThat(events).hasSize(1);
        assertThat(events.get(0).getTag()).isEqualTo(ShadowEventLogTest.TEST_TAG);
        assertThat(events.get(0).getProcessId()).isEqualTo(ShadowEventLogTest.TEST_PROCESS_ID);
        assertThat(events.get(0).getThreadId()).isEqualTo(ShadowEventLogTest.TEST_THREAD_ID);
        assertThat(events.get(0).getTimeNanos()).isEqualTo(ShadowEventLogTest.TEST_TIME_NANOS);
        assertThat(((int) (events.get(0).getData()))).isEqualTo(ShadowEventLogTest.TEST_INT);
    }

    @Test
    public void testAddEvent_testLongLog() throws Exception {
        EventLog.Event event = new ShadowEventLog.EventBuilder(ShadowEventLogTest.TEST_TAG, ShadowEventLogTest.TEST_LONG).setProcessId(ShadowEventLogTest.TEST_PROCESS_ID).setThreadId(ShadowEventLogTest.TEST_THREAD_ID).setTimeNanos(ShadowEventLogTest.TEST_TIME_NANOS).build();
        ShadowEventLog.addEvent(event);
        ArrayList<EventLog.Event> events = new ArrayList<>();
        EventLog.readEvents(new int[]{ ShadowEventLogTest.TEST_TAG }, events);
        assertThat(events).hasSize(1);
        assertThat(events.get(0).getTag()).isEqualTo(ShadowEventLogTest.TEST_TAG);
        assertThat(events.get(0).getProcessId()).isEqualTo(ShadowEventLogTest.TEST_PROCESS_ID);
        assertThat(events.get(0).getThreadId()).isEqualTo(ShadowEventLogTest.TEST_THREAD_ID);
        assertThat(events.get(0).getTimeNanos()).isEqualTo(ShadowEventLogTest.TEST_TIME_NANOS);
        assertThat(((long) (events.get(0).getData()))).isEqualTo(ShadowEventLogTest.TEST_LONG);
    }

    @Test
    public void testAddEvent_testFloatLog() throws Exception {
        EventLog.Event event = new ShadowEventLog.EventBuilder(ShadowEventLogTest.TEST_TAG, ShadowEventLogTest.TEST_FLOAT).setProcessId(ShadowEventLogTest.TEST_PROCESS_ID).setThreadId(ShadowEventLogTest.TEST_THREAD_ID).setTimeNanos(ShadowEventLogTest.TEST_TIME_NANOS).build();
        ShadowEventLog.addEvent(event);
        ArrayList<EventLog.Event> events = new ArrayList<>();
        EventLog.readEvents(new int[]{ ShadowEventLogTest.TEST_TAG }, events);
        assertThat(events).hasSize(1);
        assertThat(events.get(0).getTag()).isEqualTo(ShadowEventLogTest.TEST_TAG);
        assertThat(events.get(0).getProcessId()).isEqualTo(ShadowEventLogTest.TEST_PROCESS_ID);
        assertThat(events.get(0).getThreadId()).isEqualTo(ShadowEventLogTest.TEST_THREAD_ID);
        assertThat(events.get(0).getTimeNanos()).isEqualTo(ShadowEventLogTest.TEST_TIME_NANOS);
        assertThat(((float) (events.get(0).getData()))).isEqualTo(ShadowEventLogTest.TEST_FLOAT);
    }

    @Test
    public void testAddEvent_testListLog() throws Exception {
        EventLog.Event event = new ShadowEventLog.EventBuilder(ShadowEventLogTest.TEST_TAG, new String[]{ ShadowEventLogTest.TEST_STRING1, ShadowEventLogTest.TEST_STRING2 }).setProcessId(ShadowEventLogTest.TEST_PROCESS_ID).setThreadId(ShadowEventLogTest.TEST_THREAD_ID).setTimeNanos(ShadowEventLogTest.TEST_TIME_NANOS).build();
        ShadowEventLog.addEvent(event);
        ArrayList<EventLog.Event> events = new ArrayList<>();
        EventLog.readEvents(new int[]{ ShadowEventLogTest.TEST_TAG }, events);
        assertThat(events).hasSize(1);
        assertThat(events.get(0).getTag()).isEqualTo(ShadowEventLogTest.TEST_TAG);
        assertThat(events.get(0).getProcessId()).isEqualTo(ShadowEventLogTest.TEST_PROCESS_ID);
        assertThat(events.get(0).getThreadId()).isEqualTo(ShadowEventLogTest.TEST_THREAD_ID);
        assertThat(((String[]) (events.get(0).getData()))[0]).isEqualTo(ShadowEventLogTest.TEST_STRING1);
        assertThat(((String[]) (events.get(0).getData()))[1]).isEqualTo(ShadowEventLogTest.TEST_STRING2);
    }

    @Test
    public void testWriteEvent_string() throws Exception {
        int bytes = EventLog.writeEvent(ShadowEventLogTest.TEST_TAG, ShadowEventLogTest.TEST_STRING1);
        assertThat(bytes).isEqualTo(((Integer.BYTES) + (ShadowEventLogTest.TEST_STRING1.length())));
        ArrayList<EventLog.Event> events = new ArrayList<>();
        EventLog.readEvents(new int[]{ ShadowEventLogTest.TEST_TAG }, events);
        assertThat(events).hasSize(1);
        assertThat(events.get(0).getTag()).isEqualTo(ShadowEventLogTest.TEST_TAG);
        assertThat(((String) (events.get(0).getData()))).isEqualTo(ShadowEventLogTest.TEST_STRING1);
    }

    @Test
    public void testWriteEvent_nullString() throws Exception {
        int bytes = EventLog.writeEvent(ShadowEventLogTest.TEST_TAG, ((String) (null)));
        assertThat(bytes).isEqualTo(((Integer.BYTES) + (ShadowEventLog.NULL_PLACE_HOLDER.length())));
        ArrayList<EventLog.Event> events = new ArrayList<>();
        EventLog.readEvents(new int[]{ ShadowEventLogTest.TEST_TAG }, events);
        assertThat(events).hasSize(1);
        assertThat(events.get(0).getTag()).isEqualTo(ShadowEventLogTest.TEST_TAG);
        assertThat(((String) (events.get(0).getData()))).isEqualTo(ShadowEventLog.NULL_PLACE_HOLDER);
    }

    @Test
    public void testWriteEvent_int() throws Exception {
        int bytes = EventLog.writeEvent(ShadowEventLogTest.TEST_TAG, ShadowEventLogTest.TEST_INT);
        assertThat(bytes).isEqualTo(((Integer.BYTES) + (Integer.BYTES)));
        ArrayList<EventLog.Event> events = new ArrayList<>();
        EventLog.readEvents(new int[]{ ShadowEventLogTest.TEST_TAG }, events);
        assertThat(events).hasSize(1);
        assertThat(events.get(0).getTag()).isEqualTo(ShadowEventLogTest.TEST_TAG);
        assertThat(((int) (events.get(0).getData()))).isEqualTo(ShadowEventLogTest.TEST_INT);
    }

    @Test
    public void testWriteEvent_list() throws Exception {
        int bytes = EventLog.writeEvent(ShadowEventLogTest.TEST_TAG, ShadowEventLogTest.TEST_STRING1, ShadowEventLogTest.TEST_STRING2);
        assertThat(bytes).isEqualTo(((Integer.BYTES) + (2 * (Integer.BYTES))));
        ArrayList<EventLog.Event> events = new ArrayList<>();
        EventLog.readEvents(new int[]{ ShadowEventLogTest.TEST_TAG }, events);
        assertThat(events).hasSize(1);
        assertThat(events.get(0).getTag()).isEqualTo(ShadowEventLogTest.TEST_TAG);
        assertThat(((Object[]) (events.get(0).getData()))).asList().containsExactly(ShadowEventLogTest.TEST_STRING1, ShadowEventLogTest.TEST_STRING2).inOrder();
    }

    @Test
    public void testWriteEvent_nullList() throws Exception {
        int bytes = EventLog.writeEvent(ShadowEventLogTest.TEST_TAG, ((Object[]) (null)));
        assertThat(bytes).isEqualTo(((Integer.BYTES) + (ShadowEventLog.NULL_PLACE_HOLDER.length())));
        ArrayList<EventLog.Event> events = new ArrayList<>();
        EventLog.readEvents(new int[]{ ShadowEventLogTest.TEST_TAG }, events);
        assertThat(events).hasSize(1);
        assertThat(events.get(0).getTag()).isEqualTo(ShadowEventLogTest.TEST_TAG);
        assertThat(((String) (events.get(0).getData()))).isEqualTo(ShadowEventLog.NULL_PLACE_HOLDER);
    }

    @Test
    @Config(minSdk = VERSION_CODES.M)
    public void testWriteEvent_float() throws Exception {
        int bytes = EventLog.writeEvent(ShadowEventLogTest.TEST_TAG, ShadowEventLogTest.TEST_FLOAT);
        assertThat(bytes).isEqualTo(((Integer.BYTES) + (Float.BYTES)));
        ArrayList<EventLog.Event> events = new ArrayList<>();
        EventLog.readEvents(new int[]{ ShadowEventLogTest.TEST_TAG }, events);
        assertThat(events).hasSize(1);
        assertThat(events.get(0).getTag()).isEqualTo(ShadowEventLogTest.TEST_TAG);
        assertThat(((float) (events.get(0).getData()))).isEqualTo(ShadowEventLogTest.TEST_FLOAT);
    }

    @Test
    public void testWriteEvent_long() throws Exception {
        int bytes = EventLog.writeEvent(ShadowEventLogTest.TEST_TAG, ShadowEventLogTest.TEST_LONG);
        assertThat(bytes).isEqualTo(((Integer.BYTES) + (Long.BYTES)));
        ArrayList<EventLog.Event> events = new ArrayList<>();
        EventLog.readEvents(new int[]{ ShadowEventLogTest.TEST_TAG }, events);
        assertThat(events).hasSize(1);
        assertThat(events.get(0).getTag()).isEqualTo(ShadowEventLogTest.TEST_TAG);
        assertThat(((long) (events.get(0).getData()))).isEqualTo(ShadowEventLogTest.TEST_LONG);
    }
}
