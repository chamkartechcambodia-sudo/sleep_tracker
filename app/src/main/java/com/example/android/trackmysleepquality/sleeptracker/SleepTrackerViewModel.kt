package com.example.android.trackmysleepquality.sleeptracker

import android.app.Application
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.AndroidViewModel
import com.example.android.trackmysleepquality.database.SleepDatabaseDao

/**
 * ViewModel for SleepTrackerFragment.
 */
class SleepTrackerViewModel(
        val database: SleepDatabaseDao,
        application: Application) : AndroidViewModel(application) {

    //TODO (01) Declare Job() and cancel jobs in onCleared().

    //TODO (02) Define uiScope for coroutines.

    //TODO (03) Create a MutableLiveData variable tonight for one SleepNight.

    //TODO (04) Define a variable, nights. Then getAllNights() from the database
    //and assign to the nights variable.

    //TODO (05) In an init block, initializeTonight(), and implement it to launch a coroutine
    //to getTonightFromDatabase().

    //TODO (06) Implement getTonightFromDatabase()as a suspend function.

    //TODO (07) Implement the click handler for the Start button, onStartTracking(), using
    //coroutines. Define the suspend function insert(), to insert a new night into the database.

    //TODO (08) Create onStopTracking() for the Stop button with an update() suspend function.

    //TODO (09) For the Clear button, created onClear() with a clear() suspend function.

    //TODO (12) Transform nights into a nightsString using formatNights().

}

