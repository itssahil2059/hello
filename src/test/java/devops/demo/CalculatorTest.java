package devops.demo;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.junit.jupiter.api.*; // BeforeAll, BeforeEach, AfterEach, AfterAll, TestInstance, etc.
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

@TestInstance(Lifecycle.PER_CLASS)
class CalculatorTest {

    private final Calculator calc = new Calculator();
    private static final String USER = "sahil";

    private final AtomicInteger counter = new AtomicInteger(0);
    private final ThreadLocal<Integer> current = new ThreadLocal<>();

    @BeforeAll
    void beforeAll() {
        System.out.println("[Before All] Calculator Test suite starting ... by " + USER);
    }

    @BeforeEach
    void beforeEach(TestInfo info) {
        int n = counter.incrementAndGet();
        current.set(n);
        System.out.println("[Before Each] Starting Test #" + n + ": " + info.getDisplayName() + " by " + USER);
    }

    @AfterEach
    void afterEach(TestInfo info) {
        int n = current.get();
        System.out.println("[After Each] Finished Test #" + n + ": " + info.getDisplayName() + " by " + USER);
        current.remove();
    }

    @AfterAll
    void afterAll() {
        System.out.println("[After All] completed " + counter.get() + " test invocations by " + USER + ".");
    }

    // ---------- add: @MethodSource ----------
    static Stream<Arguments> addData() {
        return Stream.of(
            arguments(100,  2,  102),
            arguments(100, -2,   98),
            arguments(-100, 2,  -98),
            arguments(-100,-2, -102)
        );
    }

    @DisplayName("add(a,b)=expected")
    @ParameterizedTest(name = "{index}: {0} + {1} = {2}")
    @MethodSource("addData")
    void add_works(int a, int b, int expected) {
        assertEquals(expected, calc.add(a, b));
    }

    // ---------- substract: @CsvSource ----------
    @DisplayName("substract(a,b)=expected")
    @ParameterizedTest(name = "{index}: {0} - {1} = {2}")
    @CsvSource({
        "100, 2,   98",
        "100, -2, 102",
        "-100, 2, -102",
        "-100, -2, -98"
    })
    void substract_works(int a, int b, int expected) {
        assertEquals(expected, calc.substract(a, b));
    }

    // ---------- multiple: @CsvFileSource ----------
    @DisplayName("multiple(a,b)=expected (from CSV)")
    @ParameterizedTest(name = "{index}: {0} * {1} = {2}")
    @CsvFileSource(resources = "/multiply.csv", numLinesToSkip = 1)
    void multiple_works(int a, int b, int expected) {
        assertEquals(expected, calc.multiple(a, b));
    }

    // ---------- divide: one negative test ----------
    @Test
    @DisplayName("divide by zero throws")
    void divide_by_zero_throws() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> calc.divide(123, 0));
        assertTrue(ex.getMessage().toLowerCase().contains("zero"));
    }
}
