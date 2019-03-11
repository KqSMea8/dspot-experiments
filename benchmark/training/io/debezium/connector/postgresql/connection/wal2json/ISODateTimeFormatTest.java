/**
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.connector.postgresql.connection.wal2json;


import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.time.chrono.IsoEra;
import java.time.format.TextStyle;
import java.util.Locale;
import org.fest.assertions.Assertions;
import org.junit.Test;


public class ISODateTimeFormatTest {
    private static final String BCE_DISPLAY_NAME = IsoEra.BCE.getDisplayName(TextStyle.SHORT, Locale.getDefault());

    @Test
    public void testTimestamp() {
        Assertions.assertThat(DateTimeFormat.get().timestamp("2016-11-04 13:51:30")).isEqualTo(1478267490000000000L);
        Assertions.assertThat(DateTimeFormat.get().timestamp("2016-11-04 13:51:30.123")).isEqualTo(1478267490123000000L);
        Assertions.assertThat(DateTimeFormat.get().timestamp("2016-11-04 13:51:30.123000")).isEqualTo(1478267490123000000L);
        Assertions.assertThat(DateTimeFormat.get().timestamp("2016-11-04 13:51:30.123456")).isEqualTo(1478267490123456000L);
        Assertions.assertThat(DateTimeFormat.get().timestamp("2016-11-04 13:51:30.123456")).isEqualTo(1478267490123456000L);
        Assertions.assertThat(DateTimeFormat.get().timestamp(("0002-12-01 17:00:00 " + (ISODateTimeFormatTest.BCE_DISPLAY_NAME)))).isEqualTo((-6829604178871345152L));
    }

    @Test
    public void testTimestampWithTimeZone() {
        Assertions.assertThat(DateTimeFormat.get().timestampWithTimeZone("2016-11-04 13:51:30+02")).isEqualTo(1478260290000000000L);
        Assertions.assertThat(DateTimeFormat.get().timestampWithTimeZone("2016-11-04 13:51:30.123+02")).isEqualTo(1478260290123000000L);
        Assertions.assertThat(DateTimeFormat.get().timestampWithTimeZone("2016-11-04 13:51:30.123000+02")).isEqualTo(1478260290123000000L);
        Assertions.assertThat(DateTimeFormat.get().timestampWithTimeZone("2016-11-04 13:51:30.123789+02")).isEqualTo(1478260290123789000L);
        Assertions.assertThat(DateTimeFormat.get().timestampWithTimeZone("2016-11-04 13:51:30.123789+02:30")).isEqualTo(1478258490123789000L);
        Assertions.assertThat(DateTimeFormat.get().timestampWithTimeZone(("2016-11-04 13:51:30.123789+02:30 " + (ISODateTimeFormatTest.BCE_DISPLAY_NAME)))).isEqualTo(3399351806090650312L);
    }

    @Test
    public void testDate() {
        Assertions.assertThat(DateTimeFormat.get().date("2016-11-04")).isEqualTo(LocalDate.of(2016, 11, 4));
        Assertions.assertThat(DateTimeFormat.get().date(("2016-11-04 " + (ISODateTimeFormatTest.BCE_DISPLAY_NAME)))).isEqualTo(LocalDate.of((-2015), 11, 4));
    }

    @Test
    public void testTime() {
        Assertions.assertThat(DateTimeFormat.get().time("13:51:30")).isEqualTo(LocalTime.of(13, 51, 30));
    }

    @Test
    public void testTimeWithTimeZone() {
        Assertions.assertThat(DateTimeFormat.get().timeWithTimeZone("13:51:30+02")).isEqualTo(OffsetTime.of(11, 51, 30, 0, ZoneOffset.UTC));
    }

    @Test
    public void testSystemTimestamp() {
        Assertions.assertThat(DateTimeFormat.get().systemTimestamp("2017-10-17 13:51:30Z")).isEqualTo(1508248290000000000L);
        Assertions.assertThat(DateTimeFormat.get().systemTimestamp("2017-10-17 13:51:30.000Z")).isEqualTo(1508248290000000000L);
        Assertions.assertThat(DateTimeFormat.get().systemTimestamp("2017-10-17 13:51:30.456Z")).isEqualTo(1508248290456000000L);
        Assertions.assertThat(DateTimeFormat.get().systemTimestamp("2017-10-17 13:51:30.345123Z")).isEqualTo(1508248290345123000L);
        Assertions.assertThat(DateTimeFormat.get().systemTimestamp("2018-03-22 12:30:56.824452+05:30")).isEqualTo(1521702056824452000L);
        Assertions.assertThat(DateTimeFormat.get().systemTimestamp("2018-03-22 12:30:56.824452+05")).isEqualTo(1521703856824452000L);
    }
}
