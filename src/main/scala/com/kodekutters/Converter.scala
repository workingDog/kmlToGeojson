package com.kodekutters

import java.io.{IOException, File, PrintWriter}

import com.scalakml.io.{KmzFileReader, KmlFileReader}
import play.api.libs.json.Json
import play.extras.geojson.{GeoJson, LatLng}

/**
  * convert a Kml or Kmz file into a GeoJSON representation
  *
  * @author R. Wathelet
  *
  *         ref: https://developers.google.com/kml/documentation/kmlreference
  *         ref: http://geojson.org/geojson-spec.html
  */
object Converter {

/**
  * convert a Kml or Kmz file into a GeoJSON representation
  */
  def main(args: Array[String]) {
    val usage = """Usage: java -jar converter-0.1.jar kml_file geojson_file
        |example: Convert Sydney.kml Sydney.geojson""".stripMargin
    if (args.isEmpty)
      println(usage)
    else {
      val outFile = if (args.length == 2) args(1) else ""
      // the input file
      args(0).toLowerCase match {
        case inFile if inFile.endsWith("kml") => kmlToGeoJson(inFile, outFile)
        case inFile if inFile.endsWith("kmz") => kmzToGeoJson(inFile, outFile)
        case inFile => println("Error --> input file \"" + inFile + "\" must have extension .kml or .kmz")
      }
    }
  }

  /**
    * convert the input kml file and write the GeoJSON results to the output file
    * @param inFile the input kml file name must have extension .kml
    * @param outFile the GeoJSON output file
    */
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

  /**
    * convert the input kmz file and write the GeoJSON results to the output file
    * @param inFile the input kmz file name must have extension .kmz
    * @param outFile the GeoJSON output file
    */
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

  /**
    * write the list of GeoJSON objects to the output file
    * @param outFile the output file to write the GeoJSON to
    * @param geojsonList the list of GeoJSON objects to write
    */
  private def writeToFile(outFile: String, geojsonList: Seq[Option[List[GeoJson[LatLng]]]]) = {
    val writer = new PrintWriter(new File(outFile))
    try {
      geojsonList.foreach(obj => writer.write(Json.prettyPrint(Json.toJson(obj))))
    } catch {
      case e: IOException => e.printStackTrace()
    }
    finally {
      writer.close()
    }
  }

}

