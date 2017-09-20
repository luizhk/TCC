/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ort.geotools.projetotcc;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JOptionPane;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;

/**
 *
 * @author Luiz
 */
public class ConexaoBanco {
    
    public ConexaoBanco() throws IOException {
    }

    public static DataStore ConectarBase() throws IOException{
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
        if (dataStore == null) {
            JOptionPane.showMessageDialog(null, "Could not connect - check parameters");
        }
        return dataStore;
    }
        
}
    

