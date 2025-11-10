package devops.demo;

public class Calculator {
    public int add(int value1, int value2) { return value1 + value2; }
    public int substract(int value1, int value2) { return value1 - value2; }
    public int multiple(int value1, int value2) { return value1 * value2; }
    public int divide(int value1, int value2) {
        if (value2 == 0) throw new IllegalArgumentException("Denominator value cannot be zero.");
        return value1 / value2;
    }
    
}
