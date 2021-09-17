package com.gdgfinder.search

import android.app.Application
import android.content.Context
import android.location.Location
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.*
import com.gdgfinder.network.GdgApi
import com.gdgfinder.network.GdgChapter
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.Exception


class GdgListViewModel: ViewModel() {

    private val repository = GdgChapterRepository(GdgApi.retrofitService)

    private var filter = FilterHolder()

    private var currentJob: Job? = null

    private val _gdgList = MutableLiveData<List<GdgChapter>>()
    val gdgList: LiveData<List<GdgChapter>>
        get() = _gdgList

    private val _regionList = MutableLiveData<List<String>>()
    val regionList: LiveData<List<String>>
        get() = _regionList

    private val _showNeedLocation = MutableLiveData<Boolean>()
    val showNeedLocation: LiveData<Boolean>
        get() = _showNeedLocation

    private val _checkNetworkConnection = MutableLiveData<Boolean>(false)
    val checkNetworkConnection : LiveData<Boolean>
        get() = _checkNetworkConnection

    init {
        // process the initial filter
        onQueryChanged()

    }

    private fun onQueryChanged() {
        try {
            currentJob?.cancel() // if a previous query is running cancel it before starting another
            currentJob = viewModelScope.launch {
                try {
                    // this will run on a thread managed by Retrofit
                    _gdgList.value = repository.getChaptersForFilter(filter.currentValue) // return list
                    Log.v("Checku returned", _gdgList.value!!.get(0).toString())
                    Log.v("Checku FILTER", repository.getFilters().toString())
                    repository.getFilters().let {
                        // only update the filters list if it's changed since the last time
                        if (it != _regionList.value) {
                            _regionList.value = it
                        }
                    }
                    Log.v("Checku AFTER FILTER", _regionList.value.toString())

                } catch (e: Exception) {
                    _gdgList.value = listOf()
                    Log.v("Checku",e.message.toString())
                    Log.v("Checku",e.localizedMessage+"\n"+e.printStackTrace())
                }
            }
        }catch (t:Throwable){
            Log.v("Checku throwable",t.message.toString())
        }
    }

    fun onLocationUpdated(location: Location) {
        viewModelScope.launch {
            repository.onLocationChanged(location)
            onQueryChanged()
        }
    }

    fun onFilterChanged(filter: String, isChecked: Boolean) {
        if (this.filter.update(filter, isChecked)) {
            onQueryChanged()
        }
    }

    private class FilterHolder {

        var currentValue: String? = null
            private set

        fun update(changedFilter: String, isChecked: Boolean): Boolean {
            if (isChecked) {
                currentValue = changedFilter
                return true
            } else if (currentValue == changedFilter) {
                currentValue = null
                return true
            }
            return false
        }
    }

    fun initializeToCheckLocationEnabledOrNot(){
        viewModelScope.launch {
            delay(5_000)
            _showNeedLocation.value = !repository.isFullyInitialized
        }
    }

    fun internetConnected(){
        _checkNetworkConnection.value = true
    }
    fun internetNotConnected(){
        _checkNetworkConnection.value = false
    }
    override fun onCleared() {
        super.onCleared()
        currentJob?.cancel()
    }
}

