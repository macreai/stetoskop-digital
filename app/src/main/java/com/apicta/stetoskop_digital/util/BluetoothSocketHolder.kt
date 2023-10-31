package com.apicta.stetoskop_digital.util

import android.bluetooth.BluetoothSocket
import android.util.Log

object BluetoothSocketHolder {
    private var bluetoothSocket: BluetoothSocket? = null

    fun setBluetoothSocket(socket: BluetoothSocket) {
        bluetoothSocket = socket
//        Log.d("BluetoothSocketHolder", "setBluetoothSocket: $bluetoothSocket")
    }

    fun getBluetoothSocket(): BluetoothSocket? {
        return bluetoothSocket
    }
}