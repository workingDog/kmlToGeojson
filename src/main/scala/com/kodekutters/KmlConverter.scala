package com.kodekutters

import com.scalakml.kml._
import com.scalakml.{kml => KML}

import play.extras.{geojson => GEOJS}
import play.api.libs.json._
import play.extras.geojson._

import scala.collection.mutable
import scala.collection.mutable.MutableList

// is this correct? why not scala.Seq
import scala.collection.immutable.Seq

/**
  * convert Kml objects into GeoJson objects
  *
  * @author R. Wathelet
  *
  *         ref: https://github.com/workingDog/scalakml
  *         ref: https://github.com/jroper/play-geojson
  */
object KmlConverter {
  def apply() = new KmlConverter()
}

/**
  * convert Kml objects into GeoJson objects
  *
  *
  * Kml object -> GeoJson object
  * Kml Folder -> GeoJson FeatureCollection
  * Kml Document -> GeoJson FeatureCollection
  * Kml MultiGeometry -> GeoJson GeometryCollection
  * Kml Placemark -> GeoJson Feature
  * Kml Point -> GeoJson Point
  * Kml LineString -> GeoJson LineString
  * Kml LinearRing -> GeoJson LinearRing
  * Kml Polygon -> GeoJson Polygon
  * Kml Feature (Placemark, Document, Folder) -> GeoJson object equivalent
  * Kml sequence of Feature -> GeoJson FeatureCollection
  *
  *
  * ref: https://github.com/workingDog/scalakml
  * ref: https://github.com/jroper/play-geojson
  */
class KmlConverter() {

  // implicit to change a KML Coordinate to a GeoJson LatLng
  implicit class CoordinateToLalLng(coord: Coordinate) {
    def toLatLng(): LatLng = {
      assert(coord.latitude.nonEmpty)
      assert(coord.longitude.nonEmpty)
      LatLng(coord.latitude.get, coord.longitude.get)
    }
  }

  /**
    * convert a Kml object into a list of GeoJson objects
    * @return a list GeoJson objects
    */
  def toGeoJson(kmlOpt: Option[Kml]): Option[List[GEOJS.GeoJson[LatLng]]] = kmlOpt.map(kml => (for (f <- kml.feature) yield toGeoJson(f)).flatten.toList)

  /**
    * convert a Kml object into a list of GeoJson objects
    * @return a list GeoJson objects
    */
  def toGeoJson(kml: Kml): Option[List[GEOJS.GeoJson[LatLng]]] = toGeoJson(Option(kml))

  /**
    * create a GeoJSON properties from a Kml FeaturePart
    * @param fp a Kml FeaturePart object
    * @return GeoJSON properties in JSON format
    */
  private def properties(fp: KML.FeaturePart): Option[JsObject] = {
    // the list of properties (key,value)
    val props = new mutable.ListMap[String, JsValue]()
    // the properties
    fp.name.map(x => props += "name" -> JsString(x))
    fp.description.map(x => props += "description" -> JsString(x))
    fp.address.map(x => props += "address" -> JsString(x))
    fp.phoneNumber.map(x => props += "phoneNumber" -> JsString(x))
    fp.styleUrl.map(x => props += "styleUrl" -> JsString(x))
    fp.visibility.map(x => props += "visibility" -> JsBoolean(x))
    fp.open.map(x => props += "open" -> JsBoolean(x))
    fp.timePrimitive match {
      case Some(timex) if timex.isInstanceOf[TimeStamp] =>
        props += "timeStamp" -> JsString(timex.asInstanceOf[TimeStamp].when.getOrElse(""))

      case Some(timex) if timex.isInstanceOf[TimeSpan] =>
        props += "timeBegin" -> JsString(timex.asInstanceOf[TimeSpan].begin.getOrElse(""))
        props += "timeEnd" -> JsString(timex.asInstanceOf[TimeSpan].end.getOrElse(""))
    }
    // other properties from FeaturePart  todo
    Option(JsObject(props))
  }

  /**
    * convert a Kml Feature into an equivalent GeoJson object
    * @param feature the input Kml feature
    * @return a GeoJson object representation of the Kml Feature
    */
  def toGeoJson(feature: KML.Feature): Option[GeoJson[LatLng]] = {
    feature match {
      case f: Placemark => toGeoJson(f)
      case f: Document => toGeoJson(f)
      case f: Folder => toGeoJson(f)
      case f: PhotoOverlay => None
      case f: ScreenOverlay => None
      case f: GroundOverlay => None
      // case f: Tour => None  // gx
      case _ => None
    }
  }

  /**
    * convert a sequence of Kml Features into a GeoJson FeatureCollection
    * @param featureSet the set of Kml Features
    * @return a GeoJson FeatureCollection
    */
  def toGeoJson(featureSet: Seq[KML.Feature]): Option[GEOJS.GeoJson[LatLng]] = {
    // this list may contain GEOJS.FeatureCollection which will need to be expanded into a list of GEOJS.Feature
    val geoList = for (f <- featureSet) yield toGeoJson(f)
    geoList.flatten.toList match {
      // don't process empty list
      case theList if theList.isEmpty => None
      case theList =>
        // the list of individual GEOJS.Feature
        val featureList = new MutableList[GEOJS.Feature[LatLng]]()
        for (geoObj <- theList) {
          geoObj match {
            // expand any FeatureCollection into a list of GEOJS.Feature
            case f: GEOJS.FeatureCollection[LatLng] => for (ft <- f.features) featureList += ft.asInstanceOf[GEOJS.Feature[LatLng]]
            case f => featureList += f.asInstanceOf[GEOJS.Feature[LatLng]]
          }
        }
        Option(GEOJS.FeatureCollection[LatLng](featureList.toList.toSeq))
    }
  }

  /**
    * convert a Kml Folder to a GeoJson FeatureCollection object
    * @param folder the Kml input Folder object
    * @return a GeoJson FeatureCollection representation of the Kml Folder
    */
  def toGeoJson(folder: KML.Folder): Option[GEOJS.GeoJson[LatLng]] = toGeoJson(folder.features.toList)

  /**
    * convert a Kml Document into a GeoJson FeatureCollection
    * @param doc the Kml Document object
    * @return a GeoJson FeatureCollection representation of the Kml Document
    */
  def toGeoJson(doc: KML.Document): Option[GEOJS.GeoJson[LatLng]] = toGeoJson(doc.features.toList)

  /**
    * create a GeoJson Feature ... not used
    * @param geom the GeoJson Geometry
    * @param featurePart the Kml FeaturePart
    * @param pid the possible id
    * @return a GeoJson Feature
    */
  def toGeoFeature(geom: Option[GEOJS.Geometry[LatLng]], featurePart: KML.FeaturePart, pid: Option[JsValue]) = {
    geom.asInstanceOf[Option[GEOJS.Geometry[LatLng]]].map(p =>
      Option(GEOJS.Feature[LatLng](p, properties = properties(featurePart), id = pid)))
  }

  /**
    * convert a Kml Placemark into a GeoJson Feature
    * @param placemark the Kml placemark object
    * @return a GeoJson Feature representation of the Kml Placemark
    */
  def toGeoJson(placemark: Placemark): Option[GEOJS.GeoJson[LatLng]] = {
    // the possible id
    val pid = placemark.id.flatMap(x => Option(Json.toJson(x)))

    // make a GeoJson Feature with the input GeoJson Geometry
    def toGeoFeature(geom: Option[GEOJS.GeoJson[LatLng]]) =
      geom.asInstanceOf[Option[GEOJS.Geometry[LatLng]]].map(p => Option(GEOJS.Feature[LatLng](p, properties = properties(placemark.featurePart), id = pid)))

    placemark.geometry.flatMap({
      case p: KML.Point => toGeoFeature(toGeoJson(p))
      case p: KML.LineString => toGeoFeature(toGeoJson(p))
      case p: KML.LinearRing => toGeoFeature(toGeoJson(p))
      case p: KML.Polygon => toGeoFeature(toGeoJson(p))
      case p: KML.MultiGeometry => toGeoFeature(toGeoJson(p))
      case p: KML.Model => None //  COLLADA
      //  case p: KML.Track =>  // gx
      //  case p: KML.MultiTrack => // gx
      case _ => None
    }).flatten
  }

  /**
    * convert a Kml Point into a GeoJson Point object
    * @param p the Kml Point input
    * @return a GeoJson Point object
    */
  def toGeoJson(p: KML.Point): Option[GEOJS.GeoJson[LatLng]] = if (p.coordinates.nonEmpty) Option(GEOJS.Point(p.coordinates.get.toLatLng())) else None

  /**
    * convert a Kml LineString into a GeoJson LineString object
    * @param ls the Kml LineString input
    * @return a GeoJson LineString object
    */
  def toGeoJson(ls: KML.LineString): Option[GEOJS.GeoJson[LatLng]] = toLineString(ls.coordinates)

  /**
    * convert a Kml LinearRing into a GeoJson LineString object
    * @param lr the Kml LinearRing input
    * @return a GeoJson LineString object
    */
  def toGeoJson(lr: KML.LinearRing): Option[GEOJS.GeoJson[LatLng]] = toLineString(lr.coordinates)

  /**
    * create a GeoJson LineString given the list of Kml Coordinates
    * @param coords the coordinate of the LineString
    * @return a GeoJson LineString
    */
  private def toLineString(coords: Option[scala.Seq[Coordinate]]): Option[GEOJS.GeoJson[LatLng]] = {
    val laloList = for (loc <- coords.getOrElse(List.empty)) yield loc.toLatLng()
    Option(GEOJS.LineString(laloList.toList.toSeq))
  }

  /**
    * convert a Kml Polygon into a GeoJson Polygon object
    * @param poly the Kml Polygon input
    * @return a GeoJson Polygon object
    */
  def toGeoJson(poly: KML.Polygon): Option[GEOJS.GeoJson[LatLng]] = {
    val locationList = new MutableList[scala.Seq[Coordinate]]()

    // first the outer boundary
    poly.outerBoundaryIs.foreach(
      boundary => boundary.linearRing.foreach(
        ring => locationList += ring.coordinates.getOrElse(List.empty)))

    // then the holes
    poly.innerBoundaryIs.foreach(
      boundary => boundary.linearRing.foreach(
        ring => locationList ++ ring.coordinates.getOrElse(List.empty)))

    val laloList = for (loc <- locationList.flatten.toList) yield loc.toLatLng()
    Option(GEOJS.Polygon(Seq(laloList)))
  }

  /**
    * convert a Kml MultiGeometry into a GeoJson GeometryCollection object
    * @param multiGeom the Kml MultiGeometry input
    * @return a GeoJson GeometryCollection object
    */
  def toGeoJson(multiGeom: KML.MultiGeometry): Option[GEOJS.GeoJson[LatLng]] = {
    val seqGeom = multiGeom.geometries.flatMap({
      case p: KML.Point => Seq(toGeoJson(p))
      case p: KML.LineString => Seq(toGeoJson(p))
      case p: KML.LinearRing => Seq(toGeoJson(p))
      case p: KML.Polygon => Seq(toGeoJson(p))
      case p: KML.MultiGeometry => Seq(toGeoJson(p))
      case _ => None
    }).flatten.asInstanceOf[Seq[GeometryCollection[LatLng]]]
    Option(GEOJS.GeometryCollection(seqGeom))
  }

}