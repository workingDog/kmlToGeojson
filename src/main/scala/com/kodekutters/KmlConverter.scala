package com.kodekutters

import com.scalakml.kml._
import com.scalakml.{kml => KML}

import play.extras.{geojson => GEOJS}
import play.api.libs.json._
import play.extras.geojson._

import scala.collection.mutable
import scala.collection.mutable.MutableList
import scala.collection.immutable.Seq

/**
  * converts Kml objects into GeoJson objects
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
  * converts Kml objects into GeoJson objects
  *
  *
  * Kml object -> GeoJson object
  * Kml Folder -> GeoJson FeatureCollection
  * Kml Document -> GeoJson FeatureCollection
  * Kml MultiGeometry -> GeoJson GeometryCollection
  * Kml Placemark -> GeoJson Feature
  * Kml Point -> GeoJson Point
  * Kml LineString -> GeoJson LineString
  * Kml LinearRing -> GeoJson LineString
  * Kml Polygon -> GeoJson Polygon
  * Kml Feature (Placemark, Document, Folder) -> GeoJson object equivalent
  * Kml sequence of Feature -> GeoJson FeatureCollection
  * Kml region latLonAltBox -> GeoJson bbox
  *
  *
  * ref: https://github.com/workingDog/scalakml
  * ref: https://github.com/jroper/play-geojson
  */
class KmlConverter() {

  import scala.language.implicitConversions

  /**
    * convert a KML Coordinate to a LatLngAlt
    * @param coord the input KML coordinate
    * @return a LatLngAlt object
    */
  implicit def coordToLalLngAlt(coord: Coordinate): LatLngAlt = {
      assert(coord.latitude.nonEmpty)
      assert(coord.longitude.nonEmpty)
      LatLngAlt(coord.latitude.get, coord.longitude.get, coord.altitude)
    }

  /**
    * convert a Kml object into a list of GeoJson objects
    * @return a list GeoJson objects
    */
  def toGeoJson(kmlOpt: Option[Kml]): Option[List[GEOJS.GeoJson[LatLngAlt]]] = kmlOpt.map(kml => (for (f <- kml.feature) yield toGeoJson(f)).flatten.toList)

  /**
    * convert a Kml object into a list of GeoJson objects
    * @return a list GeoJson objects
    */
  def toGeoJson(kml: Kml): Option[List[GEOJS.GeoJson[LatLngAlt]]] = toGeoJson(Option(kml))

  /**
    * create a list of GeoJSON properties from a Kml FeaturePart
    * @param fp a Kml FeaturePart object
    * @return a list of (key,value) of properties
    */
  private def properties(fp: KML.FeaturePart): mutable.ListMap[String, JsValue] = {
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

      case None => None
    }
    fp.extendedData.map(_.data.foreach(data => {
        data.displayName.map(d => props += "extended_displayName" -> JsString(d))
        data.name.map(d => props += "extended_name" -> JsString(d))
        data.value.map(d => props += "extended_value" -> JsString(d))
      }))

    // other properties from FeaturePart  todo
    props
  }

  /**
    * convert a Kml Feature into an equivalent GeoJson object
    * @param feature the input Kml feature
    * @return a GeoJson object representation of the Kml Feature
    */
  def toGeoJson(feature: KML.Feature): Option[GeoJson[LatLngAlt]] = {
    feature match {
      case f: Placemark => toGeoJson(f)
      case f: Document => toGeoJson(f)
      case f: Folder => toGeoJson(f)
      case f: PhotoOverlay => None // todo
      case f: ScreenOverlay => None // todo
      case f: GroundOverlay => None // todo
      // case f: GX.Tour => None  //  todo
      case _ => None
    }
  }

  /**
    * convert a sequence of Kml Features into a GeoJson FeatureCollection
    * @param featureSet the set of Kml Features
    * @param bbox the bounding box
    * @return a GeoJson FeatureCollection
    */
  private def toGeoJson(featureSet: Seq[KML.Feature], bbox: Option[(LatLngAlt, LatLngAlt)] ): Option[GEOJS.GeoJson[LatLngAlt]] = {
    // this list may contain GEOJS.FeatureCollection which will need to be expanded into a list of GEOJS.Feature
    val geoList = for (f <- featureSet) yield toGeoJson(f)
    geoList.flatten.toList match {
      // don't process empty list
      case theList if theList.isEmpty => None
      case theList =>
        // to store the individual GEOJS.Feature
        val featureList = new MutableList[GEOJS.Feature[LatLngAlt]]()
        for (geoObj <- theList) {
          geoObj match {
            // expand any FeatureCollection into a list of GEOJS.Feature
            case f: GEOJS.FeatureCollection[LatLngAlt] => for (ft <- f.features) featureList += ft.asInstanceOf[GEOJS.Feature[LatLngAlt]]
            case f => featureList += f.asInstanceOf[GEOJS.Feature[LatLngAlt]]
          }
        }
        Option(GEOJS.FeatureCollection[LatLngAlt](featureList.toList.toSeq, bbox))
    }
  }

  /**
    * convert a Kml Folder to a GeoJson FeatureCollection object
    * @param folder the Kml input Folder object
    * @return a GeoJson FeatureCollection representation of the Kml Folder
    */
  def toGeoJson(folder: KML.Folder): Option[GEOJS.GeoJson[LatLngAlt]] = toGeoJson(folder.features.toList, bbox(folder))

  /**
    * convert a Kml Document into a GeoJson FeatureCollection
    * @param doc the Kml Document object
    * @return a GeoJson FeatureCollection representation of the Kml Document
    */
  def toGeoJson(doc: KML.Document): Option[GEOJS.GeoJson[LatLngAlt]] = toGeoJson(doc.features.toList, bbox(doc))

  /**
    * convert a Kml placemark into a GeoJson Feature
    * @param placemark the Kml placemark object
    * @return a GeoJson Feature representation of the Kml placemark
    */
  def toGeoJson(placemark: Placemark): Option[GEOJS.GeoJson[LatLngAlt]] = {
    val pid = placemark.id.flatMap(x => Option(Json.toJson(x)))
    val props = properties(placemark.featurePart)

    def addToProps(altMode: Option[KML.AltitudeMode], extrude: Option[Boolean]) = {
      altMode.map(x => props += "altitudeMode" -> JsString(x.toString))
      extrude.map(x => props += "extrude" -> JsBoolean(x))
    }

    val geojson: Option[GEOJS.GeoJson[LatLngAlt]] = placemark.geometry.flatMap( {
      case p: KML.Point => addToProps(p.altitudeMode, p.extrude); toGeoJson(p)
      case p: KML.LineString => addToProps(p.altitudeMode, p.extrude); toGeoJson(p)
      case p: KML.LinearRing => addToProps(p.altitudeMode, p.extrude); toGeoJson(p)
      case p: KML.Polygon => addToProps(p.altitudeMode, p.extrude); toGeoJson(p)
      case p: KML.MultiGeometry => toGeoJson(p)
      case p: KML.Model => None //  COLLADA todo
      // case p: GX.Track => Seq(toGeoJson(p))  // todo
      // case p: GX.MultiTrack => Seq(toGeoJson(p))  // todo
    })

    geojson.asInstanceOf[Option[GEOJS.Geometry[LatLngAlt]]].flatMap(p =>
      Option(GEOJS.Feature[LatLngAlt](p, properties = Option(JsObject(props)), id = pid, bbox(placemark))))
  }

  /**
    * create a bbox from the input Kml Feature Region latLonAltBox
    * @param feature the input Kml Feature, e.g. Placemark
    * @return a bounding box (south-west, north-east), i.e. a Tuple of (LatLngAlt,LatLngAlt)
    */
  private def bbox(feature: KML.Feature): Option[(LatLngAlt, LatLngAlt)] = {
    feature.featurePart.region.flatMap(reg => reg.latLonAltBox.map(llb => {
      assert(llb.north.nonEmpty) // north specifies the latitude of the north edge of the bounding box
      assert(llb.east.nonEmpty)  // south specifies the latitude of the south edge of the bounding box
      assert(llb.south.nonEmpty) // east specifies the longitude of the east edge of the bounding box
      assert(llb.west.nonEmpty)  // west specifies the longitude of the west edge of the bounding box
      (new LatLngAlt(llb.south.get, llb.west.get, llb.maxAltitude), new LatLngAlt(llb.north.get, llb.east.get, llb.minAltitude))
    }))
  }

  /**
    * convert a Kml Point into a GeoJson Point object
    * @param p the Kml Point input
    * @return a GeoJson Point object
    */
  def toGeoJson(p: KML.Point): Option[GEOJS.GeoJson[LatLngAlt]] = p.coordinates.flatMap(c => Option(GEOJS.Point(c)))

  /**
    * convert a Kml LineString into a GeoJson LineString object
    * @param ls the Kml LineString input
    * @return a GeoJson LineString object
    */
  def toGeoJson(ls: KML.LineString): Option[GEOJS.GeoJson[LatLngAlt]] = toLineString(ls.coordinates)

  /**
    * convert a Kml LinearRing into a GeoJson LineString object
    * @param lr the Kml LinearRing input
    * @return a GeoJson LineString object
    */
  def toGeoJson(lr: KML.LinearRing): Option[GEOJS.GeoJson[LatLngAlt]] = toLineString(lr.coordinates)

  /**
    * create a GeoJson LineString given the list of Kml Coordinates
    * @param coords the coordinate of the LineString
    * @return a GeoJson LineString
    */
  private def toLineString(coords: Option[scala.Seq[Coordinate]]): Option[GEOJS.GeoJson[LatLngAlt]] = {
    val laloList = for (loc <- coords.getOrElse(List.empty)) yield coordToLalLngAlt(loc)
    Option(GEOJS.LineString(laloList.toList.toSeq))
  }

  /**
    * convert a Kml Polygon into a GeoJson Polygon object
    * @param poly the Kml Polygon input
    * @return a GeoJson Polygon object
    */
  def toGeoJson(poly: KML.Polygon): Option[GEOJS.GeoJson[LatLngAlt]] = {
    val locations = new MutableList[LatLngAlt]()

    // first the outer boundary
    poly.outerBoundaryIs.foreach(
      boundary => boundary.linearRing.foreach(
        ring => ring.coordinates.foreach(seq => seq.foreach(c => locations += c))))

    // then the holes
    poly.innerBoundaryIs.foreach(
      boundary => boundary.linearRing.foreach(
        ring => ring.coordinates.foreach(seq => seq.foreach(c => locations += c))))

    Option(GEOJS.Polygon(Seq(locations.toList)))
  }

  /**
    * convert a Kml MultiGeometry into a GeoJson GeometryCollection object
    * @param multiGeom the Kml MultiGeometry input
    * @return a GeoJson GeometryCollection object
    */
  def toGeoJson(multiGeom: KML.MultiGeometry): Option[GEOJS.GeoJson[LatLngAlt]] = {
    val seqGeom = multiGeom.geometries.flatMap({
      case p: KML.Point => Seq(toGeoJson(p))
      case p: KML.LineString => Seq(toGeoJson(p))
      case p: KML.LinearRing => Seq(toGeoJson(p))
      case p: KML.Polygon => Seq(toGeoJson(p))
      case p: KML.MultiGeometry => Seq(toGeoJson(p))
      // case p: GX.Track => Seq(toGeoJson(p))  // todo
      // case p: GX.MultiTrack => Seq(toGeoJson(p))  // todo
      case _ => None
    }).flatten.toList.asInstanceOf[Seq[GeometryCollection[LatLngAlt]]]
    Option(GEOJS.GeometryCollection(seqGeom))
  }

}