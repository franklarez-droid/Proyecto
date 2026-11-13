package vista;

import estructura.Grafo;
import estructura.ListaEnlazada;
import modelo.Conexion;
import modelo.Estacion;
import java.awt.BorderLayout;
import javax.swing.JPanel;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.swing_viewer.SwingViewer;
import org.graphstream.ui.view.View;
import org.graphstream.ui.view.Viewer;

/**
 * Muestra las estaciones y conexiones del metro usando GraphStream.
 * Permite distinguir visualmente el recorrido calculado por Dijkstra.
 */
public class VisualizadorGrafo{

    private final Graph graphGS;
    private SwingViewer viewer;
    private final String estiloCSS;

    /**
     * Crea el grafo visual y define los colores y tamaÃ±os de sus elementos.
     */
    public VisualizadorGrafo() {
        graphGS = new SingleGraph("RedMultimodal");

        estiloCSS = "node {"
                + "fill-color: #69b8ed;"
                + "size: 22px;"
                + "text-size: 9;"
                + "text-color: #000000;"
                + "text-alignment: center;"
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
     * Dibuja las estaciones y conexiones activas de la red.
     *
     * @param grafoPropio grafo que contiene la red de transporte
     * @return panel de Swing con la representacion del grafo
     */
    public JPanel renderizarGrafo(Grafo grafoPropio) {
        return renderizarGrafo(grafoPropio, null);
    }

    /**
     * Dibuja la red y, si se proporciona una ruta, resalta su recorrido.
     *
     * @param grafoPropio grafo que contiene las estaciones y conexiones
     * @param ruta estaciones de la ruta optima en orden, o null si no hay ruta
     * @return panel de Swing con el grafo dibujado
     */
    public JPanel renderizarGrafo(Grafo grafoPropio,
                                  ListaEnlazada<Estacion> ruta) {

        // Cerrar la vista anterior antes de dibujar nuevamente.
        if (viewer != null) {
            viewer.close();
        }

        graphGS.clear();
        graphGS.setAttribute("ui.stylesheet", estiloCSS);

        // Agregar solamente las estaciones activas.
        ListaEnlazada.NodoLista<Estacion> estacionActual =
                grafoPropio.getEstaciones().getCabeza();

        while (estacionActual != null) {
            Estacion estacion = estacionActual.dato;

            if (estacion.isActiva()) {
                Node nodo = graphGS.addNode(estacion.getId());
                nodo.setAttribute("ui.label", estacion.getId());
            }

            estacionActual = estacionActual.siguiente;
        }

        // Dibujar cada conexion no dirigida una sola vez.
        ListaEnlazada.NodoLista<Conexion> conexionActual =
                grafoPropio.getConexiones().getCabeza();

        while (conexionActual != null) {
            Conexion conexion = conexionActual.dato;

            if (conexion.isActiva()
                    && conexion.getOrigen().isActiva()
                    && conexion.getDestino().isActiva()) {

                String origen = conexion.getOrigen().getId();
                String destino = conexion.getDestino().getId();

                if (!origen.equals(destino)
                        && graphGS.getNode(origen) != null
                        && graphGS.getNode(destino) != null) {

                    String idArista = obtenerIdArista(origen, destino);
                    if (graphGS.getEdge(idArista) == null) {
                        Edge arista = graphGS.addEdge(idArista, origen, destino);
                        arista.setAttribute("ui.label",
                                conexion.getTiempoMinutos() + "m");
                    }
                }
            }

            conexionActual = conexionActual.siguiente;
        }

        // Resaltar las estaciones y los tramos que forman la ruta optima.
        if (ruta != null) {
            ListaEnlazada.NodoLista<Estacion> paso = ruta.getCabeza();

            while (paso != null) {
                Node nodo = graphGS.getNode(paso.dato.getId());
                if (nodo != null) {
                    nodo.setAttribute("ui.class", "ruta");
                }

                if (paso.siguiente != null) {
                    String idArista = obtenerIdArista(
                            paso.dato.getId(), paso.siguiente.dato.getId());
                    Edge arista = graphGS.getEdge(idArista);
                    if (arista != null) {
                        arista.setAttribute("ui.class", "ruta");
                    }
                }
                paso = paso.siguiente;
            }
        }

        // GraphStream organiza automaticamente las posiciones del grafo.
        viewer = new SwingViewer(graphGS,
                Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
        viewer.enableAutoLayout();
        View vista = viewer.addDefaultView(false);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add((JPanel) vista, BorderLayout.CENTER);
        return panel;
    }

    /**
     * Obtiene el mismo identificador para una conexion en ambos sentidos.
     * Esto evita dibujar dos veces una arista de un grafo no dirigido.
     *
     * @param origen identificador de la primera estacion
     * @param destino identificador de la segunda estacion
     * @return identificador unico de la conexion para el visualizador
     */
    private String obtenerIdArista(String origen, String destino) {
        String primero = origen.compareTo(destino) < 0 ? origen : destino;
        String segundo = origen.compareTo(destino) < 0 ? destino : origen;
        return "E_" + primero.length() + "_" + primero + "_" + segundo;
    }
}
