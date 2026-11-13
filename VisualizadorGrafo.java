package vista;

import estructura.Grafo;
import estructura.ListaEnlazada;
import modelo.Conexion;
import modelo.Estacion;
import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.SwingUtilities;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.Edge;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.geom.Point3;
import org.graphstream.ui.swing_viewer.SwingViewer;
import org.graphstream.ui.view.View;
import org.graphstream.ui.view.Viewer;
import org.graphstream.ui.view.camera.Camera;

/**
 * Representa visualmente la red de transporte mediante GraphStream.
 * Permite navegar con zoom, arrastre y barras de desplazamiento.
 * No implementa estructuras de datos del grafo.
 */
public class VisualizadorGrafo {

    private final Graph graphGS;
    private SwingViewer viewer;
    private final String estiloCSS;

    /**
     * Inicializa el grafo visual y sus estilos.
     */
    public VisualizadorGrafo() {
        System.setProperty("gs.ui.renderer",
                "org.graphstream.ui.j2dviewer.J2DGraphRenderer");

        graphGS = new SingleGraph("RedMultimodal");
        estiloCSS = "node {"
                + "fill-color: #69b8ed;"
                + "size: 22px;"
                + "text-size: 9;"
                + "text-color: #000000;"
                + "text-alignment: center;"
                + "text-offset: 0px, 0px;"
                + "stroke-mode: plain;"
                + "stroke-color: #287fb5;"
                + "stroke-width: 1px;"
                + "}"
                + "edge {"
                + "fill-color: #555555;"
                + "size: 2px;"
                + "text-size: 10;"
                + "text-color: #000000;"
                + "text-alignment: along;"
                + "}"
                + "node.ruta {"
                + "fill-color: #ffd54f;"
                + "stroke-color: #e65100;"
                + "stroke-width: 2px;"
                + "}"
                + "edge.ruta {"
                + "fill-color: #e53935;"
                + "size: 4px;"
                + "}";
        graphGS.setAttribute("ui.stylesheet", estiloCSS);
    }

    /**
     * Crea la vista de las estaciones y conexiones activas del grafo.
     *
     * @param grafoPropio red de transporte que debe representarse
     * @return panel Swing con el grafo y sus controles de navegación
     */
    public JPanel renderizarGrafo(Grafo grafoPropio) {
        return renderizarGrafo(grafoPropio, null);
    }

    /**
     * Representa la red y destaca opcionalmente la ruta más rápida.
     *
     * @param grafoPropio grafo que debe representarse
     * @param ruta secuencia ordenada de estaciones o null para vista normal
     * @return panel con el grafo y controles de navegación
     */
    public JPanel renderizarGrafo(Grafo grafoPropio, ListaEnlazada<Estacion> ruta) {
        // Detener la vista anterior antes de reconstruir el grafo.
        if (viewer != null) {
            viewer.close();
            viewer = null;
        }

        graphGS.clear();
        graphGS.setAttribute("ui.stylesheet", estiloCSS);

        ListaEnlazada.NodoLista<Estacion> auxE =
                grafoPropio.getEstaciones().getCabeza();
        while (auxE != null) {
            Estacion estacion = auxE.dato;
            if (estacion.isActiva()) {
                Node nodo = graphGS.addNode(estacion.getId());
                nodo.setAttribute("ui.label", estacion.getId());
            }
            auxE = auxE.siguiente;
        }

        ListaEnlazada.NodoLista<Conexion> auxC =
                grafoPropio.getConexiones().getCabeza();
        while (auxC != null) {
            Conexion conexion = auxC.dato;
            if (conexion.isActiva()
                    && conexion.getOrigen().isActiva()
                    && conexion.getDestino().isActiva()) {
                String origen = conexion.getOrigen().getId();
                String destino = conexion.getDestino().getId();
                if (graphGS.getNode(origen) != null
                        && graphGS.getNode(destino) != null
                        && !origen.equals(destino)) {
                    String primero = origen.compareTo(destino) < 0 ? origen : destino;
                    String segundo = origen.compareTo(destino) < 0 ? destino : origen;
                    String idArista = "E_" + primero.length() + "_" + primero
                            + "_" + segundo;
                    if (graphGS.getEdge(idArista) == null) {
                        Edge arista = graphGS.addEdge(idArista, origen, destino);
                        arista.setAttribute("ui.label",
                                conexion.getTiempoMinutos() + "m");
                    }
                }
            }
            auxC = auxC.siguiente;
        }

        // Marcar nodos y tramos consecutivos de la ruta calculada.
        if (ruta != null) {
            ListaEnlazada.NodoLista<Estacion> paso = ruta.getCabeza();
            while (paso != null) {
                Node nodo = graphGS.getNode(paso.dato.getId());
                if (nodo != null) {
                    nodo.setAttribute("ui.class", "ruta");
                }
                if (paso.siguiente != null) {
                    String a = paso.dato.getId();
                    String b = paso.siguiente.dato.getId();
                    String primero = a.compareTo(b) < 0 ? a : b;
                    String segundo = a.compareTo(b) < 0 ? b : a;
                    String id = "E_" + primero.length() + "_" + primero
                            + "_" + segundo;
                    Edge tramo = graphGS.getEdge(id);
                    if (tramo != null) {
                        tramo.setAttribute("ui.class", "ruta");
                    }
                }
                paso = paso.siguiente;
            }
        }

        viewer = new SwingViewer(graphGS,
                Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
        viewer.enableAutoLayout();
        View view = viewer.addDefaultView(false);
        JPanel panelGrafo = (JPanel) view;
        JPanel contenedor = new JPanel(new BorderLayout());
        contenedor.add(panelGrafo, BorderLayout.CENTER);
        configurarNavegacion(view, contenedor);
        return contenedor;
    }

    /**
     * Habilita el zoom, el desplazamiento y las barras vinculadas a la cámara.
     *
     * @param view vista del grafo
     * @param contenedor panel donde se colocan las barras
     */
    private void configurarNavegacion(View view, JPanel contenedor) {
        JPanel panel = (JPanel) view;
        Camera camara = view.getCamera();

        JScrollBar horizontal = new JScrollBar(JScrollBar.HORIZONTAL, 500, 1, 0, 1001);
        JScrollBar vertical = new JScrollBar(JScrollBar.VERTICAL, 500, 1, 0, 1001);
        contenedor.add(horizontal, BorderLayout.SOUTH);
        contenedor.add(vertical, BorderLayout.EAST);

        // Referencia del encuadre inicial y alcance aproximado en unidades del grafo.
        final double[] origen = new double[2];
        final double[] alcance = {1.0};
        final boolean[] iniciado = {false};
        final boolean[] sincronizando = {false};

        Runnable iniciar = () -> {
            if (!iniciado[0]) {
                Point3 centro = camara.getViewCenter();
                origen[0] = centro.x;
                origen[1] = centro.y;
                alcance[0] = Math.max(1.0, camara.getGraphDimension()) * 1.5;
                iniciado[0] = true;
            }
        };

        Runnable actualizarBarras = () -> {
            if (!iniciado[0]) {
                return;
            }
            sincronizando[0] = true;
            try {
                Point3 centro = camara.getViewCenter();
                int h = 500 + (int) Math.round(
                        (centro.x - origen[0]) * 500.0 / alcance[0]);
                int v = 500 - (int) Math.round(
                        (centro.y - origen[1]) * 500.0 / alcance[0]);
                horizontal.setValue(Math.max(0, Math.min(1000, h)));
                vertical.setValue(Math.max(0, Math.min(1000, v)));
            } finally {
                sincronizando[0] = false;
            }
        };

        javax.swing.event.ChangeListener moverConBarras = e -> {
            if (sincronizando[0]) {
                return;
            }
            iniciar.run();
            camara.setAutoFitView(false);
            Point3 centro = camara.getViewCenter();
            double x = origen[0] + (horizontal.getValue() - 500)
                    * alcance[0] / 500.0;
            double y = origen[1] - (vertical.getValue() - 500)
                    * alcance[0] / 500.0;
            camara.setViewCenter(x, y, centro.z);
            panel.repaint();
        };
        horizontal.getModel().addChangeListener(moverConBarras);
        vertical.getModel().addChangeListener(moverConBarras);

        MouseAdapter mouse = new MouseAdapter() {
            private int anteriorX;
            private int anteriorY;
            private boolean arrastrando;

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                iniciar.run();
                camara.setAutoFitView(false);
                double nuevo = camara.getViewPercent()
                        * Math.pow(1.15, e.getPreciseWheelRotation());
                camara.setViewPercent(Math.max(0.05, Math.min(5.0, nuevo)));
                panel.repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    iniciar.run();
                    anteriorX = e.getX();
                    anteriorY = e.getY();
                    arrastrando = true;
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (!arrastrando) {
                    return;
                }
                camara.setAutoFitView(false);
                Point3 antes = camara.transformPxToGu(anteriorX, anteriorY);
                Point3 despues = camara.transformPxToGu(e.getX(), e.getY());
                Point3 centro = camara.getViewCenter();
                camara.setViewCenter(
                        centro.x + antes.x - despues.x,
                        centro.y + antes.y - despues.y,
                        centro.z);
                anteriorX = e.getX();
                anteriorY = e.getY();
                actualizarBarras.run();
                panel.repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                arrastrando = false;
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)
                        && e.getClickCount() == 2) {
                    camara.resetView();
                    iniciado[0] = false;
                    sincronizando[0] = true;
                    try {
                        horizontal.setValue(500);
                        vertical.setValue(500);
                    } finally {
                        sincronizando[0] = false;
                    }
                    panel.repaint();
                }
            }
        };

        panel.addMouseWheelListener(mouse);
        panel.addMouseListener(mouse);
        panel.addMouseMotionListener(mouse);
    }
}
