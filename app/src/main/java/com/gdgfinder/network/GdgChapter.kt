package com.gdgfinder.network

import android.os.Parcelable
import com.squareup.moshi.Json
import kotlinx.android.parcel.Parcelize

@Parcelize
data class GdgChapter(

    @Json(name = "chapter_name")
    val name: String,
    @Json(name = "cityarea")
    val city: String,
    @Json(name = "country")
    val country: String,
    @Json(name = "region")
    val region: String,
    @Json(name = "website")
    val website: String,
    @Json(name = "geo")
    val geo: LatLong

): Parcelable

@Parcelize
data class LatLong(

    @Json(name = "lat")
    val lat: Double,
    @Json(name = "lng")
    val long: Double

) : Parcelable

@Parcelize
data class GdgResponse(

    @Json(name = "filters_")
    val filters: Filter,
    @Json(name = "data")
    val chapters: List<GdgChapter>

): Parcelable

@Parcelize
data class Filter(
    @Json(name = "region")
    val regions: List<String>
): Parcelable
