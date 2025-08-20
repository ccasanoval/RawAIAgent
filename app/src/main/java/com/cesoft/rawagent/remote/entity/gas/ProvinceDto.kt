package com.cesoft.rawagent.remote.entity.gas

import com.google.gson.annotations.SerializedName

data class ProvinceDto(
    @SerializedName("IDPovincia")
    val id: Int,
    @SerializedName("Provincia")
    val name: String,
) {
    //fun toEntity() = AddressProvince(id = id, name = name)
}