
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
import org.geotools.data.DataStore;
import org.geotools.data.Query;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.swing.action.SafeAction;
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
import static org.geotools.swing.JMapFrame.Tool.*;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.geotools.swing.event.MapMouseEvent;
import org.geotools.swing.tool.CursorTool;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.identity.FeatureId;


public class Main extends JFrame {
    private static final Color LINHA_COR = Color.BLUE;
    private static final Color PREENCHIMENTO_COR = Color.CYAN;
    private static final Color LINHA_COR2 = Color.RED;
    private static final Color PREENCHIMENTO_COR2 = Color.GREEN;
    private static final Color COR_SELECIONADA = Color.YELLOW;
    private static final float OPACITY = 1.0f;
    private static final float LINE_WIDTH = 1.0f;
    private static final float POINT_SIZE = 4.0f;
    private static Font font;
    private JComboBox featureTypeCBox;
    private JComboBox featureTypeCBox2;
    private String geometryAttributeName;
    private final StyleFactory sf = CommonFactoryFinder.getStyleFactory();
    private final FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2();
    private enum GeomType { POINT, LINE, POLYGON };
    private GeomType geometryType;
    private DataStore dataStore;
    private static SimpleFeatureSource featureSource;
    private static SimpleFeatureSource featureSource2;
    private MapContent mapContent;
    private JMapFrame jMapFrame;
    private DefaultFeatureCollection featureCollection;
    final SimpleFeatureType TYPEEducacao = DataUtilities.createType("educacao",
        "geom:Point:srid=4326," +
        "nome:String," +
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
    final SimpleFeatureType TYPEEsporte = DataUtilities.createType("esporte",
        "geom:Point:srid=4326," +
        "tipo:String," +
        "nome:String," +
        "logradouro:String," +
        "numero:double," +
        "localiza:String"
    );
    final SimpleFeatureType TYPEPontosSaude = DataUtilities.createType("pontos_saude",
        "geom:Point:srid=4326," +
        "nome:String," +
        "rua:String," +
        "numero:double"
    );
    
    public Main() throws IOException, Exception {
        configuracaoInicial();
    }    
    private void configuracaoInicial() throws IOException{
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
        dataMenu.add(new SafeAction("Exibir Grafico") {
            @Override
            public void action(ActionEvent e) throws Throwable {
                exibirGrafico();
            }
        });
        
        ComboBoxModel cbm = new DefaultComboBoxModel(dataStore.getTypeNames());
        ComboBoxModel cbm2 = new DefaultComboBoxModel(dataStore.getTypeNames());
        
        featureTypeCBox.setModel(cbm);
        //remover tabelas referenets ao controle do postgis
        featureTypeCBox.removeItemAt(9);
        featureTypeCBox.removeItemAt(12);
        featureTypeCBox.removeItemAt(12);
        featureTypeCBox2.setModel(cbm2);
        //remover tabelas referenets ao controle do postgis
        featureTypeCBox2.removeItemAt(9);
        featureTypeCBox2.removeItemAt(12);
        featureTypeCBox2.removeItemAt(12);
        
        StyleBuilder styleBuilder = new StyleBuilder();
        font = styleBuilder.createFont(new java.awt.Font("Verdana",java.awt.Font.PLAIN,11));
        
    }      
    private void exibirGrafico() throws Exception {
        String typeName = (String) featureTypeCBox.getSelectedItem();
        String typeName2 = (String) featureTypeCBox2.getSelectedItem();
        featureSource = dataStore.getFeatureSource(typeName);
        definirGeometria(featureSource);
        Style style = criarEstiloPadraoLayer1();
        FeatureLayer layer = new FeatureLayer(featureSource, style);
        featureSource2 = dataStore.getFeatureSource(typeName2);
        SimpleFeatureSource source2 = dataStore.getFeatureSource(typeName2);
        definirGeometria(source2);
        Style style2 = criarEstiloPadraoLayer2();
        FeatureLayer layer2 = new FeatureLayer(source2, style2);
        mapContent = new MapContent();
        jMapFrame = new JMapFrame();
        jMapFrame.enableToolBar( true );
        jMapFrame.enableStatusBar( false );
        jMapFrame.enableLayerTable( true );
        jMapFrame.enableTool(PAN, ZOOM, SCROLLWHEEL, RESET);
        
        jMapFrame.enableInputMethods( true );
        JToolBar toolBar = jMapFrame.getToolBar();
        toolBar.addSeparator();
        JButton btn = null;
        if (("bairros distritos geomorfologia limite-município loteamentos quadras zoneamento limite-municipio").contains(typeName)) {
            if (("bairros distritos geomorfologia limite-município loteamentos quadras zoneamento limite-municipio").contains(typeName2)) {
                btn = new JButton("Visualizar Informações Adicionais");
                btn.addActionListener(e -> jMapFrame.getMapPane().setCursorTool(
                new CursorTool() {
                    @Override
                    public void onMouseClicked(MapMouseEvent ev) {
                        try {
                            visualizarInfo(ev);
                        } catch (CQLException | IOException ex) {
                            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
                ));
                mapContent.addLayer(layer);
            }
            else{
                btn = new JButton("Visualizar Incidência de Pontos");
                btn.addActionListener((ActionEvent e) -> jMapFrame.getMapPane().setCursorTool(
                new CursorTool() {
                    @Override
                    public void onMouseClicked(MapMouseEvent ev) {
                        try {
                            visualizarPontos(ev);
                        } catch (IOException | CQLException ex) {
                            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
                ));
                mapContent.addLayer(layer);
                mapContent.addLayer(layer2);
            }
        }
        else{
            switch (typeName) {
                case "educacao":
                    btn = new JButton("Adicionar Ponto - Educação");
                    break;
                case "educacao_inf":
                    btn = new JButton("Adicionar Ponto - Educação Infantil");
                    break;
                case "esporte":
                    btn = new JButton("Adicionar Ponto - Esporte");
                    break;
                case "pontos_saude":
                    btn = new JButton("Adicionar Ponto - Pontos de Saude");
                    break;
                default:
                    break;
            }
            btn.addActionListener(e -> jMapFrame.getMapPane().setCursorTool(
                new CursorTool() {
                    @Override
                    public void onMouseClicked(MapMouseEvent ev) {
                        try {
                            adicionarPonto(ev, typeName);
                        } catch (IOException ex) {
                            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            ));
            mapContent.addLayer(layer2);
            mapContent.addLayer(layer);
        }
        toolBar.add(btn);
        
        jMapFrame.setMapContent(mapContent);
        jMapFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        jMapFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);                
        jMapFrame.setVisible(true);
    }  
    void visualizarInfo(MapMouseEvent ev) throws IOException, CQLException {
        Point coordenadasClick = ev.getPoint();
        Rectangle retanguloAux = new Rectangle(coordenadasClick.x-2, 
                                               coordenadasClick.y-2, 5, 5);

        AffineTransform screenToWorld = jMapFrame.getMapPane().getScreenToWorldTransform();
        Rectangle2D worldRect = screenToWorld.createTransformedShape(retanguloAux).getBounds2D();
        ReferencedEnvelope bbox = new ReferencedEnvelope(worldRect, 
                jMapFrame.getMapContent().getCoordinateReferenceSystem());

        Filter filtro = CQL.toFilter("BBOX(geom, " + bbox.getMinX() +
        ", " + bbox.getMinY() + ", " + bbox.getMaxX() + ", " + bbox.getMaxY()
        + ")");
        
        
        try {
            SimpleFeatureCollection featureSelec =  featureSource.getFeatures(filtro);

            Set<FeatureId> IDs = new HashSet<>();
            try (SimpleFeatureIterator iter = featureSelec.features()) {
                while (iter.hasNext()) {
                    
                    SimpleFeature feature = iter.next();                    
                    IDs.add(feature.getIdentifier());
                    System.out.println("   " + feature.getIdentifier());
                    Geometry geom = (Geometry) feature.getDefaultGeometry();
                    
                    visualizarCaracteristicaSelecionada(IDs);
                    DecimalFormat df = new DecimalFormat("#.##");
                    JOptionPane.showMessageDialog(jMapFrame, "Area: " 
                            + df.format(geom.getArea()/1000000)+" Km²\n Nome: " 
                            +(String) feature.getAttribute("nome"), "Informações", 
                            JOptionPane.PLAIN_MESSAGE);
                    break;
                }
            }
            
            jMapFrame.repaint();

            if (IDs.isEmpty()) {
                System.out.println("Nenhum intem selecionado");
            }

        } catch (IOException | NoSuchElementException ex) {
            ex.printStackTrace(System.out);
        }
        
    }
    void visualizarPontos(MapMouseEvent ev) throws IOException, CQLException {
        String nomeFeature;
        Point screenPos = ev.getPoint();
        Rectangle screenRect = new Rectangle(screenPos.x-2, screenPos.y-2, 5, 5);

        AffineTransform screenToWorld = jMapFrame.getMapPane().getScreenToWorldTransform();
        Rectangle2D worldRect = screenToWorld.createTransformedShape(screenRect).getBounds2D();
        ReferencedEnvelope bbox = new ReferencedEnvelope(worldRect, jMapFrame.getMapContent().getCoordinateReferenceSystem());

        Filter filter = CQL.toFilter("BBOX(geom, " + bbox.getMinX() +
        ", " + bbox.getMinY() + ", " + bbox.getMaxX() + ", " + bbox.getMaxY()
        + ")");
        
        SimpleFeatureType schema = featureSource.getSchema();
        String typeName = schema.getTypeName();
        nomeFeature = typeName;
        SimpleFeatureType schema2 = featureSource2.getSchema();
        String typeName2 = schema2.getTypeName();
        String geomName2 = schema2.getGeometryDescriptor().getLocalName();
        
        SimpleFeatureCollection outerFeatures = featureSource.getFeatures(filter);
        SimpleFeatureIterator iterator = outerFeatures.features();
        Set<FeatureId> IDs = new HashSet<>();
        int max = 0;
        try {
            while (iterator.hasNext()) {
                
                SimpleFeature feature = iterator.next();
                IDs.add(feature.getIdentifier());
                visualizarCaracteristicaSelecionada(IDs);
                try {
                    Geometry geometry = (Geometry) feature.getDefaultGeometry();
                    if (!geometry.isValid()) {
                        // desconsiderar geometria inválida
                        continue;
                    }
                    Filter innerFilter = ff.intersects(ff.property(geomName2), ff.literal(geometry));
                    Query innerQuery = new Query(typeName2, innerFilter, Query.NO_NAMES);
                    SimpleFeatureCollection join = featureSource2.getFeatures(innerQuery);
                    int size = join.size();
                    max = Math.max(max, size);
                    nomeFeature = (String) feature.getAttribute("nome");
                } catch (Exception skipBadData) {
                }
            }
        } finally {
            iterator.close();
        }
        JOptionPane.showMessageDialog(jMapFrame, max +  " pontos de "+ typeName2 +" em " + nomeFeature);

        
    }   
    void adicionarPonto(MapMouseEvent ev, String typeName) throws IOException {
        DirectPosition2D p = ev.getWorldPos();
        SimpleFeatureBuilder BLDR = null;
        Coordinate pos = new Coordinate(p.getX(), p.getY());
        featureSource = dataStore.getFeatureSource(typeName);        
        Style style = SLD.createSimpleStyle(featureSource.getSchema());
        switch (typeName) {
            case "educacao_inf":
                BLDR = new SimpleFeatureBuilder(TYPEEducacao_inf);
                featureCollection = new DefaultFeatureCollection("internal", TYPEEducacao_inf);
                break;
            case "educacao":
                BLDR = new SimpleFeatureBuilder(TYPEEducacao);
                featureCollection = new DefaultFeatureCollection("internal", TYPEEducacao);
                break;
            case "esporte":
                BLDR = new SimpleFeatureBuilder(TYPEEsporte);
                featureCollection = new DefaultFeatureCollection("internal", TYPEEsporte);
                break;
            case "pontos_saude":
                BLDR = new SimpleFeatureBuilder(TYPEPontosSaude);
                featureCollection = new DefaultFeatureCollection("internal", TYPEPontosSaude);
                break;
        }
        
        featureCollection.add(criarCaracteristica(BLDR, pos, 1,typeName));
        FeatureLayer layer = new FeatureLayer(featureCollection, style);      
        SimpleFeatureStore store = (SimpleFeatureStore) dataStore.getFeatureSource( typeName );    
        Transaction transaction = new DefaultTransaction("Adicionar Ponto");
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
    private SimpleFeature criarCaracteristica(SimpleFeatureBuilder bldr, Coordinate pos, int id, String typeName) {
        GeometryFactory geofactory = JTSFactoryFinder.getGeometryFactory(new Hints(Hints.JTS_SRID,id));
        com.vividsolutions.jts.geom.Point p = geofactory.createPoint(pos);
        switch (typeName) {
            case "educacao_inf":{
                    bldr.add(p);
                    JFrame frame = new JFrame("Adicionar Informação sobre a Instituição");
                    bldr.add(JOptionPane.showInputDialog(frame, "Nome da Instituição:"));
                    bldr.add(JOptionPane.showInputDialog(frame, "Rua:"));
                    bldr.add(JOptionPane.showInputDialog(frame, "Número:"));
                    break;
                }
            case "educacao":{
                    bldr.add(p);
                    JFrame frame = new JFrame("Adicionar Informação sobre a Instituição");
                    bldr.add(JOptionPane.showInputDialog(frame, "Nome:"));
                    bldr.add(JOptionPane.showInputDialog(frame, "Rua:"));
                    bldr.add(JOptionPane.showInputDialog(frame, "Número:"));
                    bldr.add(JOptionPane.showInputDialog(frame, "Bairro:"));
                    bldr.add(JOptionPane.showInputDialog(frame, "CEP:"));
                    break;
                }
            case "esporte":{
                    bldr.add(p);
                    JFrame frame = new JFrame("Adicionar Informação sobre a Instituição");
                    bldr.add(JOptionPane.showInputDialog(frame, "Tipo:"));
                    bldr.add(JOptionPane.showInputDialog(frame, "Nome:"));
                    bldr.add(JOptionPane.showInputDialog(frame, "Logradouro:"));
                    bldr.add(JOptionPane.showInputDialog(frame, "Numero:"));
                    bldr.add(JOptionPane.showInputDialog(frame, "Localização:"));
                    break;
                }
            case "pontos_saude":{
                    bldr.add(p);
                    JFrame frame = new JFrame("Adicionar Informação sobre a Instituição");
                    bldr.add(JOptionPane.showInputDialog(frame, "Nome:"));
                    bldr.add(JOptionPane.showInputDialog(frame, "Rua:"));
                    bldr.add(JOptionPane.showInputDialog(frame, "Número:"));
                    break;
                }
            default:
                break;
        }
        return bldr.buildFeature(null);
    }
    public void visualizarCaracteristicaSelecionada(Set<FeatureId> IDs) {
        Style style;

        if (IDs.isEmpty()) {
            style = criarEstiloPadraoLayer1();
        } else {
            style = criarEstiloCaracteristicaSelecionada(IDs);
        }

        Layer layer = jMapFrame.getMapContent().layers().get(0);
        ((FeatureLayer) layer).setStyle(style);
        jMapFrame.getMapPane().repaint();
    }
    private Style criarEstiloPadraoLayer1() {
        Style style = SLD.createPolygonStyle(Color.BLACK, Color.BLACK, 1f, "nome", font);
        
        Rule rule = criarRegra(LINHA_COR, PREENCHIMENTO_COR);
        FeatureTypeStyle fts = sf.createFeatureTypeStyle();
        fts.rules().add(rule);

        style.featureTypeStyles().add(fts);
        return style;
    }
    private Style criarEstiloPadraoLayer2() {
        Style style = SLD.createPolygonStyle(Color.BLACK, Color.BLACK, 1f, "nome", font);
        
        Rule rule = criarRegra(LINHA_COR2, PREENCHIMENTO_COR2);
        FeatureTypeStyle fts = sf.createFeatureTypeStyle();
        fts.rules().add(rule);
        
        style.featureTypeStyles().add(fts);
        return style;
    }
    
    private Style criarEstiloCaracteristicaSelecionada(Set<FeatureId> IDs) {
        Style style = SLD.createPolygonStyle(Color.red, Color.green, 1f, "nome", font);
        
        Rule regraRegiaoSelecionada = criarRegra(COR_SELECIONADA, COR_SELECIONADA);
        regraRegiaoSelecionada.setFilter(ff.id(IDs));

        Rule regraOutrasRegioes = criarRegra(LINHA_COR, PREENCHIMENTO_COR);
        regraOutrasRegioes.setElseFilter(true);

        FeatureTypeStyle fts = sf.createFeatureTypeStyle();
        fts.rules().add(regraRegiaoSelecionada);
        fts.rules().add(regraOutrasRegioes);
        
        style.featureTypeStyles().add(fts);
        return style;
    }
    
    private Rule criarRegra(Color outlineColor, Color fillColor) {
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
    
    private void definirGeometria(SimpleFeatureSource source) {
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
