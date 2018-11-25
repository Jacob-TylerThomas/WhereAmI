package edu.uncw.whereami;

import android.app.Application;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Random;

import io.objectbox.Box;
import io.objectbox.BoxStore;

public class App extends Application {

    private static final int NUM_TO_ADD = 20;

    private static final int maxMillis = 3600000;
    private static final int minMillis = 60000;
    private static final double latMax = 34.2397892;
    private static final double latMin = 33.9979624;
    private static final double lonMax = -77.8055695;
    private static final double lonMin = -77.9490203;
    private static final double accMax = 25.0;
    private static final double accMin = 3.0;

    private BoxStore boxStore;

    @Override
    public void onCreate() {
        super.onCreate();
        boxStore = MyObjectBox.builder().androidContext(App.this).build();

        // Initialize with some data if the database is empty.
        Box<LocationRecording> locationBox = boxStore.boxFor(LocationRecording.class);
        if (locationBox.count() == 0) {
            Random rand = new Random(System.currentTimeMillis());
            Collection<LocationRecording> itemsToAdd = new ArrayList<>();
            long millis = new Date().getTime();

            for (int i = 0; i < NUM_TO_ADD; i++) {
                double lat = (rand.nextDouble() * (latMax - latMin)) + latMin;
                double lon = (rand.nextDouble() * (lonMax - lonMin)) + lonMin;
                double acc = (rand.nextDouble() * (accMax - accMin)) + accMin;
                millis += rand.nextInt(maxMillis - minMillis) + minMillis;
                itemsToAdd.add(new LocationRecording(new Date(millis), lat, lon, acc));
            }

            locationBox.put(itemsToAdd);
        }
    }

    public BoxStore getBoxStore() {
        return boxStore;
    }
}
