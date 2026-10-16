package controlador;

import estructura.Grafo;
import modelo.Estacion;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class GestorArchivo {

    public static boolean cargarDesdeCSV(File archivo, Grafo grafo) {
        if (archivo == null || !archivo.exists()) {
            System.err.println("El archivo no existe o la ruta es inválida.");
            return false;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            int numeroLinea = 0;

            while ((linea = br.readLine()) != null) {
                numeroLinea++;
                linea = linea.trim();

                // Ignora líneas vacías
                if (linea.isEmpty()) continue;

                // Detecta automáticamente si el separador es coma (,) o punto y coma (;)
                String separador = linea.contains(";") ? ";" : ",";
                String[] datos = linea.split(separador);

                // Omitir encabezados conocidos
                String primeraColumna = datos[0].trim().toUpperCase();
                if (primeraColumna.startsWith("ID") || primeraColumna.startsWith("ESTACION")) {
                    continue; // Pasa a la siguiente línea (es la cabecera)
                }

                try {
                    // Caso 1: Archivo de Estaciones (ID, Nombre, Linea, Zona, Capacidad)
                    if (datos.length >= 5) {
                        String id = datos[0].trim();
                        String nombre = datos[1].trim();
                        String lineaMetro = datos[2].trim();
                        String zona = datos[3].trim();
                        int capacidad = Integer.parseInt(datos[4].trim());

                        Estacion estacion = new Estacion(id, nombre, lineaMetro, zona, capacidad);
                        grafo.agregarEstacion(estacion);
                    } 
                    // Caso 2: Archivo de Conexiones (Origen, Destino, Tiempo)
                    else if (datos.length >= 3) {
                        String origen = datos[0].trim();
                        String destino = datos[1].trim();
                        int tiempo = Integer.parseInt(datos[2].trim());

                        // Registrar estaciones implícitas si aún no existen
                        if (grafo.getIndexEstaciones().buscar(origen) == null) {
                            grafo.agregarEstacion(new Estacion(origen, origen, "Línea Generica", "General", 10000));
                        }
                        if (grafo.getIndexEstaciones().buscar(destino) == null) {
                            grafo.agregarEstacion(new Estacion(destino, destino, "Línea Generica", "General", 10000));
                        }

                        grafo.agregarConexion(origen, destino, tiempo);
                    }
                } catch (NumberFormatException nfe) {
                    System.err.println("Error de formato numérico en la línea " + numeroLinea + ": " + linea);
                }
            }
            return true;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}