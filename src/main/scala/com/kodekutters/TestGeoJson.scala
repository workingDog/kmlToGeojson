package com.kodekutters

import com.scalakml.{kml => KML}
import play.extras.{geojson => GEOJS}
import com.scalakml.io.KmlFileReader
import com.scalakml.kml._
import scala.collection.immutable.Seq
import play.api.libs.json._

// just testing things

object TestGeoJson {

  // example-no-cdata.kml  Sydney.kml  KML_Samples.kml  Extended

  def main(args: Array[String]) {
    test1()
    kmlToGeoJson("./kml-files/Sydney.kml")
  }

  def kmlToGeoJson(fileName: String) = {
    val kml = new KmlFileReader().getKmlFromFile(fileName)
    val geojson = KmlConverter().toGeoJson(kml)
  //  geojson.foreach(obj => println("geojson obj: \n" + obj))
  //  println("\n")
    geojson.foreach(obj => println(Json.prettyPrint(Json.toJson(obj))))
  }

  def test1() = {
    println("--------- start of test1 ----------")
    val coord = new Coordinate(151.21037, -33.8526)
    val coords = Seq.empty :+ new Coordinate(152.21037, -32.8526) :+ new Coordinate(123.456, -37.890) :+ coord
    val inCoords = Seq.empty :+ new Coordinate(111.111, 22.222) :+ new Coordinate(133.333, 44.444)
    val point = new KML.Point(coord)
    val lineString = new KML.LineString(coords)
    val linearRing = new KML.LinearRing(coords)
    val innerLinearRing = new KML.LinearRing(inCoords)
    val outBoundary = new KML.Boundary(linearRing)
    val inBoundary = new KML.Boundary(innerLinearRing)
    val poly = new KML.Polygon(outBoundary, inBoundary)
    val placemark = Placemark(Option(poly), FeaturePart(name = Option("Sydney"), description = Option("someprop"), visibility = Option(true)), Option("123"))
    val doc = Document(features = (Seq.empty :+ placemark), featurePart = new FeaturePart(name = Option("test_document")))
    val folder = Folder(features = (Seq.empty :+ placemark), featurePart = new FeaturePart(name = Option("test_folder")))
    val seqOfFeatures = Seq(placemark, doc)
    val kml = new Kml(folder)

    val cnv = KmlConverter()

    // Features
    cnv.toGeoJson(doc).foreach(obj => println("doc: " + obj + " \ndoc: " + Json.toJson(obj)+ "\n"))
    cnv.toGeoJson(folder).foreach(obj => println("folder: " + obj + " \nfolder: " + Json.toJson(obj)+ "\n"))
    cnv.toGeoJson(placemark).foreach(obj => println("placemark: " + obj + " \nplacemark: " + Json.toJson(obj) + "\n"))
    cnv.toGeoJson(seqOfFeatures).foreach(obj => println("seqOfFeatures: " + obj + " \nseqOfFeatures: " + Json.toJson(obj)+ "\n"))
    cnv.toGeoJson(kml).foreach(obj => println("kml: " + obj + " \nkml: " + Json.toJson(obj)+ "\n"))

    // individual geometries with the bbox = None
    cnv.toGeoJson(point, None).foreach(obj => println("point: " + obj + " \npoint: " + Json.toJson(obj)+ "\n"))
    cnv.toGeoJson(lineString, None).foreach(obj => println("lineString: " + obj + " \nlineString: " + Json.toJson(obj)+ "\n"))
    cnv.toGeoJson(linearRing, None).foreach(obj => println("linearRing: " + obj + " \nlinearRing: " + Json.toJson(obj)+ "\n"))
    cnv.toGeoJson(poly, None).foreach(obj => println("poly: " + obj + " \npoly: " + Json.toJson(obj)+ "\n"))

    println("--------- end of test1 ----------\n")
  }


}
