package com.cesoft.rawagent.remote.entity.gas

import com.google.gson.annotations.SerializedName

data class CountyDto(
    @SerializedName("IDMunicipio")
    val id: Int,
    @SerializedName("Municipio")
    val name: String,
) {
    //fun toEntity() = AddressCounty(id = id, name = name)
}