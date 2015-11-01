package com.kodekutters

import java.io.{IOException, File, PrintWriter}

import com.scalakml.io.{KmzFileReader, KmlFileReader}
import play.api.libs.json.Json
import play.extras.geojson.{GeoJson, LatLng}

/**
  * convert a Kml file into a GeoJSON representation
  *
  * @author R. Wathelet
  *
  *         ref: https://developers.google.com/kml/documentation/kmlreference
  *         ref: http://geojson.org/geojson-spec.html
  */
object Converter {

  def main(args: Array[String]) {
    val usage =
      """Usage: Converter kml_file geojson_file
        |example: Convert Sydney.kml Sydney.geojson""".stripMargin
    if (args.length == 0)
      println(usage)
    else {
      // the output
      val outFile = if (args.length == 2) args(1) else ""
      // the input
      if (args(0).toLowerCase.endsWith("kml")) kmlToGeoJson(args(0), outFile)
      if (args(0).toLowerCase.endsWith("kmz")) kmzToGeoJson(args(0), outFile)
    }
  }

  def kmlToGeoJson(inFile: String, outFile: String) = {
    val kml = new KmlFileReader().getKmlFromFile(inFile)
    val geojson = KmlConverter().toGeoJson(kml)
    if (outFile.isEmpty) {
      geojson.foreach(obj => println(obj))  // testing
      geojson.foreach(obj => println(Json.prettyPrint(Json.toJson(obj))))
    } else {
      writeToFile(outFile, Seq(geojson))
    }
  }

  def kmzToGeoJson(inFile: String, outFile: String) = {
    val kmlSeq = new KmzFileReader().getKmlFromKmzFile(inFile)
    // convert each kml file to GeoJson
    val geojsonSeq = for (kml <- kmlSeq) yield KmlConverter().toGeoJson(kml)
    if (outFile.isEmpty) {
      geojsonSeq.foreach(obj => println(obj)) // testing
      geojsonSeq.foreach(obj => println(Json.prettyPrint(Json.toJson(obj))))
    } else {
      writeToFile(outFile, geojsonSeq)
    }
  }

  private def writeToFile(outFile: String, geojsonSeq: Seq[Option[List[GeoJson[LatLng]]]]) = {
    val writer = new PrintWriter(new File(outFile))
    try {
      geojsonSeq.foreach(obj => writer.write(Json.prettyPrint(Json.toJson(obj))))
    } catch {
      case e: IOException => e.printStackTrace()
    }
    finally {
      writer.close()
    }
  }

}

