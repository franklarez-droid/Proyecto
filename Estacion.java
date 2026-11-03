package modelo;

/**
 * Representa una estación / nodo dentro de la red multimodal[span_4](start_span)[span_4](end_span).
 */
public class Estacion {
    private String id;
    private String nombre;
    private String linea;
    private String zona;
    private int capacidadPasajeros;
    private boolean activa;

    public Estacion(String id, String nombre, String linea, String zona, int capacidadPasajeros) {
        this.id = id;
        this.nombre = nombre;
        this.linea = linea;
        this.zona = zona;
        this.capacidadPasajeros = capacidadPasajeros;
        this.activa = true;
    }

    // Getters y Setters
    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public String getLinea() { return linea; }
    public String getZona() { return zona; }
    public int getCapacidadPasajeros() { return capacidadPasajeros; }
    public boolean isActiva() { return activa; }
    public void setActiva(boolean activa) { this.activa = activa; }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Estacion e = (Estacion) obj;
        return id.equalsIgnoreCase(e.id);
    }
}