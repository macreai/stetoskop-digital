package com.apicta.stetoskop_digital.listener

import android.bluetooth.BluetoothDevice

interface PairedDeviceListener {
    fun onClick(device: BluetoothDevice)
}