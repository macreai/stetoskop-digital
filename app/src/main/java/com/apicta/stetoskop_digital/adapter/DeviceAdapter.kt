package com.apicta.stetoskop_digital.adapter

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.apicta.stetoskop_digital.databinding.ItemDeviceBinding
import com.apicta.stetoskop_digital.listener.DeviceListener

@SuppressLint("MissingPermission")
class DeviceAdapter(private val devices: ArrayList<BluetoothDevice>): RecyclerView.Adapter<DeviceAdapter.ViewHolder>() {

    private var deviceListener: DeviceListener? = null

    fun setOnClickListener(deviceListener: DeviceListener){
        this.deviceListener = deviceListener
    }

    class ViewHolder(private val binding: ItemDeviceBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(device: BluetoothDevice){
            binding.tvDeviceName.text = device.name
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(ItemDeviceBinding.inflate(LayoutInflater.from(parent.context), parent, false))


    override fun getItemCount(): Int = devices.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
       holder.bind(devices[position])

        holder.itemView.setOnClickListener {
            deviceListener?.onClick(devices[holder.adapterPosition])
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun addDevice(device: BluetoothDevice){
        devices.add(device)
        notifyDataSetChanged()
    }

}