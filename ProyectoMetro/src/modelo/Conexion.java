package modelo;

/**
 * Representa la conexion / arista entre dos estaciones[span_5](start_span)[span_5](end_span).
 */
public class Conexion {
    private Estacion origen;
    private Estacion destino;
    private int tiempoMinutos;
    private boolean activa;

    public Conexion(Estacion origen, Estacion destino, int tiempoMinutos) {
        this.origen = origen;
        this.destino = destino;
        this.tiempoMinutos = tiempoMinutos;
        this.activa = true;
    }

    public Estacion getOrigen() { return origen; }
    public Estacion getDestino() { return destino; }
    public int getTiempoMinutos() { return tiempoMinutos; }
    public boolean isActiva() { return activa; }
    public void setActiva(boolean activa) { this.activa = activa; }
}