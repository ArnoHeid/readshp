import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class App {

    public static void main(String[] args) {
        //ShapeFile
        File file = new File("src/main/resources/railways.shp");
        Map<String, Object> map = new HashMap<>();
        try {
            map.put("url", file.toURI().toURL());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        //PostGre
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        String url = "jdbc:postgresql://localhost/vgisU3";
        Properties props = new Properties();
        props.setProperty("user","postgres");
        props.setProperty("password","Arno1234");

        DataStore dataStore = null;
        try(Connection conn = DriverManager.getConnection(url, props);
            PreparedStatement ps = creatprepStatement(conn)
            ) {
            dataStore = DataStoreFinder.getDataStore(map);
            String typeName = dataStore.getTypeNames()[0];
            FeatureSource<SimpleFeatureType, SimpleFeature> source = dataStore.getFeatureSource(typeName);
            FeatureCollection<SimpleFeatureType, SimpleFeature> collection = source.getFeatures();
            try (FeatureIterator<SimpleFeature> features = collection.features()) {
                while (features.hasNext()) {
                    SimpleFeature feature = features.next();
                    ps.setString(1,feature.getID());
                    ps.setString(2,feature.getDefaultGeometryProperty().getValue().toString());
                    ps.executeUpdate();
                    System.out.print(feature.getID());
                    System.out.print(": ");
                    System.out.println(feature.getDefaultGeometryProperty().getValue());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            dataStore.dispose();
        }
    }


    private static PreparedStatement creatprepStatement(Connection con) throws SQLException {
        String sql = "INSERT INTO rails (geomId , geom) VALUES (?,st_geomfromtext( ?, -1 ))";
        PreparedStatement ps = con.prepareStatement(sql);
        return ps;
    }

}
