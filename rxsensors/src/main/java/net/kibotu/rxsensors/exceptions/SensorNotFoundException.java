package net.kibotu.rxsensors.exceptions;

public class SensorNotFoundException extends RuntimeException {
    /**
     * Constructs a {@code SensorNotFoundException} with predefined detail message.
     */
    public SensorNotFoundException() {
        super("Sensor not found");
    }
}