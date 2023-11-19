package com.apicta.stetoskop_digital.listener

import com.apicta.stetoskop_digital.model.remote.response.FileItem

interface FileListener {
    fun onClick(file: FileItem)
}