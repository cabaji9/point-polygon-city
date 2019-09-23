import de.micromata.opengis.kml.v_2_2_0.*;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Hyun Woo Son on 9/20/19
 **/
public class PointInPolygon {


    public static void main(String args[]) {

        Map<String, List<Polygon>> cities = new HashMap<>();
        cities.put("Quito",null);
        cities.put("Guayaquil",null);

        Kml ecuadorCities = Kml.unmarshal(new File("src/main/resources/gadm36_ECU_2.kml"));
        Document document = (Document) ecuadorCities.getFeature();
        Folder folder = (Folder) document.getFeature().get(0);
        int ciudades = folder.getFeature().size();
        System.out.println("ciudades tamaño: " + ciudades);
        folder.getFeature().forEach(feature -> {
            Placemark placemark = (Placemark) feature.clone();
            placemark.getExtendedData().getSchemaData().get(0).getSimpleData().stream().
                    forEach(simpleData -> {
                        if (simpleData.getName().equals("NAME_2") &&
                                cities.containsKey(simpleData.getValue())) {
                            cities.put(simpleData.getValue(),obtainListPolygons((MultiGeometry) placemark.getGeometry()));
                        }
                    });
        });
        List<org.locationtech.jts.geom.Coordinate> coordinatesSearchList =
                Arrays.asList(new org.locationtech.jts.geom.Coordinate(-0.2428562, -78.4932438),
                        new org.locationtech.jts.geom.Coordinate(-0.2866354,-78.5050687),
                        new org.locationtech.jts.geom.Coordinate(-0.3211701, -77.7886308),
                        new org.locationtech.jts.geom.Coordinate(-2.1253712,-79.9543459),
                        new org.locationtech.jts.geom.Coordinate( -2.0705418,-79.9377251),
                        new org.locationtech.jts.geom.Coordinate( -2.1340361,-79.8663126),
                        new org.locationtech.jts.geom.Coordinate(  -2.2288411,-79.9027629)
                        );

        coordinatesSearchList.forEach(coordinate -> {
             PointInPolygon.obtainPointCity(cities,coordinate);
        });
    }


    public static List<Polygon> obtainListPolygons(MultiGeometry multiGeometry){
        GeometryFactory gf = new GeometryFactory();
       return  multiGeometry.getGeometry().stream().map(multiGeo ->{
            de.micromata.opengis.kml.v_2_2_0.Polygon polygon =(de.micromata.opengis.kml.v_2_2_0.Polygon) multiGeo;
            List<Coordinate> coordinateList = polygon.getOuterBoundaryIs().getLinearRing().getCoordinates();
            List<org.locationtech.jts.geom.Coordinate> coordinatesMapped = coordinateList.stream().map(coordinate ->
                    new org.locationtech.jts.geom.Coordinate(coordinate.getLatitude(),coordinate.getLongitude())
            ).collect(Collectors.toList());

            int numPoints = coordinateList.size();
            LinearRing jtsRing = gf.createLinearRing(coordinatesMapped.toArray(new org.locationtech.jts.geom.Coordinate[numPoints]));
            return gf.createPolygon(jtsRing, null);
        }).collect(Collectors.toList());
    }

    public static String obtainPointCity( Map<String, List<Polygon>> cities,org.locationtech.jts.geom.Coordinate searchCoordinate){
        String cityFound = "";
        for(String city:cities.keySet()){
            List<Polygon> polygonList =    cities.get(city);
            boolean found = polygonList.stream().anyMatch(polygon -> {
                GeometryFactory gf = new GeometryFactory();
                org.locationtech.jts.geom.Point pt = gf.createPoint(searchCoordinate);
                return polygon.contains(pt);
            });
            if (found) {
                cityFound = city;
                System.out.println("ciudad encontrada: "+city + ", coordenadas "+ searchCoordinate.toString());
            }
        }
        return cityFound;
    }


}
