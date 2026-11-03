package vista;

import estructura.Grafo;
import estructura.ListaEnlazada;
import modelo.Conexion;
import modelo.Estacion;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.swing_viewer.SwingViewer;
import org.graphstream.ui.view.View;
import org.graphstream.ui.view.Viewer;

import javax.swing.JPanel;
import java.awt.BorderLayout;

import estructura.Grafo;
import modelo.Estacion;

/**
 * Encargado del renderizado gráfico de la red usando GraphStream[cite: 2].
 */
public class VisualizadorGrafo {

    private Graph graphGS;
    private SwingViewer viewer;

    public VisualizadorGrafo() {
        System.setProperty("gs.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
        graphGS = new SingleGraph("RedMultimodal");

        // Hoja de estilos CSS para GraphStream
        String estiloCSS = 
            "node {" +
            "   fill-color: #3498db;" +
            "   size: 25px;" +
            "   label-size: 11px;" +
            "   text-alignment: center;" +
            "   stroke-mode: solid;" +
            "   stroke-color: #ffffff;" +
            "   stroke-width: 2px;" +
            "}" +
            "edge {" +
            "   fill-color: #7f8c8d;" +
            "   size: 2px;" +
            "   label-size: 11px;" +
            "}";

        graphGS.setAttribute("ui.stylesheet", estiloCSS);
    }

    public JPanel renderizarGrafo(Grafo grafoPropio) {
        graphGS.clear(); // Limpia la gráfica previa
        graphGS.setAttribute("ui.stylesheet", (String) graphGS.getAttribute("ui.stylesheet"));

        // 1. Agregar Nodos (Estaciones activas)
        ListaEnlazada.NodoLista<Estacion> auxE = grafoPropio.getEstaciones().getCabeza();
        while (auxE != null) {
            Estacion est = auxE.dato;
            if (est.isActiva()) {
                Node n = graphGS.addNode(est.getId());
                n.setAttribute("ui.label", est.getId());
            }
            auxE = auxE.siguiente;
        }

        // 2. Agregar Aristas (Conexiones)
        ListaEnlazada.NodoLista<Conexion> auxC = grafoPropio.getConexiones().getCabeza();
        int edgeId = 0;
        while (auxC != null) {
            Conexion con = auxC.dato;
            if (con.isActiva() && con.getOrigen().isActiva() && con.getDestino().isActiva()) {
                String idEdge = "E_" + (edgeId++);
                // Evita duplicar aristas bidireccionales en el renderizador
                if (graphGS.getEdge(idEdge) == null && 
                    graphGS.getNode(con.getOrigen().getId()) != null && 
                    graphGS.getNode(con.getDestino().getId()) != null) {
                    
                    try {
                        org.graphstream.graph.Edge e = graphGS.addEdge(
                            idEdge, 
                            con.getOrigen().getId(), 
                            con.getDestino().getId()
                        );
                        e.setAttribute("ui.label", con.getTiempoMinutos() + "m");
                    } catch (Exception ex) {
                        // Ignora aristas repetidas en el visor
                    }
                }
            }
            auxC = auxC.siguiente;
        }

        // 3. Crear componente Swing
        viewer = new SwingViewer(graphGS, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
        viewer.enableAutoLayout();
        View view = viewer.addDefaultView(false); // false = incrustar dentro de un JPanel

        JPanel panelContenedor = new JPanel(new BorderLayout());
        panelContenedor.add((JPanel) view, BorderLayout.CENTER);
        return panelContenedor;
    }
    
    
}