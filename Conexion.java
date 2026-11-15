
/*
 * Proyecto UrbanRoute - Red de transporte.
 * Modelo de datos para los tramos o conexiones entre estaciones.
 */
package modelo;

/**
 * Representa una conexi&oacute;n o arista ponderada entre dos estaciones de la red.
 * Modela el tiempo estimado de traslado en minutos y su disponibilidad operativa.
 */
public class Conexion {
    private Estacion origen;
    private Estacion destino;
    private int tiempoMinutos;
    private boolean activa;

    
    /**
     * Crea una conexi&oacute;n activa entre una estaci&oacute;n de origen y una de destino.
     *
     * @param origen estaci&oacute;n inicial del tramo
     * @param destino estaci&oacute;n final del tramo
     * @param tiempoMinutos tiempo estimado de traslado en minutos
     */
    public Conexion(Estacion origen, Estacion destino, int tiempoMinutos) {
        this.origen = origen;
        this.destino = destino;
        this.tiempoMinutos = tiempoMinutos;
        this.activa = true;
    }
    // Getters y Setters
    
    /**
     * Obtiene la estaci&oacute;n de origen del tramo.
     *
     * @return estaci&oacute;n inicial de la conexi&oacute;n
     */
    public Estacion getOrigen() { return origen; }
    
    /**
     * Obtiene la estaci&oacute;n de destino del tramo.
     *
     * @return estaci&oacute;n final de la conexi&oacute;n
     */
    public Estacion getDestino() { return destino; }
    
    /**
     * Obtiene el peso de la arista expresado en minutos de traslado.
     *
     * @return tiempo de traslado en minutos
     */
    public int getTiempoMinutos() { return tiempoMinutos; }
    
    /**
     * Indica si la conexi&oacute;n est&aacute; habilitada para los recorridos de la red.
     *
     * @return true si la conexi&oacute;n se encuentra activa
     */
    public boolean isActiva() { return activa; }
    
    /**
     * Cambia el estado operativo de la conexi&oacute;n.
     *
     * @param activa nuevo estado l&oacute;gico del tramo
     */
    public void setActiva(boolean activa) { this.activa = activa; }
    
    /**
     * Actualiza el tiempo de traslado del tramo validando que sea positivo.
     *
     * @param tiempoMinutos nuevo tiempo de traslado en minutos, mayor que cero
     * @throws IllegalArgumentException si el tiempo indicado es menor o igual a cero
     */
    public void setTiempoMinutos(int tiempoMinutos) {
    if (tiempoMinutos <= 0) {
        throw new IllegalArgumentException(
                "El tiempo debe ser mayor que cero."
        );
    }

    this.tiempoMinutos = tiempoMinutos;
    }
}