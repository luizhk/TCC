
package ort.geotools.projetotcc;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import org.geotools.data.DataStore;
import org.geotools.data.Query;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.swing.action.SafeAction;
import org.geotools.swing.table.FeatureCollectionTableModel;
import org.opengis.feature.type.FeatureType;
import org.opengis.filter.Filter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JToolBar;
import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.Transaction;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.Hints;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.filter.text.cql2.CQLException;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Fill;
import org.geotools.styling.Font;
import org.geotools.styling.Graphic;
import org.geotools.styling.Mark;
import org.geotools.styling.Rule;
import org.geotools.styling.SLD;
import org.geotools.styling.Stroke;
import org.geotools.styling.Style;
import org.geotools.styling.StyleBuilder;
import org.geotools.styling.StyleFactory;
import org.geotools.styling.Symbolizer;
import org.geotools.swing.JMapFrame;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.geotools.swing.event.MapMouseEvent;
import org.geotools.swing.tool.CursorTool;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.identity.FeatureId;


public class Main extends JFrame {
    private static final Color LINE_COLOUR = Color.BLUE;
    private static final Color FILL_COLOUR = Color.CYAN;
    private static final Color SELECTED_COLOUR = Color.YELLOW;
    private static final float OPACITY = 1.0f;
    private static final float LINE_WIDTH = 1.0f;
    private static final float POINT_SIZE = 7.0f;
    private static Font font;
    private final JComboBox featureTypeCBox;
    private final JComboBox featureTypeCBox2;
    private String geometryAttributeName;
//    private final JTable table;
//    private final JTextField text;    
    private final StyleFactory sf = CommonFactoryFinder.getStyleFactory();
    private final FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2();
    private enum GeomType { POINT, LINE, POLYGON };
    private GeomType geometryType;
    private final DataStore dataStore;
    private static SimpleFeatureSource featureSource;
    private MapContent mapContent;
    private JMapFrame jMapFrame;
    private DefaultFeatureCollection featureCollection;
    final SimpleFeatureType TYPEEducacao = DataUtilities.createType("educacao",
        "geom:Point:srid=4326," +
        "nomesc:String," +
        "rua:String," +
        "numero:double," +
        "vila:String," +
        "cep:double"
    );
    final SimpleFeatureType TYPEEducacao_inf = DataUtilities.createType("educacao_inf",
        "geom:Point:srid=4326," +
        "nome:String," +
        "rua:String," +
        "numero:double"
    );
    
    public Main() throws IOException, Exception {
        featureSource = null;
        mapContent = new MapContent();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        getContentPane().setLayout(new BorderLayout());
        JLabel label1 = new JLabel();
        label1.setText("Layer 1");
        JLabel label2 = new JLabel();
        label2.setText("Layer 2");

        JMenuBar menubar = new JMenuBar();
        setJMenuBar(menubar);
        
        featureTypeCBox = new JComboBox();
        featureTypeCBox2 = new JComboBox();
        menubar.add(label1);
        menubar.add(featureTypeCBox);
        menubar.add(label2);
        menubar.add(featureTypeCBox2);

        JMenu dataMenu = new JMenu("Dados");
        menubar.add(dataMenu);
        
        dataStore = ConexaoBanco.ConectarBase();
        if (dataStore == null) {
            JOptionPane.showMessageDialog(null, "Não foi possível conectar - Confirmar Parâmetros");
        }
     
        pack();    
            
//        fileMenu.addSeparator();
//        fileMenu.add(new SafeAction("Sair") {
//            @Override
//            public void action(ActionEvent e) throws Throwable {
//                System.exit(0);
//            }
//        });
//        
//        dataMenu.add(new SafeAction("Características") {
//            @Override
//            public void action(ActionEvent e) throws Throwable {
//                filterFeatures();
//            }
//        });
//        dataMenu.add(new SafeAction("Somatório") {
//            @Override
//            public void action(ActionEvent e) throws Throwable {
//                countFeatures();
//            }
//        });
//        dataMenu.add(new SafeAction("Geometria") {
//            @Override
//            public void action(ActionEvent e) throws Throwable {
//                queryFeatures();
//            }
//        });

        dataMenu.add(new SafeAction("Exibir Grafico") {
            @Override
            public void action(ActionEvent e) throws Throwable {
                exibirGrafico();
            }
        });
        
        updateUI();
    }
    
    private void updateUI() throws Exception {
        ComboBoxModel cbm = new DefaultComboBoxModel(dataStore.getTypeNames());
        ComboBoxModel cbm2 = new DefaultComboBoxModel(dataStore.getTypeNames());
        
        featureTypeCBox.setModel(cbm);
        featureTypeCBox2.setModel(cbm2);
    }
    
//    private void filterFeatures() throws Exception {
//        String typeName = (String) featureTypeCBox.getSelectedItem();
//        featureSource = dataStore.getFeatureSource(typeName);
//        
//        Filter filter = CQL.toFilter(text.getText());
//        SimpleFeatureCollection features = featureSource.getFeatures(filter);
//        FeatureCollectionTableModel model = new FeatureCollectionTableModel(features);
//        table.setModel(model);
//    }
    
//    private void countFeatures() throws Exception {
//        String typeName = (String) featureTypeCBox.getSelectedItem();
//        featureSource = dataStore.getFeatureSource(typeName);
//
//        Filter filter = CQL.toFilter(text.getText());
//        SimpleFeatureCollection features = featureSource.getFeatures(filter);
//
//        int count = features.size();
//        JOptionPane.showMessageDialog(text, "Número de caracteristícas:" + count);
//    }
    
//    private void queryFeatures() throws Exception {
//        String typeName = (String) featureTypeCBox.getSelectedItem();
//        featureSource = dataStore.getFeatureSource(typeName);
//
//        FeatureType schema = featureSource.getSchema();
//        String name = schema.getGeometryDescriptor().getLocalName();
//
//        Filter filter = CQL.toFilter(text.getText());
//
//        Query query = new Query(typeName, filter, new String[] { name });
//
//        SimpleFeatureCollection features = featureSource.getFeatures(query);
//
//        FeatureCollectionTableModel model = new FeatureCollectionTableModel(features);
//        table.setModel(model);
//    }
    
    private void exibirGrafico() throws Exception {
        String typeName = (String) featureTypeCBox.getSelectedItem();
        featureSource = dataStore.getFeatureSource(typeName);
        setGeometry(featureSource);
        jMapFrame = new JMapFrame();
        jMapFrame.enableToolBar( true );
        jMapFrame.enableStatusBar( true );
        
        jMapFrame.enableInputMethods( true );
        JToolBar toolBar = jMapFrame.getToolBar();
        toolBar.addSeparator();
        JButton btn = null;
        if (("bairros distritos geomorfologia limite-município loteamentos quadras zoneamento limite-municipio").contains(typeName)) {
            btn = new JButton("Visualizar Informações Adicionais");
            btn.addActionListener(e -> jMapFrame.getMapPane().setCursorTool(
            new CursorTool() {  
                @Override
                public void onMouseClicked(MapMouseEvent ev) {
                    try {
                        visualizarInfo(ev);
                    } catch (IOException ex) {
                        Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (CQLException ex) {
                        Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
            ));
        }
        switch (typeName) {
            case "educacao":
                btn = new JButton("Adicionar Ponto - Educação");
                btn.addActionListener(e -> jMapFrame.getMapPane().setCursorTool(
                        new CursorTool() {
                            @Override
                            public void onMouseClicked(MapMouseEvent ev) {
                                try {
                                    adicionarEducacao(ev);
                                } catch (IOException ex) {
                                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                        }
                )); break;
            case "educacao_inf":
                btn = new JButton("Adicionar Ponto - Educação Infantil");
                btn.addActionListener(e -> jMapFrame.getMapPane().setCursorTool(
                        new CursorTool() {
                            @Override
                            public void onMouseClicked(MapMouseEvent ev) {
                                try {
                                    adicionarEducacaoInf(ev);
                                } catch (IOException ex) {
                                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                        }
                )); break;
            case "esporte":
                btn = new JButton("Adicionar Ponto - Esporte");
                break;
            case "pontos_saude":
                btn = new JButton("Adicionar Ponto - Pontos de Saude");
                break;
            case "pontos_taxi":
                btn = new JButton("Adicionar Ponto - Pontos Taxi");        
                break;
            default:
                break;
        }
        toolBar.add(btn);
        
        Style style = createDefaultStyle();
        FeatureLayer layer = new FeatureLayer(featureSource, style);
        
        
        ////
        //adicionar featureSource 2
        String typeName2 = (String) featureTypeCBox2.getSelectedItem();
        featureSource = dataStore.getFeatureSource(typeName);
        setGeometry(featureSource);
        SimpleFeatureSource source2 = dataStore.getFeatureSource(typeName2);
        setGeometry(source2);
        Style style2 = createDefaultStyle();
        FeatureLayer layer2 = new FeatureLayer(source2, style2);
        ////
        mapContent = new MapContent();
        mapContent.addLayer(layer);
        mapContent.addLayer(layer2);
        jMapFrame.setMapContent(mapContent);
        jMapFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        jMapFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
                
        jMapFrame.setVisible(true);
    }
    
    void visualizarInfo(MapMouseEvent ev) throws IOException, CQLException {

        System.out.println("Mouse click at: " + ev.getWorldPos());

        Point screenPos = ev.getPoint();
        Rectangle screenRect = new Rectangle(screenPos.x-2, screenPos.y-2, 5, 5);

        AffineTransform screenToWorld = jMapFrame.getMapPane().getScreenToWorldTransform();
        Rectangle2D worldRect = screenToWorld.createTransformedShape(screenRect).getBounds2D();
        ReferencedEnvelope bbox = new ReferencedEnvelope(worldRect, jMapFrame.getMapContent().getCoordinateReferenceSystem());

        Filter filter = CQL.toFilter("BBOX(geom, " + bbox.getMinX() +
        ", " + bbox.getMinY() + ", " + bbox.getMaxX() + ", " + bbox.getMaxY()
        + ")");
        try {
            SimpleFeatureCollection selectedFeatures =  featureSource.getFeatures(filter);

            Set<FeatureId> IDs = new HashSet<>();
            try (SimpleFeatureIterator iter = selectedFeatures.features()) {
                while (iter.hasNext()) {
                    
                    SimpleFeature feature = iter.next();                    
                    IDs.add(feature.getIdentifier());
                    System.out.println("   " + feature.getIdentifier());
                    Geometry geom = (Geometry) feature.getDefaultGeometry();
                    
                    displaySelectedFeatures(IDs);
                    DecimalFormat df = new DecimalFormat("#.##");
                    JOptionPane.showMessageDialog(jMapFrame, "Area: " 
                            + df.format(geom.getArea()/1000000)+" Km²\n Nome: " 
                            +(String) feature.getAttribute("nome"), "Informações", JOptionPane.PLAIN_MESSAGE);
                    break;
                }
            }

            if (IDs.isEmpty()) {
                System.out.println("   no feature selected");
            }

        } catch (IOException | NoSuchElementException ex) {
            ex.printStackTrace();
        }
        
    }
    void adicionarEducacao(MapMouseEvent ev) throws IOException {
        DirectPosition2D p = ev.getWorldPos();
        SimpleFeatureBuilder BLDR = new SimpleFeatureBuilder(TYPEEducacao);
        Coordinate pos = new Coordinate(p.getX(), p.getY());
        String typeName = (String) featureTypeCBox.getSelectedItem();
        featureSource = dataStore.getFeatureSource(typeName);        
        Style style = SLD.createSimpleStyle(featureSource.getSchema());
        featureCollection = new DefaultFeatureCollection("internal", TYPEEducacao);
        featureCollection.add(createFeature(BLDR, pos, 1,typeName));
        FeatureLayer layer = new FeatureLayer(featureCollection, style);      
        SimpleFeatureStore store = (SimpleFeatureStore) dataStore.getFeatureSource( typeName );    
        Transaction transaction = new DefaultTransaction("Add Example");
        store.setTransaction( transaction );
        try {
            store.addFeatures( featureCollection );
            transaction.commit();
        }
        catch( Exception eek){
            transaction.rollback();
        }
        mapContent.addLayer(layer);
        jMapFrame.repaint();          
    }
    void adicionarEducacaoInf(MapMouseEvent ev) throws IOException {
        DirectPosition2D p = ev.getWorldPos();
        SimpleFeatureBuilder BLDR = new SimpleFeatureBuilder(TYPEEducacao_inf);
        Coordinate pos = new Coordinate(p.getX(), p.getY());
        String typeName = (String) featureTypeCBox.getSelectedItem();
        featureSource = dataStore.getFeatureSource(typeName);        
        Style style = SLD.createSimpleStyle(featureSource.getSchema());
        featureCollection = new DefaultFeatureCollection("internal", TYPEEducacao_inf);
        featureCollection.add(createFeature(BLDR, pos, 1,typeName));
        FeatureLayer layer = new FeatureLayer(featureCollection, style);      
        SimpleFeatureStore store = (SimpleFeatureStore) dataStore.getFeatureSource( typeName );    
        Transaction transaction = new DefaultTransaction("Add Example");
        store.setTransaction( transaction );
        try {
            store.addFeatures( featureCollection );
            transaction.commit();
        }
        catch( Exception eek){
            transaction.rollback();
        }
        mapContent.addLayer(layer);
        jMapFrame.repaint();          
    }

    private SimpleFeature createFeature(SimpleFeatureBuilder bldr, Coordinate pos, int id, String typeName) {
        GeometryFactory geofactory = JTSFactoryFinder.getGeometryFactory(new Hints(Hints.JTS_SRID,id));
        com.vividsolutions.jts.geom.Point p = geofactory.createPoint(pos);
        if (typeName.equals("educacao")) {
            bldr.add(p);
            bldr.add("Santa Monica");
            bldr.add("Aroldo Schemberger");
            bldr.add(343);
            bldr.add("Jardim Carvalho");
            bldr.add(84016740);
        }
        else if (typeName.equals("educacao_inf")) {
            bldr.add(p);
            bldr.add("Santa Monica");
            bldr.add("Aroldo Schemberger");
            bldr.add(343);
        }
        return bldr.buildFeature(null);
    }
    public void displaySelectedFeatures(Set<FeatureId> IDs) {
        Style style;

        if (IDs.isEmpty()) {
            style = createDefaultStyle();

        } else {
            style = createSelectedStyle(IDs);
        }

        Layer layer = jMapFrame.getMapContent().layers().get(0);
        ((FeatureLayer) layer).setStyle(style);
        jMapFrame.getMapPane().repaint();
    }

    private Style createDefaultStyle() {
        Rule rule = createRule(LINE_COLOUR, FILL_COLOUR);

        FeatureTypeStyle fts = sf.createFeatureTypeStyle();
        fts.rules().add(rule);

        StyleBuilder styleBuilder = new StyleBuilder();
        font = styleBuilder.createFont(new java.awt.Font("Verdana",java.awt.Font.PLAIN,11));
        Style style = SLD.createPolygonStyle(Color.red, Color.green, 1f, "nome", font);
     
        style.featureTypeStyles().add(fts);
        return style;
    }

    private Style createSelectedStyle(Set<FeatureId> IDs) {
        Rule selectedRule = createRule(SELECTED_COLOUR, SELECTED_COLOUR);
        selectedRule.setFilter(ff.id(IDs));

        Rule otherRule = createRule(LINE_COLOUR, FILL_COLOUR);
        otherRule.setElseFilter(true);

        FeatureTypeStyle fts = sf.createFeatureTypeStyle();
        fts.rules().add(selectedRule);
        fts.rules().add(otherRule);

        StyleBuilder styleBuilder = new StyleBuilder();
        font = styleBuilder.createFont(new java.awt.Font("Verdana",java.awt.Font.PLAIN,11));
        Style style = SLD.createPolygonStyle(Color.red, Color.green, 1f, "nome", font);
        style.featureTypeStyles().add(fts);
        return style;
    }

    private Rule createRule(Color outlineColor, Color fillColor) {
        Symbolizer symbolizer = null;
        Fill fill;
        Stroke stroke = sf.createStroke(ff.literal(outlineColor), ff.literal(LINE_WIDTH));

        switch (geometryType) {
            case POLYGON:
                fill = sf.createFill(ff.literal(fillColor), ff.literal(OPACITY));
                symbolizer = sf.createPolygonSymbolizer(stroke, fill, geometryAttributeName);
                break;

            case LINE:
                symbolizer = sf.createLineSymbolizer(stroke, geometryAttributeName);
                break;

            case POINT:
                fill = sf.createFill(ff.literal(fillColor), ff.literal(OPACITY));

                Mark mark = sf.getCircleMark();
                mark.setFill(fill);
                mark.setStroke(stroke);

                Graphic graphic = sf.createDefaultGraphic();
                graphic.graphicalSymbols().clear();
                graphic.graphicalSymbols().add(mark);
                graphic.setSize(ff.literal(POINT_SIZE));

                symbolizer = sf.createPointSymbolizer(graphic, geometryAttributeName);
        }

        Rule rule = sf.createRule();
        rule.symbolizers().add(symbolizer);
        return rule;
    }

    private void setGeometry(SimpleFeatureSource source) {
        GeometryDescriptor geomDesc = source.getSchema().getGeometryDescriptor();
        geometryAttributeName = geomDesc.getLocalName();

        Class<?> clazz = geomDesc.getType().getBinding();

        if (Polygon.class.isAssignableFrom(clazz) ||
                MultiPolygon.class.isAssignableFrom(clazz)) {
            geometryType = GeomType.POLYGON;

        } else if (LineString.class.isAssignableFrom(clazz) ||
                MultiLineString.class.isAssignableFrom(clazz)) {

            geometryType = GeomType.LINE;

        } else {
            geometryType = GeomType.POINT;
        }

    }
    public static void main(String[] args) throws Exception {
        JFrame frame = new Main();
        frame.setVisible(true);
    }
}
