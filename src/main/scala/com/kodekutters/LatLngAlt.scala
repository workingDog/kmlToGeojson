package com.kodekutters

import play.api.libs.json._
import play.extras.geojson.{NamedCrs, CrsFormat}

import scala.collection.immutable.Seq

/**
  * A latitude longitude and altitude CRS, for use with WGS84 ( == EPSG:4326).
  * Extension of play.extras.geojson.LatLng see https://github.com/jroper/play-geojson
  * @param lat The latitude.
  * @param lng The longitude.
  * @param alt The altitude (relative to the WGS84 ellipsoid ?)
  */
case class LatLngAlt(lat: Double, lng: Double, alt: Option[Double])

object LatLngAlt {
  implicit val latLngAltFormat: Format[LatLngAlt] = Wgs84Format.format
  implicit val latLngAltCrs: CrsFormat[LatLngAlt] = Wgs84Format
}

/**
  * The WGS84 CRS format. Equals to EPSG:4326 CRS format.
  */
object Wgs84Format extends CrsFormat[LatLngAlt] {
  val crs = NamedCrs("urn:ogc:def:crs:OGC:1.3:CRS84")
  val format = Format[LatLngAlt](
    __.read[Seq[Double]].map {
          case Seq(lng, lat, alt) => LatLngAlt(lat, lng, Some(alt))
          case Seq(lng, lat) => LatLngAlt(lat, lng, None)
    },
    Writes(latLngAlt => {
      latLngAlt.alt match {
        case None => Json.arr(latLngAlt.lng, latLngAlt.lat)
        case Some(alt) => Json.arr(latLngAlt.lng, latLngAlt.lat, alt)
      }
    })
  )

  override def isDefault = true
}
