/*
 * Proyecto UrbanRoute - Red de transporte.
 * Estructura de datos din&aacute;mica lineal implementada desde cero.
 */
package estructura;

/**
 * Implementa una lista simplemente enlazada gen&eacute;rica propia para almacenar
 * colecciones de elementos sin recurrir al Java Collections Framework.
 *
 * @param <T> tipo de dato almacenado en los nodos de la lista
 */
public class ListaEnlazada<T> {
    
    /**
     * Representa un nodo individual de la lista simplemente enlazada.
     *
     * @param <T> tipo de elemento contenido en el nodo
     */
    public static class NodoLista<T> {
        public T dato;
        public NodoLista<T> siguiente;
        
        /**
         * Inicializa un nodo con el dato proporcionado y sin enlace siguiente.
         *
         * @param dato elemento a almacenar en el nodo
         */
        public NodoLista(T dato) {
            this.dato = dato;
            this.siguiente = null;
        }
    }

    private NodoLista<T> cabeza;
    private int tamaño;

    /**
     * Crea una lista enlazada vac&iacute;a.
     */
    public ListaEnlazada() {
        this.cabeza = null;
        this.tamaño = 0;
    }
    // Operaciones y modificadores
    
    /**
     * Inserta un nuevo elemento al final de la lista.
     *
     * @param dato elemento que se desea agregar
     */
    public void agregar(T dato) {
        NodoLista<T> nuevo = new NodoLista<>(dato);
        if (cabeza == null) {
            cabeza = nuevo;
        } else {
            NodoLista<T> aux = cabeza;
            while (aux.siguiente != null) {
                aux = aux.siguiente;
            }
            aux.siguiente = nuevo;
        }
        tamaño++;
    }
    
    /**
     * Elimina la primera ocurrencia del elemento indicado dentro de la lista.
     *
     * @param dato elemento que se desea remover
     * @return true si el elemento exist&iacute;a y fue eliminado, false en caso contrario
     */
    public boolean eliminar(T dato) {
        if (cabeza == null) return false;
        if (cabeza.dato.equals(dato)) {
            cabeza = cabeza.siguiente;
            tamaño--;
            return true;
        }
        NodoLista<T> aux = cabeza;
        while (aux.siguiente != null && !aux.siguiente.dato.equals(dato)) {
            aux = aux.siguiente;
        }
        if (aux.siguiente != null) {
            aux.siguiente = aux.siguiente.siguiente;
            tamaño--;
            return true;
        }
        return false;
    }
    //Consulta de la listas
    
    /**
     * Obtiene la cantidad total de elementos almacenados en la lista.
     *
     * @return n&uacute;mero de elementos actuales
     */
    public int getTamaño() { return tamaño; }
    
    /**
     * Devuelve el nodo inicial de la lista para permitir su recorrido secuencial.
     *
     * @return primer nodo de la lista o null si est&aacute; vac&iacute;a
     */
    public NodoLista<T> getCabeza() { return cabeza; }
    
    /**
     * Comprueba si la lista no contiene ning&uacute;n elemento.
     *
     * @return true si la lista est&aacute; vac&iacute;a
     */
    public boolean estaVacia() { return tamaño == 0; }
}