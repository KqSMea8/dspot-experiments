package com.baeldung.arraycopy;


import com.baeldung.arraycopy.model.Address;
import com.baeldung.arraycopy.model.Employee;
import java.util.Arrays;
import java.util.function.IntFunction;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.Assert;
import org.junit.Test;


public class ArrayCopyUtilUnitTest {
    private static Employee[] employees;

    private static final int MAX = 2;

    @Test
    public void givenArrayOfPrimitiveType_whenCopiedViaSystemsArrayCopy_thenSuccessful() {
        int[] array = new int[]{ 23, 43, 55 };
        int[] copiedArray = new int[3];
        System.arraycopy(array, 0, copiedArray, 0, 3);
        Assert.assertArrayEquals(copiedArray, array);
    }

    @Test
    public void givenArrayOfPrimitiveType_whenCopiedSubSequenceViaSystemsArrayCopy_thenSuccessful() {
        int[] array = new int[]{ 23, 43, 55, 12, 65, 88, 92 };
        int[] copiedArray = new int[3];
        System.arraycopy(array, 2, copiedArray, 0, 3);
        Assert.assertTrue((3 == (copiedArray.length)));
        Assert.assertTrue(((copiedArray[0]) == (array[2])));
        Assert.assertTrue(((copiedArray[1]) == (array[3])));
        Assert.assertTrue(((copiedArray[2]) == (array[4])));
    }

    @Test
    public void givenArrayOfPrimitiveType_whenCopiedSubSequenceViaArraysCopyOfRange_thenSuccessful() {
        int[] array = new int[]{ 23, 43, 55, 12, 65, 88, 92 };
        int[] copiedArray = Arrays.copyOfRange(array, 1, 4);
        Assert.assertTrue((3 == (copiedArray.length)));
        Assert.assertTrue(((copiedArray[0]) == (array[1])));
        Assert.assertTrue(((copiedArray[1]) == (array[2])));
        Assert.assertTrue(((copiedArray[2]) == (array[3])));
    }

    @Test
    public void givenArrayOfPrimitiveType_whenCopiedViaArraysCopyOf_thenValueChangeIsSuccessful() {
        int[] array = new int[]{ 23, 43, 55, 12 };
        int newLength = array.length;
        int[] copiedArray = Arrays.copyOf(array, newLength);
        Assert.assertArrayEquals(copiedArray, array);
        array[0] = 9;
        Assert.assertTrue(((copiedArray[0]) != (array[0])));
        copiedArray[1] = 12;
        Assert.assertTrue(((copiedArray[1]) != (array[1])));
    }

    @Test
    public void givenArrayOfNonPrimitiveType_whenCopiedViaArraysCopyOf_thenDoShallowCopy() {
        Employee[] copiedArray = Arrays.copyOf(ArrayCopyUtilUnitTest.employees, ArrayCopyUtilUnitTest.employees.length);
        Assert.assertArrayEquals(copiedArray, ArrayCopyUtilUnitTest.employees);
        ArrayCopyUtilUnitTest.employees[0].setName(((ArrayCopyUtilUnitTest.employees[0].getName()) + "_Changed"));
        // change in employees' element caused change in the copied array
        Assert.assertTrue(copiedArray[0].getName().equals(ArrayCopyUtilUnitTest.employees[0].getName()));
    }

    @Test
    public void givenArrayOfPrimitiveType_whenCopiedViaArrayClone_thenValueChangeIsSuccessful() {
        int[] array = new int[]{ 23, 43, 55, 12 };
        int[] copiedArray = array.clone();
        Assert.assertArrayEquals(copiedArray, array);
        array[0] = 9;
        Assert.assertTrue(((copiedArray[0]) != (array[0])));
        copiedArray[1] = 12;
        Assert.assertTrue(((copiedArray[1]) != (array[1])));
    }

    @Test
    public void givenArraysOfNonPrimitiveType_whenCopiedViaArrayClone_thenDoShallowCopy() {
        Employee[] copiedArray = ArrayCopyUtilUnitTest.employees.clone();
        Assert.assertArrayEquals(copiedArray, ArrayCopyUtilUnitTest.employees);
        ArrayCopyUtilUnitTest.employees[0].setName(((ArrayCopyUtilUnitTest.employees[0].getName()) + "_Changed"));
        // change in employees' element changed the copied array
        Assert.assertTrue(copiedArray[0].getName().equals(ArrayCopyUtilUnitTest.employees[0].getName()));
    }

    @Test
    public void givenArraysOfCloneableNonPrimitiveType_whenCopiedViaArrayClone_thenDoShallowCopy() {
        Address[] addresses = createAddressArray();
        Address[] copiedArray = addresses.clone();
        addresses[0].setCity(((addresses[0].getCity()) + "_Changed"));
        Assert.assertArrayEquals(copiedArray, addresses);
    }

    @Test
    public void givenArraysOfSerializableNonPrimitiveType_whenCopiedViaSerializationUtils_thenDoDeepCopy() {
        Employee[] copiedArray = SerializationUtils.clone(ArrayCopyUtilUnitTest.employees);
        ArrayCopyUtilUnitTest.employees[0].setName(((ArrayCopyUtilUnitTest.employees[0].getName()) + "_Changed"));
        // change in employees' element didn't change in the copied array
        Assert.assertFalse(copiedArray[0].getName().equals(ArrayCopyUtilUnitTest.employees[0].getName()));
    }

    @Test
    public void givenArraysOfNonPrimitiveType_whenCopiedViaStream_thenDoShallowCopy() {
        Employee[] copiedArray = Arrays.stream(ArrayCopyUtilUnitTest.employees).toArray(Employee[]::new);
        Assert.assertArrayEquals(copiedArray, ArrayCopyUtilUnitTest.employees);
        ArrayCopyUtilUnitTest.employees[0].setName(((ArrayCopyUtilUnitTest.employees[0].getName()) + "_Changed"));
        // change in employees' element didn't change in the copied array
        Assert.assertTrue(copiedArray[0].getName().equals(ArrayCopyUtilUnitTest.employees[0].getName()));
    }

    @Test
    public void givenArraysOfPrimitiveType_whenCopiedViaStream_thenSuccessful() {
        String[] strArray = new String[]{ "orange", "red", "green'" };
        String[] copiedArray = Arrays.stream(strArray).toArray(String[]::new);
        Assert.assertArrayEquals(copiedArray, strArray);
    }
}
