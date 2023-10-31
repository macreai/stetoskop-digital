package com.apicta.stetoskop_digital.viewmodel

import android.bluetooth.BluetoothSocket
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class BluetoothViewModel: ViewModel() {
    private val _bluetoothName = MutableLiveData<String?>()
    val bluetoothName: LiveData<String?> = _bluetoothName

    fun setBluetoothName(deviceName: String){
        _bluetoothName.value = deviceName
        Log.d(TAG, "setBluetoothName: $bluetoothName")
    }

    fun destroyBluetoothName(){
        _bluetoothName.value = null
    }

    companion object{
        private const val TAG = "BluetoothViewModel"
    }
}