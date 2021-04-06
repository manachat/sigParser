package ru.vafilonov;

import java.util.TreeMap;


/**
 * Class description
 */
public class TestClass1 {

    /**
     * public field
     */
    public int publicField;

    public double fieldWithoutDoc;

    /**
     * private field
     */
    private char privateField;

    /**
     * static field
     */
    static boolean staticField;

    /**
     * Default constructor
     */
    public TestClass1() {

    }

    /**
     * Constructor with methods
     * @param value1
     * @param value2
     */
    public TestClass1(int value1, double value2) {

    }

    /**
     * private const
     */
    private TestClass1(String s) {

    }

    /**
     *Gets sum
     * @param a
     * @param b
     * @return
     */
    public int publicMethod(int a, int b) {
        return 0;
    }

    /**
     * private method
     */
    private void privateMethod() {

    }

    /**
     * static method
     * @param builder
     * @return
     */
    protected static TreeMap<Integer, String> protectedStaticMethod(StringBuilder builder) {
        return  null;
    }

    public void methodWithoutDoc() {

    }

    /**
     * Some inner class
     */
    public static class InnerClass {

    }

    /**
     * Inner enum doc
     */
    public enum InnerEnum {
        ONE,
        TWO,
        THREE
    }


}
