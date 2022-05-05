package edu.ivytech.runtrackersp22

import androidx.lifecycle.ViewModel

class MapsViewModel : ViewModel() {
    val locationListLiveData = LocationRepository.get().getLocations()
}