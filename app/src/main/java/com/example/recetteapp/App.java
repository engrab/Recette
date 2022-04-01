package com.example.recetteapp;

import android.app.Application;

import com.mazenrashed.printooth.Printooth;


public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Printooth.INSTANCE.init(this);


    }
}
