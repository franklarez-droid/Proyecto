package controlador;

import estructura.Grafo;
import estructura.ListaEnlazada;
import modelo.Conexion;
import modelo.Estacion;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Lee y guarda los datos de la red de transporte en archivos CSV.
 * Acepta archivos de estaciones, archivos de conexiones y archivos
 * que contienen ambas secciones.
 */
public class GestorArchivo {

    /**
     * Carga un CSV en el grafo recibido.
     *
     * @param archivo archivo que se desea leer
     * @param grafo grafo donde se registraran los datos
     * @return true si el archivo se leyo correctamente
     */
    public static boolean cargarDesdeCSV(File archivo, Grafo grafo) {
        try {
            cargarDesdeCSVConErrores(archivo, grafo);
            return true;
        } catch (IOException | IllegalArgumentException ex) {
            return false;
        }
    }

    /**
     * Lee estaciones y conexiones. Si hay una fila incorrecta,
     * informa el numero de linea para que la interfaz muestre el error.
     * Se debe llamar con un grafo nuevo para evitar cargas parciales.
     *
     * @param archivo archivo CSV que se va a leer
     * @param grafo grafo donde se agregaran estaciones y conexiones
     * @throws IOException si ocurre un problema al leer el archivo
     * @throws IllegalArgumentException si alguna fila es incorrecta
     */
    public static void cargarDesdeCSVConErrores(File archivo, Grafo grafo)
            throws IOException {
        if (archivo == null || grafo == null || !archivo.isFile()) {
            throw new IOException("No se pudo abrir el archivo seleccionado.");
        }

        try (BufferedReader lector = Files.newBufferedReader(
                archivo.toPath(), StandardCharsets.UTF_8)) {
            String linea;
            int numero = 0;
            while ((linea = lector.readLine()) != null) {
                numero++;
                linea = linea.replace("\uFEFF", "").trim();
                if (linea.isEmpty()) {
                    continue;
                }

                String separador = linea.contains(";") ? ";" : ",";
                String[] campos = linea.split(separador, -1);
                String primero = campos[0].trim();

                if (primero.equalsIgnoreCase("ID_Estacion")
                        || primero.equalsIgnoreCase("Estacion_Origen")) {
                    continue;
                }

                try {
                    if (campos.length == 5) {
                        String id = campos[0].trim();
                        String nombre = campos[1].trim();
                        String nombreLinea = campos[2].trim();
                        String zona = campos[3].trim();
                        int capacidad = Integer.parseInt(campos[4].trim());

                        if (id.isEmpty() || nombre.isEmpty() || nombreLinea.isEmpty()
                                || zona.isEmpty() || capacidad <= 0) {
                            throw new IllegalArgumentException("Datos de estacion incompletos.");
                        }
                        if (grafo.getIndexEstaciones().buscar(id) != null) {
                            throw new IllegalArgumentException("ID de estacion repetido: " + id);
                        }
                        grafo.agregarEstacion(new Estacion(
                                id, nombre, nombreLinea, zona, capacidad));
                    } else if (campos.length == 3) {
                        String origen = campos[0].trim();
                        String destino = campos[1].trim();
                        int tiempo = Integer.parseInt(campos[2].trim());
                        if (origen.isEmpty() || destino.isEmpty() || tiempo <= 0
                                || origen.equalsIgnoreCase(destino)) {
                            throw new IllegalArgumentException("Conexion invalida.");
                        }

                        // Se conservan los valores predeterminados usados
                        // en el proyecto para un CSV solo de conexiones.
                        if (grafo.getIndexEstaciones().buscar(origen) == null) {
                            grafo.agregarEstacion(new Estacion(
                                    origen, origen, "Línea Generica", "General", 10000));
                        }
                        if (grafo.getIndexEstaciones().buscar(destino) == null) {
                            grafo.agregarEstacion(new Estacion(
                                    destino, destino, "Línea Generica", "General", 10000));
                        }
                        grafo.agregarConexion(origen, destino, tiempo);
                    } else {
                        throw new IllegalArgumentException("Cantidad incorrecta de columnas.");
                    }
                } catch (NumberFormatException ex) {
                    throw new IOException("Numero invalido en la linea " + numero + ".", ex);
                } catch (IllegalArgumentException ex) {
                    throw new IOException("Error en la linea " + numero + ": "
                            + ex.getMessage(), ex);
                }
            }
        }
    }

    /**
     * Guarda las estaciones activas y sus conexiones en un unico CSV.
     *
     * @param archivo destino del CSV
     * @param grafo red que se desea guardar
     * @return true si se guardo correctamente
     */
    public static boolean guardarEnCSV(File archivo, Grafo grafo) {
        try {
            guardarEnCSVConErrores(archivo, grafo);
            return true;
        } catch (IOException | IllegalArgumentException ex) {
            return false;
        }
    }

    /**
     * Guarda la red en un archivo temporal y lo reemplaza al terminar.
     * Asi se evita dejar el archivo original a medio escribir si falla.
     *
     * @param archivo archivo de destino
     * @param grafo red que se desea guardar
     * @throws IOException si no puede escribirse el archivo
     */
    public static void guardarEnCSVConErrores(File archivo, Grafo grafo)
            throws IOException {
        if (archivo == null || grafo == null) {
            throw new IllegalArgumentException("Archivo o grafo no valido.");
        }

        Path destino = archivo.toPath().toAbsolutePath();
        Path carpeta = destino.getParent();
        Path temporal = Files.createTempFile(carpeta, "urbanroute-", ".tmp");
        try {
            try (BufferedWriter escritor = Files.newBufferedWriter(
                    temporal, StandardCharsets.UTF_8)) {
                escritor.write("ID_Estacion,Nombre,Linea,Zona,Capacidad_Pasajeros_Hora");
                escritor.newLine();

                ListaEnlazada.NodoLista<Estacion> estacionActual =
                        grafo.getEstaciones().getCabeza();
                while (estacionActual != null) {
                    Estacion estacion = estacionActual.dato;
                    if (estacion.isActiva()) {
                        // Los campos del formulario no pueden tener comas ni saltos de linea.
                        escritor.write(estacion.getId() + "," + estacion.getNombre()
                                + "," + estacion.getLinea() + "," + estacion.getZona()
                                + "," + estacion.getCapacidadPasajeros());
                        escritor.newLine();
                    }
                    estacionActual = estacionActual.siguiente;
                }

                escritor.newLine();
                escritor.write("Estacion_Origen,Estacion_Destino,Tiempo_Minutos");
                escritor.newLine();

                ListaEnlazada.NodoLista<Conexion> conexionActual =
                        grafo.getConexiones().getCabeza();
                while (conexionActual != null) {
                    Conexion conexion = conexionActual.dato;
                    if (conexion.isActiva()
                            && conexion.getOrigen().isActiva()
                            && conexion.getDestino().isActiva()) {
                        String origen = conexion.getOrigen().getId();
                        String destinoId = conexion.getDestino().getId();
                        // En el grafo no dirigido, cada enlace se guarda una sola vez.
                        if (origen.compareToIgnoreCase(destinoId) < 0) {
                            escritor.write(origen + "," + destinoId + ","
                                    + conexion.getTiempoMinutos());
                            escritor.newLine();
                        }
                    }
                    conexionActual = conexionActual.siguiente;
                }
            }
            try {
                Files.move(temporal, destino, StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException ex) {
                Files.move(temporal, destino, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporal);
        }
    }
}
