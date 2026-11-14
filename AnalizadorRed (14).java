package controlador;

import estructura.Grafo;
import estructura.ListaEnlazada;
import modelo.Conexion;
import modelo.Estacion;

/**
 * Ejecuta algoritmos de rutas, conectividad y centralidad de la red.
 * Utiliza arreglos y las estructuras propias del proyecto.
 */
public class AnalizadorRed {

    /**
     * Resultado de Dijkstra: camino ordenado y duración total.
     */
    public static class ResultadoRuta {
        private final ListaEnlazada<Estacion> estaciones;
        private final long tiempoTotal;

        /**
         * @param estaciones estaciones desde origen hasta destino
         * @param tiempoTotal duración mínima del trayecto, en minutos
         */
        public ResultadoRuta(ListaEnlazada<Estacion> estaciones, long tiempoTotal) {
            this.estaciones = estaciones;
            this.tiempoTotal = tiempoTotal;
        }

        /** @return estaciones de origen a destino */
        public ListaEnlazada<Estacion> getEstaciones() {
            return estaciones;
        }

        /** @return tiempo total, en minutos */
        public long getTiempoTotal() {
            return tiempoTotal;
        }
    }

    /**
     * Calcula la ruta más rápida mediante Dijkstra sin Java Collections.
     * Se ignoran estaciones y conexiones inactivas y pesos no positivos.
     *
     * @param grafo red actual
     * @param idOrigen identificador de la estación inicial
     * @param idDestino identificador de la estación final
     * @return ruta y duración o null si no hay camino válido
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
     * Versión compatible con el nombre original del proyecto.
     *
     * @return estaciones ordenadas de origen a destino, o null sin ruta
     */
    public static ListaEnlazada<Estacion> calcularRutaOptima(
            Grafo grafo, String idOrigen, String idDestino) {
        ResultadoRuta resultado = calcularRutaDetallada(grafo, idOrigen, idDestino);
        return resultado == null ? null : resultado.getEstaciones();
    }

    // DETECCION DE SUBREDES (DFS)

    /**
     * Cuenta las subredes independientes utilizando DFS.
     * Cada estacion activa se cuenta dentro de una sola subred.
     *
     * @param grafo red de transporte
     * @return cantidad de subredes encontradas
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
     * Recorre las estaciones conectadas a una estacion y marca las visitadas.
     *
     * @param grafo red de transporte
     * @param actual estacion desde la que continua el recorrido
     * @param nodos arreglo de estaciones
     * @param visitados indica que estaciones ya se visitaron
     * @param n cantidad de estaciones del arreglo
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

    /**
     * Obtiene un texto con las estaciones que pertenecen a cada subred.
     * Utiliza DFS y no modifica las estaciones ni las conexiones del grafo.
     *
     * @param grafo red de transporte
     * @return texto con las estaciones agrupadas por subred
     */
    public static String obtenerDetalleSubredes(Grafo grafo) {
        if (grafo == null) {
            return "No hay estaciones en la red.";
        }

        ListaEnlazada<Estacion> estaciones = grafo.getEstaciones();
        int n = estaciones.getTamaño();
        if (n == 0) {
            return "No hay estaciones en la red.";
        }

        Estacion[] nodos = new Estacion[n];
        boolean[] visitados = new boolean[n];

        // Copiar las estaciones de la lista enlazada a un arreglo.
        ListaEnlazada.NodoLista<Estacion> aux = estaciones.getCabeza();
        int i = 0;
        while (aux != null) {
            nodos[i] = aux.dato;
            i++;
            aux = aux.siguiente;
        }

        StringBuilder resultado = new StringBuilder();
        int componentes = 0;

        // Iniciar un DFS por cada grupo de estaciones no visitado.
        for (int j = 0; j < n; j++) {
            if (!visitados[j] && nodos[j].isActiva()) {
                componentes++;
                resultado.append("Subred ").append(componentes).append(":\n");

                dfsDetalle(grafo, nodos[j], nodos, visitados, n, resultado);
                resultado.append("\n");
            }
        }

        if (componentes == 0) {
            return "No hay estaciones activas en la red.";
        }
        return resultado.toString();
    }

    /**
     * Recorre las estaciones de una subred mediante DFS y agrega sus datos
     * al texto de resultados.
     *
     * @param grafo red de transporte
     * @param actual estacion que se esta recorriendo
     * @param nodos arreglo de estaciones
     * @param visitados indica que estaciones ya se visitaron
     * @param n cantidad de estaciones del arreglo
     * @param resultado texto que contiene las estaciones de la subred
     */
    private static void dfsDetalle(Grafo grafo, Estacion actual,
            Estacion[] nodos, boolean[] visitados, int n,
            StringBuilder resultado) {

        int indice = -1;
        for (int i = 0; i < n; i++) {
            if (nodos[i].equals(actual)) {
                indice = i;
                break;
            }
        }

        // Detener el recorrido si la estacion no existe o ya fue visitada.
        if (indice == -1 || visitados[indice] || !actual.isActiva()) {
            return;
        }

        visitados[indice] = true;
        resultado.append(" - ").append(actual.getId())
                .append(" - ").append(actual.getNombre()).append("\n");

        ListaEnlazada<Conexion> adyacentes = grafo.obtenerAdyacentes(actual);
        ListaEnlazada.NodoLista<Conexion> aux = adyacentes.getCabeza();

        while (aux != null) {
            Estacion destino = aux.dato.getDestino();
            dfsDetalle(grafo, destino, nodos, visitados, n, resultado);
            aux = aux.siguiente;
        }
    }

    // IDENTIFICACION DE HUBS (CENTRALIDAD DE GRADO)

    /**
     * Obtiene la estacion activa con mayor cantidad de conexiones directas.
     * Si hay empate, devuelve la primera encontrada.
     *
     * @param grafo red de transporte
     * @return estacion con mayor grado, o null si no hay estaciones activas
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