package cn.jimmiez.pcu;

import static org.junit.Assert.*;

public class CommonAssertions {

    private static final double COMPARE_DOUBLE_TOLERANCE = 1E-5;

    public static void assertLessThan(double v1, double v2) {
        String message = "The value " + v1 + " should be less than " + v1 + ".";
        assertTrue(message, v1 <= v2);
    }

    public static void assertLessEqualThan(double v1, double v2) {
        assertLessEqualThan(v1, v2, COMPARE_DOUBLE_TOLERANCE);
    }

    public static void assertLessEqualThan(double v1, double v2, double tolerance) {
        String message = "The value " + v1 + " should be less than " + v1 + ".";
        assertTrue(message, v1 < v2 || Math.abs(v1 - v2) <= tolerance);
    }

    public static void assertLessThan(int v1, int v2) {
        String message = "The value " + v1 + " should be less than " + v1 + ".";
        assertTrue(message, v1 < v2);
    }

    public static void assertLessEqualThan(int v1, int v2) {
        String message = "The value " + v1 + " should be not bigger than " + v1 + ".";
        assertTrue(message, v1 <= v2);
    }

    public static void assertGreaterThan(int v1, int v2) {
        String message = "The value " + v1 + " should be larger than " + v1 + ".";
        assertTrue(message, v1 > v2);
    }

    public static void assertGreaterEqualThan(int v1, int v2) {
        String message = "The value " + v1 + " should be not less than " + v1 + ".";
        assertTrue(message, v1 >= v2);
    }

}
