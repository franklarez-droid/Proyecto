
/*
 * Proyecto UrbanRoute - Red de transporte.
 * Modelo de datos para las estaciones de la red multimodal.
 */
package modelo;

/**
 * Representa una estaci&oacute;n o nodo dentro de la red de transporte multimodal.
 * Almacena su identificaci&oacute;n, l&iacute;nea de operaci&oacute;n, ubicaci&oacute;n por zona,
 * capacidad horaria de pasajeros y estado l&oacute;gico en la red.
 */
public class Estacion {
    private String id;
    private String nombre;
    private String linea;
    private String zona;
    private int capacidadPasajeros;
    private boolean activa;

    
    /**
     * Inicializa una estaci&oacute;n activa con sus atributos operativos.
     *
     * @param id identificador &uacute;nico de la estaci&oacute;n
     * @param nombre nombre descriptivo de la parada o estaci&oacute;n
     * @param linea l&iacute;nea de transporte a la que pertenece
     * @param zona sector o zona urbana donde se ubica
     * @param capacidadPasajeros capacidad m&aacute;xima de atenci&oacute;n en pasajeros por hora
     */
    public Estacion(String id, String nombre, String linea, String zona, int capacidadPasajeros) {
        this.id = id;
        this.nombre = nombre;
        this.linea = linea;
        this.zona = zona;
        this.capacidadPasajeros = capacidadPasajeros;
        this.activa = true;
    }

    // Getters y Setters
    
    /**
     * Obtiene el identificador &uacute;nico de la estaci&oacute;n.
     *
     * @return identificador de la estaci&oacute;n
     */
    public String getId() { return id; }
    
    /**
     * Obtiene el nombre descriptivo de la estaci&oacute;n.
     *
     * @return nombre de la estaci&oacute;n
     */
    public String getNombre() { return nombre; }
    
    /**
     * Obtiene el nombre de la l&iacute;nea de transporte asociada.
     *
     * @return nombre de la l&iacute;nea
     */
    public String getLinea() { return linea; }
    
    /**
     * Obtiene la zona geogr&aacute;fica donde se encuentra la estaci&oacute;n.
     *
     * @return zona de la estaci&oacute;n
     */
    public String getZona() { return zona; }
    
    /**
     * Obtiene la capacidad m&aacute;xima de pasajeros por hora de la estaci&oacute;n.
     *
     * @return capacidad en pasajeros por hora
     */
    public int getCapacidadPasajeros() { return capacidadPasajeros; }
    
    /**
     * Indica si la estaci&oacute;n se encuentra operativa dentro de la red.
     *
     * @return true si la estaci&oacute;n est&aacute; activa, false si fue cerrada o eliminada
     */
    public boolean isActiva() { return activa; }
    
    /**
     * Modifica el estado operativo de la estaci&oacute;n para simular aperturas o cierres.
     *
     * @param activa nuevo estado l&oacute;gico de la estaci&oacute;n
     */
    public void setActiva(boolean activa) { this.activa = activa; }

    
    /**
     * Compara dos estaciones a partir de su identificador sin distinguir may&uacute;sculas.
     *
     * @param obj objeto a comparar con la estaci&oacute;n actual
     * @return true si ambos objetos corresponden al mismo identificador de estaci&oacute;n
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Estacion e = (Estacion) obj;
        return id.equalsIgnoreCase(e.id);
    }
}