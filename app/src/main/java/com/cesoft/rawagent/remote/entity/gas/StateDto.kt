package com.cesoft.rawagent.remote.entity.gas

import com.google.gson.annotations.SerializedName

data class StateDto(
    @SerializedName("IDCCAA")
    val id: Int,
    @SerializedName("CCAA")
    val name: String,
) {
    //fun toEntity() = AddressState(id = id, name = name)
}
