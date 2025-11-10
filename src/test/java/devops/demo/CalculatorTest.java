package devops.demo;

import static org.junit.jupiter.api.Assertions.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.junit.jupiter.api.*; // @BeforeAll, etc.
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*; // @CsvSource, @CsvFileSource, @MethodSource

class CalculatorTest {

    private static final String USER = "sahilbhusal";
    private static final AtomicInteger CASE_NO = new AtomicInteger(0);
    private static String lastExpr = "";
    private static String lastResult = "";

    private final Calculator calc = new Calculator();

    @BeforeAll
    static void beforeAll() {
        System.out.println("[Before All] Calculator Test suite starting ... by " + USER);
    }

    @BeforeEach
    void beforeEach() {
        int n = CASE_NO.incrementAndGet();
        System.out.println("[Before Each] Starting Test #" + n + ": " + lastExpr);
    }

    @AfterEach
    void afterEach() {
        int n = CASE_NO.get();
        System.out.println("[After Each] Finished Test #" + n + ": " + lastResult);
        lastExpr = "";
        lastResult = "";
    }

    @AfterAll
    static void afterAll() {
        System.out.println("[After All] completed " + CASE_NO.get() + " test invocations by " + USER + ".");
    }

    /* ---------- add(): @MethodSource ---------- */
    static Stream<Arguments> addData() {
        return Stream.of(
            Arguments.of(100,   2,  102),
            Arguments.of(100,  -2,   98),
            Arguments.of(-100,  2,  -98),
            Arguments.of(-100, -2, -102)
        );
    }

    @ParameterizedTest(name = "add {0} + {1} = {2}")
    @MethodSource("addData")
    void add_two_numbers(int a, int b, int expected) {
        lastExpr = a + " + " + b + " = " + expected;
        int actual = calc.add(a, b);
        lastResult = a + " + " + b + " = " + actual;
        assertEquals(expected, actual);
    }

    /* ---------- substract(): @CsvSource ---------- */
    @ParameterizedTest(name = "sub {0} - {1} = {2}")
    @CsvSource({
        "100, 2, 98",
        "100, -2, 102",
        "-100, 2, -102",
        "-100, -2, -98"
    })
    void subtract_two_numbers(int a, int b, int expected) {
        lastExpr = a + " - " + b + " = " + expected;
        int actual = calc.substract(a, b);
        lastResult = a + " - " + b + " = " + actual;
        assertEquals(expected, actual);
    }

    /* ---------- multiple(): @CsvFileSource ---------- */
    @ParameterizedTest(name = "mul {0} * {1} = {2}")
    @CsvFileSource(resources = "/multiply.csv", numLinesToSkip = 0)
    void multiply_two_numbers(int a, int b, int expected) {
        lastExpr = a + " * " + b + " = " + expected;
        int actual = calc.multiple(a, b);
        lastResult = a + " * " + b + " = " + actual;
        assertEquals(expected, actual);
    }

    /* ---------- divide(): cover BOTH branches + several happy paths ---------- */
    @Test
    @DisplayName("divide: throws on divide by zero (exception branch)")
    void divide_throws_on_zero() {
        lastExpr = "divide_byZero()";
        assertThrows(IllegalArgumentException.class, () -> calc.divide(1, 0));
        lastResult = "divide_byZero() threw IllegalArgumentException";
    }

    @Test
    @DisplayName("divide: non-zero branch fully covered (sign combos)")
    void divide_covers_nonzero_branch() {
        assertEquals(50,  calc.divide(100,  2));
        assertEquals(-50, calc.divide(100, -2));
        assertEquals(-50, calc.divide(-100, 2));
        assertEquals(50,  calc.divide(-100, -2));
        lastResult = "non-zero divide paths covered";
    }

    /* ---------- optional: hit Calculator.main() for last-line coverage ---------- */
    @Test
    @DisplayName("main() runs (no-op) for coverage")
    void main_runs() {
        Calculator.main(new String[0]);
        lastResult = "main() executed";
    }
}
