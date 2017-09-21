/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ort.geotools.projetotcc;


import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.DefaultQuery;
import org.geotools.data.Query;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.swing.data.JFileDataStoreChooser;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.geotools.filter.text.cql2.CQL;

import com.vividsolutions.jts.geom.Geometry;

/**
 * This class shows how to "join" two feature sources.
 * 
 * @author Jody
 * 
 * 
 * @source $URL$
 */
public class ExemploJoin {

/**
 * 
 * @param args
 *            shapefile to use, if not provided the user will be prompted
 */
public static void main(String[] args) throws Exception {
    System.out.println("Welcome to GeoTools:" + GeoTools.getVersion());
    
//    File file, file2;
//    if (args.length == 0) {
//        file = JFileDataStoreChooser.showOpenFile("shp", null);
//    } else {
//        file = new File(args[0]);
//    }
//    if (args.length <= 1) {
//        file2 = JFileDataStoreChooser.showOpenFile("shp", null);
//    } else {
//        file2 = new File(args[1]);
//    }
//    if (file == null || !file.exists() || file2 == null || !file2.exists()) {
//        System.exit(1);
//    }

    DataStore dataStore;
    Map<String,Object> params = new HashMap<>();
    params.put( "dbtype", "postgis");
    params.put( "host", "localhost");
    params.put( "port", 5432);
    params.put( "schema", "public");
    params.put( "database", "Shapefiles");
    params.put( "user", "postgres");
    params.put( "passwd", "123");
    dataStore = DataStoreFinder.getDataStore(params);
    
    SimpleFeatureSource shapes = dataStore.getFeatureSource("bairros");

    SimpleFeatureSource shapes2 = dataStore.getFeatureSource("educacao");
    
    joinExample(shapes, shapes2);
    System.exit(0);
}

private static void joinExample(SimpleFeatureSource shapes, SimpleFeatureSource shapes2)
        throws Exception {
    SimpleFeatureType schema = shapes.getSchema();
    String typeName = schema.getTypeName();
    String geomName = schema.getGeometryDescriptor().getLocalName();
    
    SimpleFeatureType schema2 = shapes2.getSchema();
    String typeName2 = schema2.getTypeName();
    String geomName2 = schema2.getGeometryDescriptor().getLocalName();
    FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2();
    
    Query outerGeometry = new Query(typeName, Filter.INCLUDE, new String[] { geomName });
    
    ////
    Filter filter = CQL.toFilter("nome like 'CARÁ-CARÁ'");
    ////
    
    SimpleFeatureCollection outerFeatures = shapes.getFeatures(filter);
    SimpleFeatureIterator iterator = outerFeatures.features();
    int max = 0;
    try {
        while (iterator.hasNext()) {
            SimpleFeature feature = iterator.next();
            try {
                Geometry geometry = (Geometry) feature.getDefaultGeometry();
                if (!geometry.isValid()) {
                    // skip bad data
                    continue;
                }
                Filter innerFilter = ff.intersects(ff.property(geomName2), ff.literal(geometry));
                Query innerQuery = new Query(typeName2, innerFilter, Query.NO_NAMES);
                SimpleFeatureCollection join = shapes2.getFeatures(innerQuery);
                int size = join.size();
                max = Math.max(max, size);
            } catch (Exception skipBadData) {
            }
        }
    } finally {
        iterator.close();
    }
    System.out.println(max + " " + typeName2 + " features encontrados em " + typeName);
}

}
