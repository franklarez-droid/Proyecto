package controlador;

import estructura.Grafo;
import estructura.ListaEnlazada;
import modelo.Conexion;
import modelo.Estacion;

/**
 * Controlador de algoritmos para análisis de la red[span_24](start_span)[span_24](end_span).
 */
public class AnalizadorRed {

    // 1. RUTA DE MENOR TIEMPO (DIJKSTRA)[span_25](start_span)[span_25](end_span)[span_26](start_span)[span_26](end_span)
    public static ListaEnlazada<Estacion> calcularRutaOptima(Grafo grafo, String idOrigen, String idDestino) {
        Estacion inicio = grafo.getIndexEstaciones().buscar(idOrigen);
        Estacion fin = grafo.getIndexEstaciones().buscar(idDestino);

        if (inicio == null || fin == null || !inicio.isActiva() || !fin.isActiva()) {
            return null;
        }

        ListaEnlazada<Estacion> todas = grafo.getEstaciones();
        int n = todas.getTamaño();

        Estacion[] nodos = new Estacion[n];
        int[] distancias = new int[n];
        boolean[] visitados = new boolean[n];
        Estacion[] previo = new Estacion[n];

        ListaEnlazada.NodoLista<Estacion> aux = todas.getCabeza();
        int idx = 0;
        int idxInicio = -1;

        while (aux != null) {
            nodos[idx] = aux.dato;
            distancias[idx] = Integer.MAX_VALUE;
            visitados[idx] = false;
            previo[idx] = null;
            if (aux.dato.equals(inicio)) idxInicio = idx;
            idx++;
            aux = aux.siguiente;
        }

        if (idxInicio == -1) return null;
        distancias[idxInicio] = 0;

        for (int i = 0; i < n; i++) {
            int u = -1;
            int minDist = Integer.MAX_VALUE;
            for (int j = 0; j < n; j++) {
                if (!visitados[j] && distancias[j] < minDist) {
                    minDist = distancias[j];
                    u = j;
                }
            }

            if (u == -1 || distancias[u] == Integer.MAX_VALUE) break;
            visitados[u] = true;

            if (nodos[u].equals(fin)) break;

            ListaEnlazada<Conexion> ady = grafo.obtenerAdyacentes(nodos[u]);
            ListaEnlazada.NodoLista<Conexion> cAux = ady.getCabeza();

            while (cAux != null) {
                Conexion c = cAux.dato;
                Estacion vEst = c.getDestino();
                int vIdx = -1;
                for (int k = 0; k < n; k++) {
                    if (nodos[k].equals(vEst)) { vIdx = k; break; }
                }

                if (vIdx != -1 && !visitados[vIdx]) {
                    int alt = distancias[u] + c.getTiempoMinutos();
                    if (alt < distancias[vIdx]) {
                        distancias[vIdx] = alt;
                        previo[vIdx] = nodos[u];
                    }
                }
                cAux = cAux.siguiente;
            }
        }

        // Reconstruir camino
        ListaEnlazada<Estacion> caminoInvertido = new ListaEnlazada<>();
        Estacion curr = fin;
        while (curr != null) {
            caminoInvertido.agregar(curr);
            int cIdx = -1;
            for (int k = 0; k < n; k++) {
                if (nodos[k].equals(curr)) { cIdx = k; break; }
            }
            if (cIdx == -1) break;
            curr = previo[cIdx];
        }

        return caminoInvertido;
    }

    // 2. DETECCIÓN DE SUBREDES (BFS/DFS)[span_27](start_span)[span_27](end_span)[span_28](start_span)[span_28](end_span)
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

    // 3. IDENTIFICACIÓN DE HUBS (CENTRALIDAD DE GRADO)[span_29](start_span)[span_29](end_span)[span_30](start_span)[span_30](end_span)
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