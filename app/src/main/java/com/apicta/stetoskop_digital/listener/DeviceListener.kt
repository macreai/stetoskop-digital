package com.apicta.stetoskop_digital.listener

import android.bluetooth.BluetoothDevice

interface DeviceListener {
    fun onClick(device: BluetoothDevice)
}