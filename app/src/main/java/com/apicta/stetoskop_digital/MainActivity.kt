package com.apicta.stetoskop_digital

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
import com.apicta.stetoskop_digital.viewmodel.BluetoothViewModel

class MainActivity : AppCompatActivity() {

    private val bluetoothViewModel: BluetoothViewModel by viewModels()

//    private val bluetooth = Bluetooth(this)
//
//    private val bluetoothCallback: BluetoothCallback = object : BluetoothCallback {
//        override fun onBluetoothTurningOn() {}
//        override fun onBluetoothTurningOff() {}
//        override fun onBluetoothOff() {}
//        override fun onBluetoothOn() {
//            Toast.makeText(this@MainActivity, "Bluetooth Already Turn On", Toast.LENGTH_SHORT).show()
//        }
//
//        override fun onUserDeniedActivation() {
//            finish()
//        }
//    }

//    private lateinit var bluetooth: Bluetooth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        bluetooth = Bluetooth(this)

//        bluetooth.setBluetoothCallback(bluetoothCallback)
    }

    override fun onStart() {
        super.onStart()
//        bluetooth.onStart()
//        if (bluetooth.isEnabled){
//            Toast.makeText(this@MainActivity, "Bluetooth On", Toast.LENGTH_SHORT).show()
//        } else {
//            bluetooth.showEnableDialog(this)
//        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
//        bluetooth.onActivityResult(requestCode, resultCode)

//        if (requestCode == RESULT_OK){
//            Toast.makeText(this, "Bluetooth On", Toast.LENGTH_SHORT).show()
//        }
//
//        if (resultCode == RESULT_CANCELED){
//            Toast.makeText(this, "Bluetooth turn on dialog canceled", Toast.LENGTH_SHORT).show()
//        }
    }

    override fun onResume() {
        super.onResume()
        checkAndRequestLocationPermission()
        requestStoragePermission()
    }

    private fun requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val storagePermission = Manifest.permission.WRITE_EXTERNAL_STORAGE

            if (ContextCompat.checkSelfPermission(this, storagePermission) != PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(storagePermission), REQUEST_CODE_PERMISSION_STORAGE)
            }
        }
    }

    private fun checkAndRequestLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_CODE_PERMISSION_LOCATION
                )
            } else {
                checkGPSAndShowDialog()
            }
        } else {
            checkGPSAndShowDialog()
        }
    }

    private fun checkGPSAndShowDialog() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !checkGPSIsOpen()) {
            AlertDialog.Builder(this)
                .setTitle("Notifikasi")
                .setMessage("GPS diperlukan untuk aplikasi ini.")
                .setNegativeButton("Batal",
                    DialogInterface.OnClickListener { dialog, which -> finish() })
                .setPositiveButton("Pengaturan",
                    DialogInterface.OnClickListener { dialog, which ->
                        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                        startActivityForResult(intent, REQUEST_CODE_OPEN_GPS)
                    })
                .setCancelable(false)
                .show()
        }
    }

    private fun checkGPSIsOpen(): Boolean {
        val locationManager =
            this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                ?: return false
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CODE_PERMISSION_LOCATION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkGPSAndShowDialog()
                } else {
                    finish()
                }
            }
            REQUEST_CODE_PERMISSION_STORAGE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
                } else {
                    finish()
                }
            }
        }
    }

//    private fun showBluetoothDialog() {
//        if (!bluetooth.isEnabled){
//            val enableBluetoothIntent = Intent(Bluetooth.ACTION_REQUEST_ENABLE)
//            startBluetoothIntentForResult.launch(enableBluetoothIntent)
//        }
//    }

    override fun onDestroy() {
        super.onDestroy()
        bluetoothViewModel.destroyBluetoothName()
    }


    companion object {
        private const val REQUEST_CODE_OPEN_GPS = 1
        private const val REQUEST_CODE_PERMISSION_LOCATION = 2
        private val REQUEST_CODE_PERMISSION_STORAGE = 3
        private lateinit var instance: MainActivity

        fun getContext(): Context {
            return instance.applicationContext
        }

    }
}