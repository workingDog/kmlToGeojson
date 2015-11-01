# scalakml is a KML Version 2.2 library written in scala.


## KML an Open Geospatial Consortium (OGC) standard

From reference 1, "KML is an XML language focused on geographic visualization, including annotation of maps and images. Geographic visualization includes not only the presentation of graphical data on the globe, but also the control of the user's navigation in the sense of where to go and where to look."

Specifically KML is used in Google Earth to display various geographic elements, such as; images, 
place marks, polygon shapes, 3D models, etc...

The scalakml library provides the KML elements as scala classes. 

## References
 
1) OGC 07-147r2 Version: 2.2.0, Category: OGC Standard, Editor: Tim Wilson, at http://www.opengeospatial.org/standards/kml

2) Google developers KML Reference, at https://developers.google.com/kml/documentation/kmlreference

## Packages

The scalakml library consists of 4 major packages:
- 1) com.scalakml.kml, the set of KML classes
- 2) com.scalakml.gx, the Google's GX extensions classes derived from the official kml22gx.xsd
- 3) com.scalakml.io, the reading and writing of kml elements
- 4) com.scalakml.atom, the Atom classes

## Dependencies

scalakml depends on a companion library called scalaxal. This library has the xAL classes 
needed for the AddressDetails. scalaxal is developed in 
another repository, see https://github.com/workingDog/scalaxal. 
The scalaxal.jar is included here in the lib directory.

## Documentation

See reference 1 and 2 for the full documentation of kml.
I've copied and pasted most of the documentations from the references into the classes,
but there is still a bit more to be done.

Most kml elements described in the references have been implemented with the same or similar class name.
There is one exception, and that is the FeaturePart.
This class represents the abstract feature part described in the references.
Here it is a concrete class called FeaturePart and holds the elements of the abstract feature.
Such that, for example, Placemark has a featurePart element, so to get the name of the placemark
you could write:
- placemark.featurePart.name

The following (Feature) classes use FeaturePart:
Document, Folder, Placemark, NetworkLink, PhotoOverlay, ScreenOverlay, GroundOverlay and Tour.
A FeaturePart can be created and is typically added as an element to: Document, Folder, Placemark, etc...

Example kml files from http://code.google.com/p/kml-samples/ are included in the kml-files directory.
The file KML_Samples.kml contains CDATA elements. The same file without
the CDATA elements (example-no-cdata.kml) is also included.

## Usage

To use the library in your project simply include the scalakml.jar file provided in the bin directory, and 
the scalaxal.jar from the lib directory.

    object WriteExample4 {
      def main(args: Array[String])  {
        // create a scala Kml object with a Placemark that contains a Point
        val kml = new Kml(new Placemark("Sydney", new Point(RelativeToGround, 151.21037, -33.8526, 12345.0)))
        // write the kml object to System.out 
        new KmlPrintWriter().write(kml, new PrettyPrinter(80, 3))
      }
    }

## Status

Stable.

The gx: extension has what is in the official kml22gx.xsd schema,
it does not include some of the gx: elements from Google listed in reference 2.

Using scala 2.11.6 and java 8 SDK, with IntelliJ IDEA 14.

To generate a new jar file from the source using sbt, type: sbt package

To generate the scaladoc, type: sbt doc


Ringo Wathelet
