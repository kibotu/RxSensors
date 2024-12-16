package com.gvillani.rxsensors.internal;

import android.hardware.Sensor;

import androidx.annotation.RestrictTo;

import com.gvillani.rxsensors.exceptions.SensorNotFoundException;

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public final class Preconditions {
    public static void checkNotNull(Object value, String message) {
        if (value == null) {
            throw new NullPointerException(message);
        }
    }

    public static void checkSensorExists(Sensor sensor) {
        if (sensor == null) {
            throw new SensorNotFoundException();
        }
    }

    private Preconditions() {
        throw new AssertionError("No instances.");
    }
}
