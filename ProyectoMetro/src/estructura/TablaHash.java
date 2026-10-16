package estructura;

import modelo.Estacion;

/**
 * Tabla Hash propia para indexar objetos Estación en O(1)[span_10](start_span)[span_10](end_span)[span_11](start_span)[span_11](end_span).
 */
public class TablaHash {
    private static class NodoHash {
        String clave;
        Estacion valor;
        NodoHash siguiente;

        NodoHash(String clave, Estacion valor) {
            this.clave = clave;
            this.valor = valor;
            this.siguiente = null;
        }
    }

    private NodoHash[] tabla;
    private int capacidad;
    private int tamaño;

    public TablaHash(int capacidad) {
        this.capacidad = capacidad;
        this.tabla = new NodoHash[capacidad];
        this.tamaño = 0;
    }

    public TablaHash() {
        this(101); // Tamaño primo inicial
    }

    private int funcionHash(String clave) {
        int hash = 0;
        for (int i = 0; i < clave.length(); i++) {
            hash = (31 * hash + clave.charAt(i)) % capacidad;
        }
        return Math.abs(hash);
    }

    public void insertar(String clave, Estacion estacion) {
        int indice = funcionHash(clave);
        NodoHash actual = tabla[indice];

        while (actual != null) {
            if (actual.clave.equalsIgnoreCase(clave)) {
                actual.valor = estacion; // Actualiza si ya existe
                return;
            }
            actual = actual.siguiente;
        }

        NodoHash nuevo = new NodoHash(clave, estacion);
        nuevo.siguiente = tabla[indice];
        tabla[indice] = nuevo;
        tamaño++;
    }

    public Estacion buscar(String clave) {
        if (clave == null) return null;
        int indice = funcionHash(clave);
        NodoHash actual = tabla[indice];

        while (actual != null) {
            if (actual.clave.equalsIgnoreCase(clave)) {
                return actual.valor;
            }
            actual = actual.siguiente;
        }
        return null;
    }

    public boolean eliminar(String clave) {
        int indice = funcionHash(clave);
        NodoHash actual = tabla[indice];
        NodoHash previo = null;

        while (actual != null) {
            if (actual.clave.equalsIgnoreCase(clave)) {
                if (previo == null) {
                    tabla[indice] = actual.siguiente;
                } else {
                    previo.siguiente = actual.siguiente;
                }
                tamaño--;
                return true;
            }
            previo = actual;
            actual = actual.siguiente;
        }
        return false;
    }

    public int getTamaño() { return tamaño; }
    
    public ListaEnlazada<Estacion> obtenerTodas() {
        ListaEnlazada<Estacion> lista = new ListaEnlazada<>();
        for (int i = 0; i < capacidad; i++) {
            NodoHash actual = tabla[i];
            while (actual != null) {
                lista.agregar(actual.valor);
                actual = actual.siguiente;
            }
        }
        return lista;
    }
}