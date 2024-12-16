# RxSensors

[![Maven Central Version](https://img.shields.io/maven-central/v/net.kibotu/RxSensors)](https://central.sonatype.com/artifact/net.kibotu/RxSensors) [![](https://jitpack.io/v/kibotu/RxSensors.svg)](https://jitpack.io/#kibotu/RxSensors) [![Android CI](https://github.com/kibotu/RxSensors/actions/workflows/android.yml/badge.svg)](https://github.com/kibotu/RxSensors/actions/workflows/android.yml) [![API](https://img.shields.io/badge/Min%20API-21%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=21) [![API](https://img.shields.io/badge/Target%20API-35%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=35) [![API](https://img.shields.io/badge/Java-17-brightgreen.svg?style=flat)](https://www.oracle.com/java/technologies/javase/17all-relnotes.html) [![Gradle Version](https://img.shields.io/badge/gradle-8.11.1-green.svg)](https://docs.gradle.org/current/release-notes) [![Kotlin](https://img.shields.io/badge/kotlin-2.1.0-green.svg)](https://kotlinlang.org/)

RxSensors is a simple library, RxJava2 compatible, that acts as a wrapper around the Android Sensor's system, converting the stream of sensor data, into a Flowable that emits the same events, so that you can combine more sources of data for more complex operations.

## Features

* RxJava 2 compatible
* Handle backpressure (using Flowable)
* Provides utilities for filtering the stream of data, so that you can for example filter you data using a Low Pass Filter (LPF) or other types of filters, based for example on the accuracy.
* Built-in high level sensor data measurements: it can provides orientation data (Azimuth, Pitch, Roll).

## Usage

# How to install

## MavenCentral

```groovy 
allprojects {
    repositories {
        mavenCentral()
    }
}

dependencies {
    implementation 'net.kibotu:SequentialImagePlayer:{latest-version}'
}

```

## Jitpack

```groovy
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}

dependencies {
    implementation 'com.github.kibotu:SequentialImagePlayer:{latest-version}'
}
```

### Acquire stream of sensor data:

Let's suppose that we want to:
* acquire acceleration data
* data has at least medium accuracy level (*SENSOR_STATUS_ACCURACY_MEDIUM*)
* handle backpressure, so that the buffer contains not more than 128, and we want other items to be discarded.
* run all this operations in background except the final step (for updating the UI)
* apply a LPF for reducing noise, with a specific alpha parameter (0.2)
* receive only distinct events
* eventually we want the result to be displayed on the UI.

The below snippet of code will do that:

```Java
Disposable disposable = RxSensor.sensorEvent(this, Sensor.TYPE_ACCELEROMETER)
                .subscribeOn(Schedulers.computation())
                .filter(RxSensorFilter.minAccuracy(SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM))
                .onBackpressureBuffer(128, () -> Log.w(TAG, "dropped item!"), BackpressureOverflowStrategy.DROP_LATEST)
                .distinctUntilChanged(RxSensorFilter.uniqueEventValues())
                .compose(RxSensorTransformer.lowPassFilter(0.2F))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(rxSensorEvent -> updateUi(rxSensorEvent));
```

When you are done using the sensor or when the sensor activity pauses, you have to be sure to dispose the returned disposable object:

```Java
disposable.dispose();
```

This will in turn unregister the associated sensor's listener.

You can specify a type of sensor, as in the example, and the library will try for you to determine the correct Sensor (the dafault sensor), or return an error (you can catch it in `onError` in your subscription) in case such sensor is not available. Or you can also pass a specific `Sensor` to the `sensorEvent` method:


```Java
Sensor accelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

RxSensor.sensorEvent(this, accelerometerSensor, SensorManager.SENSOR_DELAY_UI)

```

If we want to obtain orientation data we can use the method `orientationEventWithRemap`

```Java
Disposable disposable = RxSensor.orientationEventWithRemap(this,
                SensorManager.AXIS_X, SensorManager.AXIS_Z, SensorManager.SENSOR_DELAY_FASTEST)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(rxSensorEvent -> updateUi(rxSensorEvent));
```

## Notes

* A disadvantage of using this library is that it can produce a pretty high pressure on the Garbage Collector, expecially for high frequency data acquiring. This is due to the fact that each time an event is sent a new object is created. Further developments will provide a better strategy for handling this condition (maybe using an object pool).
* It is important to note that a filter like the LPF can work fine only if the source of data has no discontinuity. For example for a stream of data that vary in the range -180, +180 it will not work, because of the jump (discontinuity) in the end scale value.
* Most of the filters are applicable for the three "dimensions" (x, y, z), even though not all of the sensor data has exactly three dimensions: for example the Environment sensors have typically just one dimensions, this means that all the filter or elaboration on the second (y) and third (z) dimension will not have any effect.
