# Convert Kml to GeoJSON format 

This application "Converter" converts Kml or Kmz files into a GeoJSON representation. 

[Kml](https://developers.google.com/kml/documentation/kmlreference) is used in Google Earth to display 
various geographic elements, such as; images, place marks, polygon shapes, 3D models, etc...

Similarly [GeoJSON](http://geojson.org/) is a format for encoding a variety of geographic data structures.

This scala application "Converter" uses the [scalakml library](https://github.com/workingDog/scalakml) and 
the [GeoJSON library](https://github.com/jroper/play-geojson) to do the conversion.
 
## Current objects mapping

The following Kml mapping is implemented.

    Kml object -> list of GeoJson object
    Kml Folder -> GeoJson FeatureCollection
    Kml Document -> GeoJson FeatureCollection
    Kml MultiGeometry -> GeoJson GeometryCollection
    Kml Placemark -> GeoJson Feature
    Kml Point -> GeoJson Point
    Kml LineString -> GeoJson LineString
    Kml LinearRing -> GeoJson LinearRing
    Kml Polygon -> GeoJson Polygon

The GeoJSON Feature "properties" are generated from the following Kml elements:

    name, description, address, phoneNumber, styleUrl, visibility, open
 
The Kml "id" attribute is converted to the GeoJSON Feature "id".

Everything else is ignored.
 
Only [WGS84](https://en.wikipedia.org/wiki/World_Geodetic_System) coordinate reference system 
is supported and all longitudes and latitudes are in decimal degrees.
 
## References
 
1) OGC 07-147r2 Version: 2.2.0, Category: OGC Standard, Editor: Tim Wilson, at http://www.opengeospatial.org/standards/kml

2) Google developers KML Reference, at https://developers.google.com/kml/documentation/kmlreference

3) GeoJSON reference document, at http://geojson.org/geojson-spec.html

## Dependencies

Depends on the scala [scalakml library](https://github.com/workingDog/scalakml)
and its companion library [scalaxal](https://github.com/workingDog/scalaxal), 
and on the scala [GeoJSON library](https://github.com/jroper/play-geojson).

For convenience, these libraries are included here in the lib directory.

## Usage

To use, compile and package the code, then simply type at the prompt:
 
    Converter kml_file.kml geojson_file.geojson
 
where "kml_file.kml" is the Kml file you want to convert, and "geojson_file.geojson" is the destination file 
with the GeoJSON results.
 
You can also use the "KmlConverter" class in your code, such as: 

    val geojson = KmlConverter().toGeoJson(kml)
    
This gives you a GeoJson object, that can easily be converted to JSON, such as:
  
    geojson.foreach(obj => println(Json.prettyPrint(Json.toJson(obj))))
  
"KmlConverter" has one generic method "toGeoJson()" that takes any of the implemented Kml objects. 
See also "TestGeoJson".
 
## Compile and package

The easiest way to compile and package the application is to use [SBT](http://www.scala-sbt.org/).
 
## Status

work in progress, not tested 

Using scala 2.11.7, java 8 and SBT.


Ringo Wathelet
