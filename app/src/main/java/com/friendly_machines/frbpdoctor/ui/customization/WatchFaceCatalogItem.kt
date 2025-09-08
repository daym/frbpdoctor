package com.friendly_machines.frbpdoctor.ui.customization

import com.google.gson.annotations.SerializedName

data class WatchfaceCatalogItem(
    @SerializedName("dialplateId")
    val dialplateId: String,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("width")
    val width: String = "240",
    
    @SerializedName("height")
    val height: String = "240",
    
    @SerializedName("deviceType")
    val deviceType: String = "S22"
) {
    fun getDialPlateIdInt(): Int = dialplateId.toIntOrNull() ?: 0
    fun getWidthInt(): Int = width.toIntOrNull() ?: 240
    fun getHeightInt(): Int = height.toIntOrNull() ?: 240
    
    fun getBinFileName(): String = "${name}.bin"
    fun getPngFileName(): String = "${name}.png"
}

data class WatchfaceCatalog(
    @SerializedName("code")
    val code: Int,
    
    @SerializedName("message") 
    val message: String,
    
    @SerializedName("data")
    val data: List<WatchfaceCatalogItem>
)