/*
 * Proyecto UrbanRoute - Red de transporte.
 * TDA Grafo Ponderado No Dirigido para el modelado de la red multimodal.
 */
package estructura;

import modelo.Conexion;
import modelo.Estacion;

/**
 * Implementa un TDA Grafo Ponderado No Dirigido desarrollado desde cero.
 * Gestiona las estaciones y sus conexiones bidireccionales, delegando la
 * b&uacute;squeda e indexaci&oacute;n en tiempo O(1) a una TablaHash propia.
 */
public class Grafo {
    
    /**
     * Nodo auxiliar para encadenamiento de adyacencias.
     */
    private static class NodoAdyacencia {
        Conexion conexion;
        NodoAdyacencia siguiente;

        /**
         * Inicializa un nodo de adyacencia con su conexi&oacute;n asociada.
         *
         * @param conexion conexi&oacute;n almacenada en el nodo
         */
        NodoAdyacencia(Conexion conexion) {
            this.conexion = conexion;
            this.siguiente = null;
        }
    }

    private TablaHash indexEstaciones;
    private ListaEnlazada<Estacion> estaciones;
    private ListaEnlazada<Conexion> conexiones;

    /**
     * Inicializa un grafo vac&iacute;o con su tabla de dispersi&oacute;n y listas enlazadas propias.
     */
    public Grafo() {
        this.indexEstaciones = new TablaHash();
        this.estaciones = new ListaEnlazada<>();
        this.conexiones = new ListaEnlazada<>();
    }
    //Modificador de red
    
    /**
     * Incorpora una nueva estaci&oacute;n al grafo y la indexa en la tabla hash
     * siempre que su identificador no exista previamente.
     *
     * @param estacion estaci&oacute;n que se desea registrar en la red
     */
    public void agregarEstacion(Estacion estacion) {
        if (indexEstaciones.buscar(estacion.getId()) == null) {
            indexEstaciones.insertar(estacion.getId(), estacion);
            estaciones.agregar(estacion);
        }
    }
    
    /**
     * Modifica el tiempo de traslado en ambos sentidos de una conexi&oacute;n directa activa.
     *
     * @param idOrigen identificador de la estaci&oacute;n A
     * @param idDestino identificador de la estaci&oacute;n B
     * @param nuevoTiempo nuevo tiempo de traslado en minutos, mayor que cero
     * @return true si la conexi&oacute;n exist&iacute;a y se actualiz&oacute; correctamente, false en caso contrario
     */
    public boolean modificarTiempo(
        String idOrigen, String idDestino, int nuevoTiempo) {

        if (idOrigen == null || idDestino == null
            || idOrigen.equalsIgnoreCase(idDestino)
            || nuevoTiempo <= 0) {
        return false;
    }

    Estacion origen = indexEstaciones.buscar(idOrigen);
    Estacion destino = indexEstaciones.buscar(idDestino);

    if (origen == null || destino == null
            || !origen.isActiva() || !destino.isActiva()) {
        return false;
    }

    // Comprobar que existe una conexion directa activa
    boolean existe = false;

    ListaEnlazada.NodoLista<Conexion> actual =
            conexiones.getCabeza();

    while (actual != null) {

        Conexion conexion = actual.dato;

        if (conexion.isActiva()
                && conexion.getOrigen().equals(origen)
                && conexion.getDestino().equals(destino)) {
            existe = true;
            break;
        }

        actual = actual.siguiente;
    }

    if (!existe) {
        return false;
    }

    // Actualizar ambos sentidos de la conexion
    actual = conexiones.getCabeza();

    while (actual != null) {

        Conexion conexion = actual.dato;

        boolean directo =
                conexion.getOrigen().equals(origen)
                && conexion.getDestino().equals(destino);

        boolean inverso =
                conexion.getOrigen().equals(destino)
                && conexion.getDestino().equals(origen);

        if (conexion.isActiva() && (directo || inverso)) {
            conexion.setTiempoMinutos(nuevoTiempo);
        }

        actual = actual.siguiente;
    }

    return true;
    }

    /**
     * Crea una conexi&oacute;n bidireccional ponderada entre dos estaciones existentes.
     *
     * @param idOrigen identificador de la primera estaci&oacute;n
     * @param idDestino identificador de la segunda estaci&oacute;n
     * @param tiempo duraci&oacute;n del traslado entre ambas estaciones, en minutos
     */
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
    
    /**
     * Desactiva l&oacute;gicamente una estaci&oacute;n y la remueve tanto de la tabla hash
     * como de la lista activa del grafo para simular cierres operativos.
     *
     * @param idEstacion identificador de la estaci&oacute;n a eliminar
     * @return true si la estaci&oacute;n exist&iacute;a y fue eliminada, false si no se encontr&oacute;
     */
    public boolean eliminarEstacion(String idEstacion) {
        Estacion e = indexEstaciones.buscar(idEstacion);
        if (e == null) return false;

        e.setActiva(false); // Desactivar lógica
        indexEstaciones.eliminar(idEstacion);
        estaciones.eliminar(e);
        return true;
    }
    //Consulta de adyacencia y estructura
    
    /**
     * Obtiene las conexiones salientes activas hacia estaciones vecinas operativas.
     *
     * @param e estaci&oacute;n de referencia cuyos vecinos se desean consultar
     * @return lista enlazada con las conexiones adyacentes v&aacute;lidas
     */
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

    /**
     * Obtiene la tabla hash utilizada para indexar las estaciones por identificador.
     *
     * @return &iacute;ndice hash de estaciones
     */
    public TablaHash getIndexEstaciones() { return indexEstaciones; }
    
    /**
     * Obtiene la lista enlazada de estaciones registradas en el grafo.
     *
     * @return lista de estaciones del grafo
     */
    public ListaEnlazada<Estacion> getEstaciones() { return estaciones; }
    
    /**
     * Obtiene la lista enlazada con las conexiones dirigidas almacenadas en el grafo.
     *
     * @return lista de conexiones del grafo
     */
    public ListaEnlazada<Conexion> getConexiones() { return conexiones; }
}