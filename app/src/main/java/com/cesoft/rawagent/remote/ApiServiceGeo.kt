package com.cesoft.rawagent.remote

import retrofit2.http.GET
import retrofit2.http.Query

//{
// "type":"FeatureCollection",
// "features":[
// {
// "type":"Feature",
// "geometry":{"type":"Point","coordinates":[-0.211773,39.666926]},
// "properties":{
//      "country_code":"es",
//      "housenumber":"61",
//      "street":"Calle Isla De Corcega",
//      "country":"Spain",
//      "datasource":{"sourcename":"openaddresses","attribution":"© OpenAddresses contributors","license":"BSD-3-Clause License"},
//      "postcode":"46520",
//      "state":"Valencian Community",
//      "district":"Sagunto/Sagunt",
//      "city":"Port de Sagunt",
//      "county":"Valencia",
//      "county_code":"V",
//      "lon":-0.211773,"lat":39.666926,
//      "distance":21.395602107308676,
//      "result_type":"building",
//      "formatted":"Calle Isla De Corcega, 61, 46520 Port de Sagunt, Spain",
//      "address_line1":"Calle Isla De Corcega, 61",
//      "address_line2":"46520 Port de Sagunt, Spain",
//      "timezone":{"name":"Europe/Madrid","offset_STD":"+01:00","offset_STD_seconds":3600,"offset_DST":"+02:00","offset_DST_seconds":7200,
//      "abbreviation_STD":"CET","abbreviation_DST":"CEST"},
//      "plus_code":"8CFXMQ8Q+Q7", "rank":{"popularity":4.629528200489351},
//      "place_id":"51e08096ae601bcbbf59116dc7d45dd54340c00203e203476f70656e6164647265737365733a616464726573733a65732f636f756e747279776964652d6164647265737365732d636f756e7472793a37346539613361356539313230343338"}}
//      ],
//      "query":{"lat":39.6670267,"lon":-0.21156,"plus_code":"8CFXMQ8Q+R9"}
// }
data class ReverseGeoDto(
    val features: List<FeatureDto>,
)
data class FeatureDto(
    val properties: PropertiesDto
)
data class PropertiesDto(
    val formatted: String,
)

//https://api.geoapify.com/v1/geocode/reverse?lat=51.21709661403662&lon=6.7782883744862374&apiKey=476a20ef5a4d476cb899731a49c343cf
interface ApiServiceGeo {
    @GET("/v1/geocode/reverse")
    suspend fun reverseGeocode(
        @Query("apiKey") apiKey: String,
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
    ): Result<ReverseGeoDto>
}
