/*
 * Proyecto UrbanRoute - Red de transporte.
 * Algoritmos de an&aacute;lisis de rutas &oacute;ptimas, conectividad y centralidad.
 */
package controlador;

import estructura.Grafo;
import estructura.ListaEnlazada;
import modelo.Conexion;
import modelo.Estacion;

/**
 * Ejecuta algoritmos de rutas, conectividad y centralidad de la red.
 * Utiliza arreglos y las estructuras propias del proyecto sin Java Collections.
 */
public class AnalizadorRed {

    /**
     * Encapsula el resultado del algoritmo de Dijkstra: secuencia ordenada
     * de estaciones desde el origen hasta el destino y duraci&oacute;n total en minutos.
     */
    public static class ResultadoRuta {
        private final ListaEnlazada<Estacion> estaciones;
        private final long tiempoTotal;

        /**
         * Inicializa el resultado de una b&uacute;squeda de camino m&iacute;nimo.
         *
         * @param estaciones estaciones ordenadas desde el origen hasta el destino
         * @param tiempoTotal duraci&oacute;n m&iacute;nima acumulada del trayecto, en minutos
         */
        public ResultadoRuta(ListaEnlazada<Estacion> estaciones, long tiempoTotal) {
            this.estaciones = estaciones;
            this.tiempoTotal = tiempoTotal;
        }

        /**
         * Obtiene la lista de estaciones que conforman la ruta &oacute;ptima.
         *
         * @return estaciones de origen a destino
         */
        public ListaEnlazada<Estacion> getEstaciones() {
            return estaciones;
        }

        /**
         * Obtiene el tiempo total acumulado del recorrido &oacute;ptimo.
         *
         * @return tiempo total, en minutos
         */
        public long getTiempoTotal() {
            return tiempoTotal;
        }
    }

    // Algoritmo Dilktra
    
    /**
     * Calcula la ruta m&aacute;s r&aacute;pida entre dos estaciones mediante el algoritmo de Dijkstra
     * sin utilizar Java Collections. Ignora estaciones y conexiones inactivas o con
     * pesos no positivos.
     *
     * @param grafo red actual de transporte
     * @param idOrigen identificador de la estaci&oacute;n inicial
     * @param idDestino identificador de la estaci&oacute;n final
     * @return objeto ResultadoRuta con el recorrido y tiempo m&iacute;nimo, o null si no hay camino
     */
    public static ResultadoRuta calcularRutaDetallada(
            Grafo grafo, String idOrigen, String idDestino) {

        if (grafo == null || idOrigen == null || idDestino == null) {
            return null;
        }

        Estacion origen = grafo.getIndexEstaciones().buscar(idOrigen);
        Estacion destino = grafo.getIndexEstaciones().buscar(idDestino);
        if (origen == null || destino == null
                || !origen.isActiva() || !destino.isActiva()) {
            return null;
        }

        int n = grafo.getEstaciones().getTamaño();
        if (n == 0) {
            return null;
        }

        Estacion[] nodos = new Estacion[n];
        long[] distancia = new long[n];
        int[] anterior = new int[n];
        boolean[] visitado = new boolean[n];

        ListaEnlazada.NodoLista<Estacion> actual =
                grafo.getEstaciones().getCabeza();
        int pos = 0;
        int indiceOrigen = -1;
        int indiceDestino = -1;

        while (actual != null) {
            nodos[pos] = actual.dato;
            distancia[pos] = Long.MAX_VALUE;
            anterior[pos] = -1;
            if (nodos[pos].equals(origen)) {
                indiceOrigen = pos;
            }
            if (nodos[pos].equals(destino)) {
                indiceDestino = pos;
            }
            pos++;
            actual = actual.siguiente;
        }

        if (indiceOrigen == -1 || indiceDestino == -1) {
            return null;
        }
        distancia[indiceOrigen] = 0;

        for (int paso = 0; paso < n; paso++) {
            int u = -1;
            for (int i = 0; i < n; i++) {
                if (!visitado[i] && nodos[i].isActiva()
                        && distancia[i] != Long.MAX_VALUE
                        && (u == -1 || distancia[i] < distancia[u])) {
                    u = i;
                }
            }
            if (u == -1) {
                break;
            }
            if (u == indiceDestino) {
                break;
            }
            visitado[u] = true;

            ListaEnlazada.NodoLista<Conexion> arista =
                    grafo.obtenerAdyacentes(nodos[u]).getCabeza();
            while (arista != null) {
                Conexion conexion = arista.dato;
                Estacion vecino = conexion.getDestino();
                int peso = conexion.getTiempoMinutos();

                if (conexion.isActiva() && vecino.isActiva() && peso > 0) {
                    int v = -1;
                    for (int k = 0; k < n; k++) {
                        if (nodos[k].equals(vecino)) {
                            v = k;
                            break;
                        }
                    }
                    if (v != -1 && !visitado[v]
                            && distancia[u] <= Long.MAX_VALUE - peso) {
                        long alternativa = distancia[u] + peso;
                        if (alternativa < distancia[v]) {
                            distancia[v] = alternativa;
                            anterior[v] = u;
                        }
                    }
                }
                arista = arista.siguiente;
            }
        }

        if (distancia[indiceDestino] == Long.MAX_VALUE) {
            return null;
        }

        // Reconstruir desde destino hacia origen en un arreglo auxiliar.
        int[] invertido = new int[n];
        int longitud = 0;
        int indice = indiceDestino;
        while (indice != -1 && longitud < n) {
            invertido[longitud++] = indice;
            indice = anterior[indice];
        }
        if (invertido[longitud - 1] != indiceOrigen) {
            return null;
        }

        ListaEnlazada<Estacion> camino = new ListaEnlazada<>();
        for (int i = longitud - 1; i >= 0; i--) {
            camino.agregar(nodos[invertido[i]]);
        }
        return new ResultadoRuta(camino, distancia[indiceDestino]);
    }

    /**
     * Devuelve &uacute;nicamente la secuencia de estaciones de la ruta &oacute;ptima entre dos nodos.
     *
     * @param grafo red actual de transporte
     * @param idOrigen identificador de la estaci&oacute;n inicial
     * @param idDestino identificador de la estaci&oacute;n final
     * @return estaciones ordenadas de origen a destino, o null si no existe ruta
     */
    public static ListaEnlazada<Estacion> calcularRutaOptima(
            Grafo grafo, String idOrigen, String idDestino) {
        ResultadoRuta resultado = calcularRutaDetallada(grafo, idOrigen, idDestino);
        return resultado == null ? null : resultado.getEstaciones();
    }

    // Detección de subredes
    
    /**
     * Determina la cantidad de subredes o componentes conexos aislados mediante
     * b&uacute;squeda en profundidad (DFS) sobre las estaciones activas.
     *
     * @param grafo red de transporte a analizar
     * @return n&uacute;mero de componentes conexos independientes en la red
     */
    public static int contarComponentesConexos(Grafo grafo) {
        ListaEnlazada<Estacion> estaciones = grafo.getEstaciones();
        int n = estaciones.getTamaño();
        if (n == 0) return 0;

        Estacion[] nodos = new Estacion[n];
        boolean[] visitados = new boolean[n];

        ListaEnlazada.NodoLista<Estacion> aux = estaciones.getCabeza();
        int i = 0;
        while (aux != null) {
            nodos[i] = aux.dato;
            visitados[i] = false;
            i++;
            aux = aux.siguiente;
        }

        int componentes = 0;
        for (int j = 0; j < n; j++) {
            if (!visitados[j] && nodos[j].isActiva()) {
                componentes++;
                dfsRec(grafo, nodos[j], nodos, visitados, n);
            }
        }
        return componentes;
    }

    /**
     * Explora recursivamente en profundidad todas las estaciones alcanzables
     * dentro de un mismo componente conexo.
     *
     * @param grafo red de transporte actual
     * @param actual estaci&oacute;n visitada en la llamada actual
     * @param nodos arreglo lineal con las estaciones de la red
     * @param visitados arreglo booleano de control de nodos visitados
     * @param n cantidad total de estaciones en el arreglo
     */
    private static void dfsRec(Grafo grafo, Estacion actual, Estacion[] nodos, boolean[] visitados, int n) {
        int idx = -1;
        for (int k = 0; k < n; k++) {
            if (nodos[k].equals(actual)) { idx = k; break; }
        }
        if (idx == -1 || visitados[idx]) return;

        visitados[idx] = true;
        ListaEnlazada<Conexion> ady = grafo.obtenerAdyacentes(actual);
        ListaEnlazada.NodoLista<Conexion> cAux = ady.getCabeza();

        while (cAux != null) {
            Estacion dest = cAux.dato.getDestino();
            dfsRec(grafo, dest, nodos, visitados, n);
            cAux = cAux.siguiente;
        }
    }

    // Identificación de hubs
    
    /**
     * Identifica la estaci&oacute;n neur&aacute;lgica (hub principal) calculando la centralidad
     * de grado, es decir, el nodo activo con mayor n&uacute;mero de conexiones directas.
     *
     * @param grafo red de transporte a evaluar
     * @return estaci&oacute;n activa con mayor grado de conexi&oacute;n, o null si no hay estaciones
     */
    public static Estacion obtenerHubPrincipal(Grafo grafo) {
        ListaEnlazada<Estacion> estaciones = grafo.getEstaciones();
        ListaEnlazada.NodoLista<Estacion> aux = estaciones.getCabeza();

        Estacion hubMax = null;
        int maxGrado = -1;

        while (aux != null) {
            Estacion e = aux.dato;
            if (e.isActiva()) {
                int grado = grafo.obtenerAdyacentes(e).getTamaño();
                if (grado > maxGrado) {
                    maxGrado = grado;
                    hubMax = e;
                }
            }
            aux = aux.siguiente;
        }
        return hubMax;
    }
}