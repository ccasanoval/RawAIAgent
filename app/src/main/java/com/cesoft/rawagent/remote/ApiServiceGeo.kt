package com.cesoft.rawagent.remote

import com.cesoft.rawagent.remote.entity.GeoDto
import retrofit2.http.GET
import retrofit2.http.Query

//https://api.geoapify.com/v1/geocode/reverse?lat=51.21709661403662&lon=6.7782883744862374&apiKey=476a20ef5a4d476cb899731a49c343cf
interface ApiServiceGeo {
    @GET("/v1/geocode/reverse")
    suspend fun reverseGeocode(
        @Query("apiKey") apiKey: String,
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
    ): Result<GeoDto>
}
