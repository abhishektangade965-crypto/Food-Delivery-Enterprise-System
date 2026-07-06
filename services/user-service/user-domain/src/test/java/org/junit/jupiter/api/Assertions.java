package org.junit.jupiter.api;

public class Assertions {
    public static void assertEquals(Object expected, Object actual) {
        if (expected == null && actual == null) return;
        if (expected != null && expected.equals(actual)) return;
        throw new AssertionError("Expected: " + expected + ", but got: " + actual);
    }

    public static void assertNotNull(Object actual) {
        if (actual == null) {
            throw new AssertionError("Expected actual to be not null");
        }
    }

    public static void assertThrows(Class<? extends Throwable> expectedType, Runnable executable) {
        try {
            executable.run();
        } catch (Throwable t) {
            if (expectedType.isInstance(t)) {
                return;
            }
            throw new AssertionError("Expected exception of type " + expectedType.getName() + " but got " + t.getClass().getName());
        }
        throw new AssertionError("Expected exception of type " + expectedType.getName() + " but no exception was thrown");
    }
}
