package devops.demo;

public class Calculator {
    public int add(int value1, int value2) { return value1 + value2; }
    public int substract(int value1, int value2) { return value1 - value2; }
    public int multiple(int value1, int value2) { return value1 * value2; }
    public int divide(int value1, int value2) {
        if (value2 == 0) throw new IllegalArgumentException("Denominator value cannot be zero.");
        return value1 / value2;
    }
    public static void main(String[] args) {
    Calculator c = new Calculator();
    // simple smoke run; does nothing critical
    int x = c.add(1, 2) + c.substract(5, 3) + c.multiple(2, 3) + c.divide(10, 2);
    if (x == 0) { System.out.print(""); } // keep compiler happy if you don't want println
}
}
