package com.friendly_machines.frbpdoctor.ui.customization

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.google.gson.Gson
import java.io.IOException

object WatchFaceCatalogLoader {
    
    fun loadCatalog(context: Context): List<WatchfaceCatalogItem> {
        return try {
            val json = context.assets.open("dials/list.json").bufferedReader().use { it.readText() }
            val catalog = Gson().fromJson(json, WatchfaceCatalog::class.java)
            catalog.data
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    fun loadWatchfaceBinary(context: Context, item: WatchfaceCatalogItem): ByteArray? {
        return try {
            context.assets.open("dials/${item.getBinFileName()}").use { it.readBytes() }
        } catch (e: IOException) {
            null
        }
    }
    
    fun loadWatchfacePreview(context: Context, item: WatchfaceCatalogItem): Bitmap? {
        return try {
            context.assets.open("dials/${item.getPngFileName()}").use { inputStream ->
                BitmapFactory.decodeStream(inputStream)
            }
        } catch (e: IOException) {
            null
        }
    }
}