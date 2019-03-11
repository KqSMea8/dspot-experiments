package tech.tablesaw.api;


import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tech.tablesaw.io.csv.CsvReadOptions;


/**
 * TODO All the methods on this class should be tested carefully
 */
public class RowTest {
    @Test
    public void columnNames() throws IOException {
        Table table = Table.read().csv("../data/bush.csv");
        Row row = new Row(table);
        Assertions.assertEquals(table.columnNames(), row.columnNames());
    }

    @Test
    public void testColumnCount() throws IOException {
        Table table = Table.read().csv("../data/bush.csv");
        Row row = new Row(table);
        Assertions.assertEquals(table.columnCount(), row.columnCount());
    }

    @Test
    public void testGetBoolean() throws IOException {
        ColumnType[] types = new ColumnType[]{ ColumnType.STRING, ColumnType.STRING, ColumnType.INTEGER, ColumnType.INTEGER, ColumnType.INTEGER, ColumnType.INTEGER, ColumnType.DOUBLE, ColumnType.DOUBLE, ColumnType.DOUBLE, ColumnType.BOOLEAN, ColumnType.INTEGER, ColumnType.INTEGER, ColumnType.INTEGER, ColumnType.DOUBLE, ColumnType.DOUBLE };
        Table table = Table.read().csv(CsvReadOptions.builder(new File("../data/baseball.csv")).columnTypes(types));
        Row row = new Row(table);
        while (row.hasNext()) {
            row.next();
            Assertions.assertEquals(table.booleanColumn(9).get(row.getRowNumber()), row.getBoolean(9));
            Assertions.assertEquals(table.booleanColumn("Playoffs").get(row.getRowNumber()), row.getBoolean("Playoffs"));
        } 
    }

    @Test
    public void testGetDate() throws IOException {
        Table table = Table.read().csv("../data/bush.csv");
        Row row = new Row(table);
        while (row.hasNext()) {
            row.next();
            LocalDate date = table.dateColumn("date").get(row.getRowNumber());
            Assertions.assertEquals(date, row.getDate(0));
            Assertions.assertEquals(date, row.getDate("date"));
        } 
    }

    @Test
    public void testGetDate2() throws IOException {
        Table table = Table.read().csv("../data/bush.csv");
        Row row = new Row(table);
        while (row.hasNext()) {
            row.next();
            Assertions.assertEquals(table.dateColumn("date").get(row.getRowNumber()), row.getDate("DATE"));
        } 
    }

    @Test
    public void testGetDateTime() throws IOException {
        ColumnType[] types = new ColumnType[]{ ColumnType.LOCAL_DATE, ColumnType.LOCAL_TIME, ColumnType.STRING, ColumnType.STRING, ColumnType.SHORT, ColumnType.SHORT, ColumnType.SHORT, ColumnType.DOUBLE, ColumnType.DOUBLE, ColumnType.DOUBLE, ColumnType.DOUBLE };
        Table table = Table.read().csv(CsvReadOptions.builder(new File("../data/rev_tornadoes_1950-2014.csv")).columnTypes(types).minimizeColumnSizes(true));
        DateTimeColumn dateTimeCol = table.dateColumn("Date").atTime(table.timeColumn("Time"));
        dateTimeCol.setName("DateTime");
        table.addColumns(dateTimeCol);
        Row row = new Row(table);
        while (row.hasNext()) {
            row.next();
            LocalDateTime dttm = dateTimeCol.get(row.getRowNumber());
            Assertions.assertEquals(dttm, row.getDateTime(11));
            Assertions.assertEquals(dttm, row.getDateTime("DateTime"));
        } 
    }

    @Test
    public void testGetDouble() throws IOException {
        ColumnType[] types = new ColumnType[]{ ColumnType.STRING, ColumnType.STRING, ColumnType.INTEGER, ColumnType.INTEGER, ColumnType.INTEGER, ColumnType.INTEGER, ColumnType.DOUBLE, ColumnType.DOUBLE, ColumnType.DOUBLE, ColumnType.INTEGER, ColumnType.INTEGER, ColumnType.INTEGER, ColumnType.INTEGER, ColumnType.DOUBLE, ColumnType.DOUBLE };
        Table table = Table.read().csv(CsvReadOptions.builder(new File("../data/baseball.csv")).columnTypes(types));
        Row row = new Row(table);
        while (row.hasNext()) {
            row.next();
            Assertions.assertEquals(table.doubleColumn(6).getDouble(row.getRowNumber()), row.getDouble(6), 0.01);
            Assertions.assertEquals(table.doubleColumn("OBP").getDouble(row.getRowNumber()), row.getDouble("OBP"), 0.01);
        } 
    }

    @Test
    public void testGetFloat() throws IOException {
        ColumnType[] types = new ColumnType[]{ ColumnType.STRING, ColumnType.STRING, ColumnType.INTEGER, ColumnType.INTEGER, ColumnType.INTEGER, ColumnType.INTEGER, ColumnType.FLOAT, ColumnType.FLOAT, ColumnType.FLOAT, ColumnType.INTEGER, ColumnType.INTEGER, ColumnType.INTEGER, ColumnType.INTEGER, ColumnType.FLOAT, ColumnType.FLOAT };
        Table table = Table.read().csv(CsvReadOptions.builder(new File("../data/baseball.csv")).columnTypes(types));
        Row row = new Row(table);
        while (row.hasNext()) {
            row.next();
            Assertions.assertEquals(table.floatColumn(6).getFloat(row.getRowNumber()), row.getFloat(6), 0.01);
            Assertions.assertEquals(table.floatColumn("OBP").getFloat(row.getRowNumber()), row.getFloat("OBP"), 0.01);
        } 
    }

    @Test
    public void testGetLong() throws IOException {
        ColumnType[] types = new ColumnType[]{ ColumnType.LOCAL_DATE, ColumnType.LONG, ColumnType.STRING };
        Table table = Table.read().csv(CsvReadOptions.builder(new File("../data/bush.csv")).columnTypes(types).minimizeColumnSizes(true));
        Row row = new Row(table);
        while (row.hasNext()) {
            row.next();
            Assertions.assertEquals(table.longColumn(1).getLong(row.getRowNumber()), row.getLong(1));
            Assertions.assertEquals(table.longColumn("approval").getLong(row.getRowNumber()), row.getLong("approval"));
        } 
    }

    @Test
    public void testGetObject() throws IOException {
        ColumnType[] types = new ColumnType[]{ ColumnType.LOCAL_DATE, ColumnType.LONG, ColumnType.STRING };
        Table table = Table.read().csv(CsvReadOptions.builder(new File("../data/bush.csv")).columnTypes(types).minimizeColumnSizes(true));
        Row row = new Row(table);
        while (row.hasNext()) {
            row.next();
            Assertions.assertEquals(table.dateColumn(0).get(row.getRowNumber()), row.getObject(0));
            Assertions.assertEquals(table.dateColumn("date").get(row.getRowNumber()), row.getObject("date"));
        } 
    }

    @Test
    public void testGetPackedDate() throws IOException {
        Table table = Table.read().csv("../data/bush.csv");
        Row row = new Row(table);
        while (row.hasNext()) {
            row.next();
            Assertions.assertEquals(table.dateColumn(0).getIntInternal(row.getRowNumber()), row.getPackedDate(0));
            Assertions.assertEquals(table.dateColumn("date").getIntInternal(row.getRowNumber()), row.getPackedDate("date"));
        } 
    }

    @Test
    public void testGetPackedDateTime() throws IOException {
        ColumnType[] types = new ColumnType[]{ ColumnType.LOCAL_DATE, ColumnType.LOCAL_TIME, ColumnType.STRING, ColumnType.STRING, ColumnType.SHORT, ColumnType.SHORT, ColumnType.SHORT, ColumnType.DOUBLE, ColumnType.DOUBLE, ColumnType.DOUBLE, ColumnType.DOUBLE };
        Table table = Table.read().csv(CsvReadOptions.builder(new File("../data/rev_tornadoes_1950-2014.csv")).columnTypes(types).minimizeColumnSizes(true));
        DateTimeColumn dateTimeCol = table.dateColumn("Date").atTime(table.timeColumn("Time"));
        dateTimeCol.setName("DateTime");
        table.addColumns(dateTimeCol);
        Row row = new Row(table);
        while (row.hasNext()) {
            row.next();
            Assertions.assertEquals(table.dateTimeColumn(11).getLongInternal(row.getRowNumber()), row.getPackedDateTime(11));
            Assertions.assertEquals(table.dateTimeColumn("DateTime").getLongInternal(row.getRowNumber()), row.getPackedDateTime("DateTime"));
        } 
    }

    @Test
    public void testGetPackedTime() throws IOException {
        ColumnType[] types = new ColumnType[]{ ColumnType.LOCAL_DATE, ColumnType.LOCAL_TIME, ColumnType.STRING, ColumnType.STRING, ColumnType.SHORT, ColumnType.SHORT, ColumnType.SHORT, ColumnType.DOUBLE, ColumnType.DOUBLE, ColumnType.DOUBLE, ColumnType.DOUBLE };
        Table table = Table.read().csv(CsvReadOptions.builder(new File("../data/rev_tornadoes_1950-2014.csv")).columnTypes(types).minimizeColumnSizes(true));
        Row row = new Row(table);
        while (row.hasNext()) {
            row.next();
            Assertions.assertEquals(table.timeColumn(1).getIntInternal(row.getRowNumber()), row.getPackedTime(1));
            Assertions.assertEquals(table.timeColumn("Time").getIntInternal(row.getRowNumber()), row.getPackedTime("Time"));
        } 
    }

    @Test
    public void testGetShort() throws IOException {
        ColumnType[] types = new ColumnType[]{ ColumnType.LOCAL_DATE, ColumnType.SHORT, ColumnType.STRING };
        Table table = Table.read().csv(CsvReadOptions.builder(new File("../data/bush.csv")).columnTypes(types).minimizeColumnSizes(true));
        Row row = new Row(table);
        while (row.hasNext()) {
            row.next();
            Assertions.assertEquals(table.shortColumn(1).getShort(row.getRowNumber()), row.getShort(1));
            Assertions.assertEquals(table.shortColumn("approval").getShort(row.getRowNumber()), row.getShort("approval"));
        } 
    }

    @Test
    public void testGetString() throws IOException {
        ColumnType[] types = new ColumnType[]{ ColumnType.LOCAL_DATE, ColumnType.SHORT, ColumnType.STRING };
        Table table = Table.read().csv(CsvReadOptions.builder(new File("../data/bush.csv")).minimizeColumnSizes(true).columnTypes(types));
        Row row = new Row(table);
        while (row.hasNext()) {
            row.next();
            Assertions.assertEquals(table.stringColumn(2).get(row.getRowNumber()), row.getString(2));
            Assertions.assertEquals(table.stringColumn("who").get(row.getRowNumber()), row.getString("who"));
        } 
    }

    @Test
    public void testGetText() throws IOException {
        ColumnType[] types = new ColumnType[]{ ColumnType.LOCAL_DATE, ColumnType.SHORT, ColumnType.TEXT };
        Table table = Table.read().csv(CsvReadOptions.builder(new File("../data/bush.csv")).minimizeColumnSizes(true).columnTypes(types));
        Row row = new Row(table);
        while (row.hasNext()) {
            row.next();
            Assertions.assertEquals(table.textColumn(2).get(row.getRowNumber()), row.getText(2));
            Assertions.assertEquals(table.textColumn("who").get(row.getRowNumber()), row.getText("who"));
        } 
    }

    @Test
    public void testGetTime() throws IOException {
        ColumnType[] types = new ColumnType[]{ ColumnType.LOCAL_DATE, ColumnType.LOCAL_TIME, ColumnType.STRING, ColumnType.STRING, ColumnType.SHORT, ColumnType.SHORT, ColumnType.SHORT, ColumnType.DOUBLE, ColumnType.DOUBLE, ColumnType.DOUBLE, ColumnType.DOUBLE };
        Table table = Table.read().csv(CsvReadOptions.builder(new File("../data/rev_tornadoes_1950-2014.csv")).columnTypes(types).minimizeColumnSizes(true));
        Row row = new Row(table);
        while (row.hasNext()) {
            row.next();
            LocalTime time = table.timeColumn("Time").get(row.getRowNumber());
            Assertions.assertEquals(time, row.getTime(1));
            Assertions.assertEquals(time, row.getTime("Time"));
        } 
    }

    @Test
    public void testSetBoolean() throws IOException {
        ColumnType[] types = new ColumnType[]{ ColumnType.STRING, ColumnType.STRING, ColumnType.INTEGER, ColumnType.INTEGER, ColumnType.INTEGER, ColumnType.INTEGER, ColumnType.DOUBLE, ColumnType.DOUBLE, ColumnType.DOUBLE, ColumnType.BOOLEAN, ColumnType.INTEGER, ColumnType.INTEGER, ColumnType.INTEGER, ColumnType.DOUBLE, ColumnType.DOUBLE };
        Table table = Table.read().csv(CsvReadOptions.builder(new File("../data/baseball.csv")).columnTypes(types));
        Row row = new Row(table);
        while (row.hasNext()) {
            row.next();
            Boolean rowVal = table.booleanColumn("Playoffs").get(row.getRowNumber());
            row.setBoolean("Playoffs", (!rowVal));
            Assertions.assertEquals((!rowVal), row.getBoolean(9));
            row.setBoolean("Playoffs", rowVal);
            Assertions.assertEquals(rowVal, row.getBoolean("Playoffs"));
        } 
    }

    @Test
    public void testSetDate() throws IOException {
        Table table = Table.read().csv("../data/bush.csv");
        Row row = new Row(table);
        while (row.hasNext()) {
            row.next();
            LocalDate date = table.dateColumn("date").get(row.getRowNumber());
            // test setDate(index, value)
            LocalDate dateIncrementedByOne = date.plusDays(1);
            row.setDate(0, dateIncrementedByOne);
            Assertions.assertEquals(dateIncrementedByOne, row.getDate(0));
            // test setDate(key, value)
            LocalDate dateIncrementedByTwo = date.plusDays(2);
            row.setDate("date", dateIncrementedByTwo);
            Assertions.assertEquals(dateIncrementedByTwo, row.getDate("date"));
        } 
    }

    @Test
    public void testSetDateTime() throws IOException {
        ColumnType[] types = new ColumnType[]{ ColumnType.LOCAL_DATE, ColumnType.LOCAL_TIME, ColumnType.STRING, ColumnType.STRING, ColumnType.SHORT, ColumnType.SHORT, ColumnType.SHORT, ColumnType.DOUBLE, ColumnType.DOUBLE, ColumnType.DOUBLE, ColumnType.DOUBLE };
        Table table = Table.read().csv(CsvReadOptions.builder(new File("../data/rev_tornadoes_1950-2014.csv")).columnTypes(types).minimizeColumnSizes(true));
        DateTimeColumn dateTimeCol = table.dateColumn("Date").atTime(table.timeColumn("Time"));
        dateTimeCol.setName("DateTime");
        table.addColumns(dateTimeCol);
        Row row = new Row(table);
        while (row.hasNext()) {
            row.next();
            LocalDateTime dttm_less5 = dateTimeCol.get(row.getRowNumber()).minusHours(5);
            row.setDateTime(11, dttm_less5);
            Assertions.assertEquals(dttm_less5, row.getDateTime(11));
            LocalDateTime dttm_add5 = dateTimeCol.get(row.getRowNumber()).plusHours(5);
            row.setDateTime("DateTime", dttm_add5);
            Assertions.assertEquals(dttm_add5, row.getDateTime("DateTime"));
        } 
    }

    @Test
    public void testSetDouble() throws IOException {
        ColumnType[] types = new ColumnType[]{ ColumnType.STRING, ColumnType.STRING, ColumnType.INTEGER, ColumnType.INTEGER, ColumnType.INTEGER, ColumnType.INTEGER, ColumnType.DOUBLE, ColumnType.DOUBLE, ColumnType.DOUBLE, ColumnType.INTEGER, ColumnType.INTEGER, ColumnType.INTEGER, ColumnType.INTEGER, ColumnType.DOUBLE, ColumnType.DOUBLE };
        Table table = Table.read().csv(CsvReadOptions.builder(new File("../data/baseball.csv")).columnTypes(types));
        Row row = new Row(table);
        while (row.hasNext()) {
            row.next();
            double rowVal = table.doubleColumn("OBP").getDouble(row.getRowNumber());
            // setDouble(columnIndex, value)
            row.setDouble(6, (rowVal + (Math.PI)));
            Assertions.assertEquals((rowVal + (Math.PI)), row.getDouble(6), 0.001);
            // setDouble(columnName, value)
            row.setDouble("OBP", (rowVal + (2 * (Math.PI))));
            Assertions.assertEquals((rowVal + (2 * (Math.PI))), row.getDouble("OBP"), 0.001);
        } 
    }

    @Test
    public void testSetFloat() throws IOException {
        ColumnType[] types = new ColumnType[]{ ColumnType.STRING, ColumnType.STRING, ColumnType.INTEGER, ColumnType.INTEGER, ColumnType.INTEGER, ColumnType.INTEGER, ColumnType.FLOAT, ColumnType.FLOAT, ColumnType.FLOAT, ColumnType.INTEGER, ColumnType.INTEGER, ColumnType.INTEGER, ColumnType.INTEGER, ColumnType.FLOAT, ColumnType.FLOAT };
        Table table = Table.read().csv(CsvReadOptions.builder(new File("../data/baseball.csv")).columnTypes(types));
        Row row = new Row(table);
        while (row.hasNext()) {
            row.next();
            float rowVal = table.floatColumn("OBP").getFloat(row.getRowNumber());
            // setFloat(columnIndex, value)
            row.setFloat(6, (rowVal + ((float) (Math.PI))));
            Assertions.assertEquals((rowVal + ((float) (Math.PI))), row.getFloat(6), 0.001);
            // setFloat(columnName, value)
            row.setFloat("OBP", (rowVal + (2 * ((float) (Math.PI)))));
            Assertions.assertEquals((rowVal + (2 * ((float) (Math.PI)))), row.getFloat("OBP"), 0.001);
        } 
    }

    @Test
    public void testSetInt() throws IOException {
        ColumnType[] types = new ColumnType[]{ ColumnType.STRING, ColumnType.STRING, ColumnType.INTEGER, ColumnType.INTEGER, ColumnType.INTEGER, ColumnType.INTEGER, ColumnType.DOUBLE, ColumnType.DOUBLE, ColumnType.DOUBLE, ColumnType.INTEGER, ColumnType.INTEGER, ColumnType.INTEGER, ColumnType.INTEGER, ColumnType.DOUBLE, ColumnType.DOUBLE };
        Table table = Table.read().csv(CsvReadOptions.builder(new File("../data/baseball.csv")).columnTypes(types));
        Row row = new Row(table);
        while (row.hasNext()) {
            row.next();
            int rowVal = table.intColumn("RS").getInt(row.getRowNumber());
            // setDouble(columnIndex, value)
            row.setInt(3, (rowVal + 1));
            Assertions.assertEquals((rowVal + 1), row.getInt(3));
            // setDouble(columnName, value)
            row.setInt("RS", (rowVal + 2));
            Assertions.assertEquals((rowVal + 2), row.getInt("RS"));
        } 
    }

    @Test
    public void testSetLong() throws IOException {
        ColumnType[] types = new ColumnType[]{ ColumnType.LOCAL_DATE, ColumnType.LONG, ColumnType.STRING };
        Table table = Table.read().csv(CsvReadOptions.builder(new File("../data/bush.csv")).minimizeColumnSizes(true).columnTypes(types));
        Row row = new Row(table);
        while (row.hasNext()) {
            row.next();
            Long rowVal = table.longColumn(1).getLong(row.getRowNumber());
            // setLong(columnIndex, value)
            row.setLong(1, (rowVal + 1));
            Assertions.assertEquals((rowVal + 1), row.getLong(1));
            // setLong(columnName, value)
            row.setLong("approval", (rowVal + 2));
            Assertions.assertEquals((rowVal + 2), row.getLong("approval"));
        } 
    }

    @Test
    public void testSetShort() throws IOException {
        ColumnType[] types = new ColumnType[]{ ColumnType.LOCAL_DATE, ColumnType.SHORT, ColumnType.STRING };
        Table table = Table.read().csv(CsvReadOptions.builder(new File("../data/bush.csv")).minimizeColumnSizes(true).columnTypes(types));
        Row row = new Row(table);
        while (row.hasNext()) {
            row.next();
            Short rowVal = table.shortColumn(1).getShort(row.getRowNumber());
            // setShort(columnIndex, value)
            row.setShort(1, ((short) (rowVal + 1)));
            Assertions.assertEquals(((short) (rowVal + 1)), row.getShort(1));
            // setShort(columnName, value)
            row.setShort("approval", ((short) (rowVal + 2)));
            Assertions.assertEquals((rowVal + 2), row.getShort("approval"));
        } 
    }

    @Test
    public void testSetString() throws IOException {
        Table table = Table.read().csv("../data/bush.csv");
        Row row = new Row(table);
        while (row.hasNext()) {
            row.next();
            String rowVal = table.stringColumn(2).get(row.getRowNumber());
            String updateVal1 = rowVal.concat("2");
            String updateVal2 = rowVal.concat("3");
            // setString(columnIndex, value)
            row.setString(2, updateVal1);
            Assertions.assertEquals(updateVal1, row.getString(2));
            // setString(columnName, value)
            row.setString("who", updateVal2);
            Assertions.assertEquals(updateVal2, row.getString("who"));
        } 
    }

    @Test
    public void testSetText() throws IOException {
        ColumnType[] types = new ColumnType[]{ ColumnType.LOCAL_DATE, ColumnType.SHORT, ColumnType.TEXT };
        Table table = Table.read().csv(CsvReadOptions.builder(new File("../data/bush.csv")).minimizeColumnSizes(true).columnTypes(types));
        Row row = new Row(table);
        while (row.hasNext()) {
            row.next();
            String rowVal = table.textColumn(2).get(row.getRowNumber());
            String updateVal1 = rowVal.concat("2");
            String updateVal2 = rowVal.concat("3");
            // setText(columnIndex, value)
            row.setText(2, updateVal1);
            Assertions.assertEquals(updateVal1, row.getText(2));
            // setText(columnName, value)
            row.setText("who", updateVal2);
            Assertions.assertEquals(updateVal2, row.getText("who"));
        } 
    }

    @Test
    public void testSetTime() throws IOException {
        ColumnType[] types = new ColumnType[]{ ColumnType.LOCAL_DATE, ColumnType.LOCAL_TIME, ColumnType.STRING, ColumnType.STRING, ColumnType.SHORT, ColumnType.SHORT, ColumnType.SHORT, ColumnType.DOUBLE, ColumnType.DOUBLE, ColumnType.DOUBLE, ColumnType.DOUBLE };
        Table table = Table.read().csv(CsvReadOptions.builder(new File("../data/rev_tornadoes_1950-2014.csv")).columnTypes(types).minimizeColumnSizes(true));
        Row row = new Row(table);
        while (row.hasNext()) {
            row.next();
            LocalTime dttm_less5 = table.timeColumn("Time").get(row.getRowNumber()).minusHours(5);
            row.setTime(1, dttm_less5);
            Assertions.assertEquals(dttm_less5, row.getTime(1));
            LocalTime dttm_add5 = table.timeColumn("Time").get(row.getRowNumber()).plusHours(5);
            row.setTime("Time", dttm_add5);
            Assertions.assertEquals(dttm_add5, row.getTime("Time"));
        } 
    }
}
