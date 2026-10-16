package estructura;

import modelo.Conexion;
import modelo.Estacion;

/**
 * TDA Grafo Ponderado No Dirigido con Lista de Adyacencia[span_14](start_span)[span_14](end_span)[span_15](start_span)[span_15](end_span).
 */
public class Grafo {
    private static class NodoAdyacencia {
        Conexion conexion;
        NodoAdyacencia siguiente;

        NodoAdyacencia(Conexion conexion) {
            this.conexion = conexion;
            this.siguiente = null;
        }
    }

    private TablaHash indexEstaciones;
    private ListaEnlazada<Estacion> estaciones;
    private ListaEnlazada<Conexion> conexiones;

    public Grafo() {
        this.indexEstaciones = new TablaHash();
        this.estaciones = new ListaEnlazada<>();
        this.conexiones = new ListaEnlazada<>();
    }

    public void agregarEstacion(Estacion estacion) {
        if (indexEstaciones.buscar(estacion.getId()) == null) {
            indexEstaciones.insertar(estacion.getId(), estacion);
            estaciones.agregar(estacion);
        }
    }

    public void agregarConexion(String idOrigen, String idDestino, int tiempo) {
        Estacion e1 = indexEstaciones.buscar(idOrigen);
        Estacion e2 = indexEstaciones.buscar(idDestino);

        if (e1 != null && e2 != null) {
            Conexion c1 = new Conexion(e1, e2, tiempo);
            Conexion c2 = new Conexion(e2, e1, tiempo); // Grafo no dirigido
            conexiones.agregar(c1);
            conexiones.agregar(c2);
        }
    }

    public boolean eliminarEstacion(String idEstacion) {
        Estacion e = indexEstaciones.buscar(idEstacion);
        if (e == null) return false;

        e.setActiva(false); // Desactivar lógica
        indexEstaciones.eliminar(idEstacion);
        estaciones.eliminar(e);
        return true;
    }

    public ListaEnlazada<Conexion> obtenerAdyacentes(Estacion e) {
        ListaEnlazada<Conexion> adyacentes = new ListaEnlazada<>();
        ListaEnlazada.NodoLista<Conexion> aux = conexiones.getCabeza();

        while (aux != null) {
            Conexion c = aux.dato;
            if (c.isActiva() && c.getOrigen().equals(e) && c.getDestino().isActiva()) {
                adyacentes.agregar(c);
            }
            aux = aux.siguiente;
        }
        return adyacentes;
    }

    public TablaHash getIndexEstaciones() { return indexEstaciones; }
    public ListaEnlazada<Estacion> getEstaciones() { return estaciones; }
    public ListaEnlazada<Conexion> getConexiones() { return conexiones; }
}