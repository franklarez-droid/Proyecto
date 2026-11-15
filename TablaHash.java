/*
 * Proyecto UrbanRoute - Red de transporte.
 * Estructura de dispersi&oacute;n propia para indexaci&oacute;n de estaciones en O(1).
 */
package estructura;

import modelo.Estacion;

/**
 * Implementa una tabla hash propia con resoluci&oacute;n de colisiones mediante
 * encadenamiento separado (listas enlazadas por cubeta) para indexar y
 * recuperar objetos Estacion por su identificador en tiempo promedio O(1).
 */
public class TablaHash {
    
    /**
     * Nodo interno de encadenamiento para almacenar pares clave-valor en cada cubeta.
     */
    private static class NodoHash {
        String clave;
        Estacion valor;
        NodoHash siguiente;
            
        /**
         * Crea un nodo asociando el identificador de estaci&oacute;n con su instancia.
         *
         * @param clave identificador &uacute;nico de la estaci&oacute;n
         * @param valor objeto estaci&oacute;n correspondiente a la clave
         */
        NodoHash(String clave, Estacion valor) {
            this.clave = clave;
            this.valor = valor;
            this.siguiente = null;
        }
    }

    private NodoHash[] tabla;
    private int capacidad;
    private int tamaño;

    /**
     * Inicializa la tabla hash con una capacidad espec&iacute;fica de cubetas.
     *
     * @param capacidad n&uacute;mero de posiciones del arreglo interno
     */
    public TablaHash(int capacidad) {
        this.capacidad = capacidad;
        this.tabla = new NodoHash[capacidad];
        this.tamaño = 0;
    }

    /**
     * Inicializa la tabla hash con una capacidad prima predeterminada de 101 cubetas
     * para reducir la probabilidad de colisiones.
     */
    public TablaHash() {
        this(101); // Tamaño primo inicial
    }
    
    /**
     * Calcula el &iacute;ndice de cubeta mediante dispersi&oacute;n polin&oacute;mica (base 31)
     * sobre los caracteres del identificador.
     *
     * @param clave identificador de la estaci&oacute;n a dispersar
     * @return &iacute;ndice v&aacute;lido dentro del arreglo interno
     */
    private int funcionHash(String clave) {
        int hash = 0;
        for (int i = 0; i < clave.length(); i++) {
            hash = (31 * hash + clave.charAt(i)) % capacidad;
        }
        return Math.abs(hash);
    }
    //Operaciones de Dispersión
    
    /**
     * Inserta una estaci&oacute;n en la tabla hash o actualiza su referencia si la clave ya existe.
     *
     * @param clave identificador &uacute;nico de la estaci&oacute;n
     * @param estacion instancia de Estacion que se desea indexar
     */
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

    
    /**
     * Recupera una estaci&oacute;n a partir de su identificador sin distinguir may&uacute;sculas.
     *
     * @param clave identificador de la estaci&oacute;n buscada
     * @return objeto Estacion asociado o null si no se encuentra registrado
     */
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
    
    /**
     * Elimina de la tabla hash el registro asociado al identificador indicado.
     *
     * @param clave identificador de la estaci&oacute;n a remover
     * @return true si el registro fue eliminado, false si la clave no exist&iacute;a
     */
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
    // Consultas de la tabla
    
    /**
     * Obtiene la cantidad de estaciones indexadas actualmente en la tabla.
     *
     * @return n&uacute;mero de registros almacenados
     */
    public int getTamaño() { return tamaño; }
    
    /**
     * Recorre todas las cubetas y devuelve una lista enlazada con las estaciones indexadas.
     *
     * @return lista enlazada con todas las estaciones de la tabla
     */
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