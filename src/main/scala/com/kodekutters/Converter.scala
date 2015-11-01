package com.kodekutters

import com.scalakml.io.KmlFileReader
import com.scalakml.kml._
import com.scalakml.{kml => KML}
import play.api.libs.json.Json

import scala.collection.immutable.Seq


/**
  * convert a Kml file into a GeoJSON representation
  *
  * @author R. Wathelet
  *
  * ref: https://developers.google.com/kml/documentation/kmlreference
  * ref: http://geojson.org/geojson-spec.html
  */
object Converter {

  def main(args: Array[String]) {
    val usage =
      """Usage: Converter kml_file geojson_file
        |example: Convert Sydney.kml Sydney.geojson""".stripMargin
    if (args.length == 0)
      println(usage)
    else {
      if (args.nonEmpty) {
        if (args(0).toLowerCase.endsWith("kml")) kmlToGeoJson(args(0))
        if (args(0).toLowerCase.endsWith("kmz")) kmzToGeoJson(args(0))
      }
    }
  }

  def kmlToGeoJson(fileName: String) = {
    val kml = new KmlFileReader().getKmlFromFile(fileName)
    val geojson = KmlConverter().toGeoJson(kml)
    geojson.foreach(obj => println("geojson obj: \n" + obj))
    //  println("\n")
    //  geojson.foreach(obj => println("geojson json: \n" + Json.toJson(obj)))
  }

  def kmzToGeoJson(fileName: String) = {
    val kml = new KmlFileReader().getKmlFromFile(fileName)
    val geojson = KmlConverter().toGeoJson(kml)
    geojson.foreach(obj => println("geojson obj: \n" + obj))
    //  println("\n")
    //  geojson.foreach(obj => println("geojson json: \n" + Json.toJson(obj)))
  }

}

