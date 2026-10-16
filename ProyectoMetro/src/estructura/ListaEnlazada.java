package estructura;

/**
 * Lista simplemente enlazada propia para reemplazar ArrayList o LinkedList.
 */
public class ListaEnlazada<T> {
    public static class NodoLista<T> {
        public T dato;
        public NodoLista<T> siguiente;

        public NodoLista(T dato) {
            this.dato = dato;
            this.siguiente = null;
        }
    }

    private NodoLista<T> cabeza;
    private int tamaño;

    public ListaEnlazada() {
        this.cabeza = null;
        this.tamaño = 0;
    }

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

    public int getTamaño() { return tamaño; }
    public NodoLista<T> getCabeza() { return cabeza; }
    public boolean estaVacia() { return tamaño == 0; }
}