package com.kodekutters

import play.api.libs.json._
import play.extras.geojson.{NamedCrs, CrsFormat}

import scala.collection.immutable.Seq

/**
  * A latitude longitude and altitude CRS, for use with WGS84 ( == EPSG:4326).
  * Extension of the original code play.extras.geojson.LatLng see https://github.com/jroper/play-geojson
  * @param lat The latitude in decimal degrees.
  * @param lng The longitude in decimal degrees.
  * @param alt The altitude in meters, default relative to the WGS84 ellipsoid,
  *            but can be interpreted according to the AltitudeMode of a Feature
  */
case class LatLngAlt(lat: Double, lng: Double, alt: Option[Double] = None) {
  def this(lat: Double, lng: Double, alt: Double) = this(lat, lng, Option(alt))
}

object LatLngAlt {
  implicit val latLngAltFormat: Format[LatLngAlt] = Wgs84Format.format
  implicit val latLngAltCrs: CrsFormat[LatLngAlt] = Wgs84Format
}

/**
  * The WGS84 CRS format. Equals to EPSG:4326 CRS format.
  * for GeoJSON read and write (lng,lat,alt) not (lat,lng,alt)
  */
object Wgs84Format extends CrsFormat[LatLngAlt] {
  val crs = NamedCrs("urn:ogc:def:crs:OGC:1.3:CRS84")
  val format = Format[LatLngAlt](
    __.read[Seq[Double]].map {
          case Seq(lng, lat, alt) => LatLngAlt(lat, lng, Some(alt))
          case Seq(lng, lat) => LatLngAlt(lat, lng)
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
