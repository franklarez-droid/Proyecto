/*
 * Proyecto UrbanRoute - Red de transporte.
 * Ventana principal y formularios de edicion.
 */
package vista;

import controlador.AnalizadorRed;
import controlador.GestorArchivo;
import estructura.Grafo;
import estructura.ListaEnlazada;
import modelo.Estacion;
import java.io.File;
import java.io.IOException;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * Ventana principal del sistema de transporte.
 * Permite cargar archivos CSV, modificar estaciones y conexiones,
 * y consultar los resultados de los algoritmos desde la interfaz.
 */
public class VentanaPrincipal extends javax.swing.JFrame {

    private Grafo grafo = new Grafo();
    private VisualizadorGrafo visualizador = new VisualizadorGrafo();
    private File archivoActual;
    private boolean actualizandoLineas = false;

    /**
     * Guarda los datos de una conexion que aun no se ha incorporado al grafo.
     */
    private static class ConexionPendiente {

        String idDestino;
        int tiempo;

        ConexionPendiente(String idDestino, int tiempo) {
            this.idDestino = idDestino;
            this.tiempo = tiempo;
        }
    }

    /** Conexiones que se muestran antes de confirmar una estacion nueva. */
    private ListaEnlazada<ConexionPendiente> conexionesPendientes
            = new ListaEnlazada<>();


    /**
     * Inicializa los componentes de NetBeans y configura las ventanas secundarias.
     */
    public VentanaPrincipal() {
        initComponents();

        editarEliminarEstacion.setDefaultCloseOperation(
                javax.swing.WindowConstants.HIDE_ON_CLOSE);

        algoritmoRuta.setDefaultCloseOperation(
                javax.swing.WindowConstants.HIDE_ON_CLOSE
        );

        panelGrafoLienzo.setLayout(new java.awt.BorderLayout());
        setLocationRelativeTo(null);

        editarAgregarEstacion.setDefaultCloseOperation(
                javax.swing.WindowConstants.HIDE_ON_CLOSE);
    }

    // ===== METODOS DE EDICION - AGREGAR ESTACION =====

    /**
     * Prepara los campos para registrar una estacion y muestra las estaciones disponibles.
     */
    private void prepararFormularioAgregarEstacion() {

        nuevaID.setText("");
        nuevaNombre.setText("");
        nuevaZona.setText("");
        nuevaCapacidad.setText("");
        nuevaTiempo.setText("");

        cargarLineasDisponibles();

        nuevaConexion.removeAllItems();
        nuevaConexion.addItem("Seleccionar estación...");

        ListaEnlazada.NodoLista<Estacion> actual
                = grafo.getEstaciones().getCabeza();

        while (actual != null) {

            Estacion estacion = actual.dato;

            if (estacion.isActiva()) {

                nuevaConexion.addItem(
                        estacion.getId() + " - " + estacion.getNombre()
                );
            }

            actual = actual.siguiente;
        }

        // Informar cuando se crea la primera estación
        if (grafo.getEstaciones().estaVacia()) {

            nuevaConexion.setEnabled(false);
            nuevaTiempo.setEnabled(false);
            nuevaAgregar.setEnabled(false);

            JOptionPane.showMessageDialog(
                    editarAgregarEstacion,
                    "No hay estaciones registradas.\n"
                    + "Puedes crear la primera estación sin conexiones.\n"
                    + "Después podrás agregar otras estaciones y conectarlas.",
                    "Primera estación",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } else {

            nuevaConexion.setEnabled(true);
            nuevaTiempo.setEnabled(true);
            nuevaAgregar.setEnabled(true);
        }

        conexionesPendientes = new ListaEnlazada<>();
        resumenConexiones.setText("");

    }

    /**
     * Carga las lineas de las estaciones activas sin repetirlas.
     * Incluye la opcion de crear una linea nueva.
     */
    private void cargarLineasDisponibles() {

        actualizandoLineas = true;

        try {

            // Limpiar las opciones anteriores
            nuevaLinea.removeAllItems();
            nuevaLinea.addItem("Seleccionar línea...");

            // Obtener las estaciones del grafo
            ListaEnlazada.NodoLista<Estacion> actual
                    = grafo.getEstaciones().getCabeza();

            // Recorrer todas las estaciones
            while (actual != null) {

                Estacion estacion = actual.dato;

                if (estacion.isActiva()) {

                    String linea = estacion.getLinea();

                    // Verificar que la linea tenga un nombre valido
                    if (linea != null && !linea.trim().isEmpty()) {

                        boolean existe = false;

                        // Comprobar que la linea no esté repetida
                        for (int i = 1; i < nuevaLinea.getItemCount(); i++) {

                            String opcion = nuevaLinea.getItemAt(i);

                            if (opcion != null
                                    && opcion.equalsIgnoreCase(linea)) {

                                existe = true;
                                break;
                            }
                        }

                        // Agregar solo las lineas que no existan
                        if (!existe) {
                            nuevaLinea.addItem(linea);
                        }
                    }
                }

                actual = actual.siguiente;
            }

            // Permitir crear una linea nueva
            nuevaLinea.addItem("+ Nueva línea...");

            // Seleccionar la opcion inicial
            nuevaLinea.setSelectedIndex(0);

        } finally {

            // Habilitar nuevamente el procesamiento de eventos
            actualizandoLineas = false;
        }
    }

    /**
     * Muestra en el formulario las conexiones que todavia no se han guardado.
     */
    private void actualizarResumenConexiones() {

        resumenConexiones.setText("");

        ListaEnlazada.NodoLista<ConexionPendiente> actual
                = conexionesPendientes.getCabeza();

        while (actual != null) {

            resumenConexiones.append(
                    actual.dato.idDestino + " - "
                    + actual.dato.tiempo + " minutos\n"
            );

            actual = actual.siguiente;
        }
    }

    // ===== METODOS DE EDICION - MODIFICAR Y ELIMINAR =====

    /**
     * Carga las estaciones activas en los selectores para modificar tiempos.
     */
    private void cargarEstacionesModificarTiempo() {

        modificarTiempoEA.removeAllItems();
        modificarTiempoEB.removeAllItems();

        modificarTiempoEA.addItem("Seleccionar estación...");
        modificarTiempoEB.addItem("Seleccionar estación...");

        ListaEnlazada.NodoLista<Estacion> actual
                = grafo.getEstaciones().getCabeza();

        while (actual != null) {

            Estacion estacion = actual.dato;

            if (estacion.isActiva()) {

                String opcion = estacion.getId()
                        + " - " + estacion.getNombre();

                modificarTiempoEA.addItem(opcion);
                modificarTiempoEB.addItem(opcion);
            }

            actual = actual.siguiente;

        }
    }

    /**
     * Actualiza el selector con las estaciones activas que pueden eliminarse.
     */
    private void cargarEstacionesEliminar() {

        eliminarEstacionCombo.removeAllItems();
        eliminarEstacionCombo.addItem("Seleccionar estación...");

        ListaEnlazada.NodoLista<Estacion> actual
                = grafo.getEstaciones().getCabeza();

        while (actual != null) {

            Estacion estacion = actual.dato;

            if (estacion.isActiva()) {

                eliminarEstacionCombo.addItem(
                        estacion.getId() + " - "
                        + estacion.getNombre()
                );
            }

            actual = actual.siguiente;
        }

        eliminar.setEnabled(
                eliminarEstacionCombo.getItemCount() > 1
        );

        eliminarEstacionCombo.setSelectedIndex(0);
    }

    // ===== METODOS DE ALGORITMOS - RUTA OPTIMA =====

    /**
     * Carga las estaciones para calcular una ruta y limpia el resultado anterior.
     */
    private void prepararFormularioRuta() {

        rutaOrigen.removeAllItems();
        rutaDestino.removeAllItems();

        rutaOrigen.addItem("Seleccionar estación...");
        rutaDestino.addItem("Seleccionar estación...");

        rutaResultado.setText("");

        ListaEnlazada.NodoLista<Estacion> actual
                = grafo.getEstaciones().getCabeza();

        while (actual != null) {

            Estacion estacion = actual.dato;

            if (estacion.isActiva()) {

                String opcion = estacion.getId()
                        + " - " + estacion.getNombre();

                rutaOrigen.addItem(opcion);
                rutaDestino.addItem(opcion);
            }

            actual = actual.siguiente;
        }

        // Desactivar el calculo si no hay suficientes estaciones
        boolean suficientes
                = rutaOrigen.getItemCount() > 2;

        rutaCalcular.setEnabled(suficientes);

        if (!suficientes) {
            rutaResultado.setText(
                    "Se necesitan al menos dos estaciones "
                    + "activas para calcular una ruta."
            );
        }
    }

    /**
     * Obtiene una estacion activa segun la posicion seleccionada en el formulario.
     * 
     * @param indice posicion seleccionada, sin contar el mensaje inicial
     * @return estacion correspondiente o null si no existe
     */
    private Estacion obtenerEstacionRuta(int indice) {

        if (indice <= 0) {
            return null;
        }

        int posicion = 1;

        ListaEnlazada.NodoLista<Estacion> actual
                = grafo.getEstaciones().getCabeza();

        while (actual != null) {

            if (actual.dato.isActiva()) {

                if (posicion == indice) {
                    return actual.dato;
                }

                posicion++;
            }

            actual = actual.siguiente;
        }

        return null;
    }

    /**
     * Redibuja la red y resalta una ruta cuando se proporciona.
     * 
     * @param ruta estaciones del recorrido, o null para mostrar la red normal
     */
    private void actualizarVisualizacionRuta(
            ListaEnlazada<Estacion> ruta) {

        JPanel lienzo = visualizador.renderizarGrafo(
                grafo, ruta
        );

        panelGrafoLienzo.removeAll();

        panelGrafoLienzo.setLayout(
                new java.awt.BorderLayout()
        );

        panelGrafoLienzo.add(
                lienzo,
                java.awt.BorderLayout.CENTER
        );

        panelGrafoLienzo.revalidate();
        panelGrafoLienzo.repaint();
    }

    /**
     * Oculta el formulario de ruta sin cerrar la ventana principal.
     * 
     * @param evt evento del boton Cerrar
     */
    private void rutaCerrarActionPerformed(
            java.awt.event.ActionEvent evt) {

        algoritmoRuta.setVisible(false);
    }

    // ===== METODOS DE ARCHIVOS Y VISUALIZACION =====

    /**
     * Muestra el selector de guardado y escribe la red completa.
     * Si el usuario cancela o se produce un error, devuelve false.
     *
     * @return true cuando se guardo el archivo correctamente
     */
    private boolean guardarRedConSelector() {
        return guardarRedConSelector(null);
    }

    /**
     * Guarda la red y evita sobrescribir el CSV que se va a cargar.
     *
     * @param archivoProhibido archivo que no se debe sobrescribir
     * @return true cuando se guardo correctamente
     */
    private boolean guardarRedConSelector(File archivoProhibido) {
        if (grafo.getEstaciones().estaVacia()) {
            JOptionPane.showMessageDialog(this,
                    "No hay estaciones para guardar.",
                    "Red vacia", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        JFileChooser selector = new JFileChooser();
        selector.setDialogTitle("Guardar red de transporte");
        selector.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Archivos CSV (*.csv)", "csv"));
        if (archivoActual != null) {
            selector.setSelectedFile(archivoActual);
        }
        if (selector.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return false;
        }

        File archivo = selector.getSelectedFile();
        if (!archivo.getName().toLowerCase().endsWith(".csv")) {
            archivo = new File(archivo.getAbsolutePath() + ".csv");
        }
        if (archivoProhibido != null
                && archivo.toPath().toAbsolutePath().normalize().equals(
                        archivoProhibido.toPath().toAbsolutePath().normalize())) {
            JOptionPane.showMessageDialog(this,
                    "Debes elegir otro nombre para el respaldo.\n"
                    + "No puedes sobrescribir el CSV que vas a cargar.",
                    "Archivo de destino", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (archivo.exists()) {
            int confirmar = JOptionPane.showConfirmDialog(this,
                    "El archivo ya existe. ¿Deseas sobrescribirlo?",
                    "Confirmar guardado", JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (confirmar != JOptionPane.YES_OPTION) {
                return false;
            }
        }

        try {
            GestorArchivo.guardarEnCSVConErrores(archivo, grafo);
            archivoActual = archivo;
            JOptionPane.showMessageDialog(this,
                    "La red se guardo correctamente en:\n" + archivo.getAbsolutePath(),
                    "Archivo guardado", JOptionPane.INFORMATION_MESSAGE);
            return true;
        } catch (IOException | IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo guardar la red.\n" + ex.getMessage(),
                    "Error al guardar", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    /**
     * Refresca el panel del grafo con las estaciones y conexiones actuales.
     */
    private void actualizarVistaGrafo() {
        JPanel lienzo = visualizador.renderizarGrafo(grafo);
        panelGrafoLienzo.removeAll();
        panelGrafoLienzo.setLayout(new java.awt.BorderLayout());
        panelGrafoLienzo.add(lienzo, java.awt.BorderLayout.CENTER);
        panelGrafoLienzo.revalidate();
        panelGrafoLienzo.repaint();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        editarAgregarEstacion = new javax.swing.JFrame();
        jPanel2 = new javax.swing.JPanel();
        labelDatos = new javax.swing.JLabel();
        labelID = new javax.swing.JLabel();
        nuevaID = new javax.swing.JTextField();
        labelNombre = new javax.swing.JLabel();
        nuevaNombre = new javax.swing.JTextField();
        jSeparator1 = new javax.swing.JSeparator();
        laberLinea = new javax.swing.JLabel();
        labelZona = new javax.swing.JLabel();
        nuevaLinea = new javax.swing.JComboBox<>();
        nuevaZona = new javax.swing.JTextField();
        labelCapacidad = new javax.swing.JLabel();
        nuevaCapacidad = new javax.swing.JTextField();
        labelConexiones = new javax.swing.JLabel();
        labelEstacion = new javax.swing.JLabel();
        nuevaConexion = new javax.swing.JComboBox<>();
        labelTiempo = new javax.swing.JLabel();
        nuevaTiempo = new javax.swing.JTextField();
        nuevaAgregar = new javax.swing.JButton();
        nuevaCancelar = new javax.swing.JButton();
        nuevaGuardar = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        resumenConexiones = new javax.swing.JTextArea();
        jSeparator3 = new javax.swing.JSeparator();
        editarEliminarEstacion = new javax.swing.JFrame();
        eliminarEstacionCombo = new javax.swing.JComboBox<>();
        eliminar = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        editarModificarTiempoJFrame = new javax.swing.JFrame();
        jPanel1 = new javax.swing.JPanel();
        labelEA = new javax.swing.JLabel();
        labelEB = new javax.swing.JLabel();
        labelT = new javax.swing.JLabel();
        tiempoIn = new javax.swing.JTextField();
        modificarTiempoEA = new javax.swing.JComboBox<>();
        modificarTiempoGuardar = new javax.swing.JButton();
        modificarTiempoEB = new javax.swing.JComboBox<>();
        algoritmoRuta = new javax.swing.JFrame();
        origenLabel = new javax.swing.JLabel();
        rutaOrigen = new javax.swing.JComboBox<>();
        destinoLabel = new javax.swing.JLabel();
        rutaDestino = new javax.swing.JComboBox<>();
        rutaScroll = new javax.swing.JScrollPane();
        rutaResultado = new javax.swing.JTextArea();
        rutaCalcular = new javax.swing.JButton();
        panelGrafoLienzo = new javax.swing.JPanel();
        Menu = new javax.swing.JMenuBar();
        Archivo = new javax.swing.JMenu();
        cargarCSV = new javax.swing.JMenuItem();
        guardarCSV = new javax.swing.JMenuItem();
        Editar = new javax.swing.JMenu();
        botonAgregarEstacion = new javax.swing.JMenuItem();
        botonEliminarEstacion = new javax.swing.JMenuItem();
        modificarTiempo = new javax.swing.JMenuItem();
        Algoritmos = new javax.swing.JMenu();
        calcularRuta = new javax.swing.JMenuItem();
        detectarSubredes = new javax.swing.JMenuItem();
        identificarHubs = new javax.swing.JMenuItem();

        editarAgregarEstacion.setTitle("Nueva estación");

        labelDatos.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        labelDatos.setText("Datos de la estación");

        labelID.setText("ID de estación");

        labelNombre.setText("Nombre");

        laberLinea.setText("Línea");

        labelZona.setText("Zona");

        nuevaLinea.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Linea 1", "Linea 2", "Linea 3", "Linea 4" }));
        nuevaLinea.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nuevaLineaActionPerformed(evt);
            }
        });

        labelCapacidad.setText("Capacidad (pasajeros por hora)");

        labelConexiones.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        labelConexiones.setText("Conexiones de la estación");

        labelEstacion.setText("Estación existente");

        nuevaConexion.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Seleccionar estación..." }));
        nuevaConexion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nuevaConexionActionPerformed(evt);
            }
        });

        labelTiempo.setText("Tiempo de traslado (minutos)");

        nuevaTiempo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nuevaTiempoActionPerformed(evt);
            }
        });

        nuevaAgregar.setText("+Agregar conexión");
        nuevaAgregar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nuevaAgregarActionPerformed(evt);
            }
        });

        nuevaCancelar.setText("Cancelar");
        nuevaCancelar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nuevaCancelarActionPerformed(evt);
            }
        });

        nuevaGuardar.setText("Guardar");
        nuevaGuardar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nuevaGuardarActionPerformed(evt);
            }
        });

        resumenConexiones.setEditable(false);
        resumenConexiones.setColumns(20);
        resumenConexiones.setRows(5);
        jScrollPane1.setViewportView(resumenConexiones);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(labelConexiones, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(nuevaCapacidad)
                            .addComponent(jSeparator1)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel2Layout.createSequentialGroup()
                                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(labelID, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(laberLinea, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addGap(12, 12, 12))
                                    .addGroup(jPanel2Layout.createSequentialGroup()
                                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addComponent(nuevaLinea, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(nuevaID))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(labelNombre)
                                    .addComponent(labelZona)
                                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(nuevaNombre, javax.swing.GroupLayout.DEFAULT_SIZE, 121, Short.MAX_VALUE)
                                        .addComponent(nuevaZona))))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(jSeparator3, javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 245, Short.MAX_VALUE)
                                    .addGroup(jPanel2Layout.createSequentialGroup()
                                        .addComponent(nuevaCancelar)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(nuevaGuardar))
                                    .addComponent(nuevaAgregar, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(nuevaTiempo, javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(labelTiempo, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(nuevaConexion, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(labelDatos, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(labelCapacidad, javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(labelEstacion, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGap(0, 0, Short.MAX_VALUE)))
                        .addContainerGap())))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(labelDatos)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 4, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelID)
                    .addComponent(labelNombre))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(nuevaID, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(nuevaNombre, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(laberLinea)
                    .addComponent(labelZona))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(nuevaZona, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(nuevaLinea, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(labelCapacidad)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(nuevaCapacidad, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(labelConexiones)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 4, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(labelEstacion)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(nuevaConexion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(labelTiempo)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(nuevaTiempo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(nuevaAgregar)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 122, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(nuevaCancelar)
                    .addComponent(nuevaGuardar))
                .addGap(12, 12, 12))
        );

        javax.swing.GroupLayout editarAgregarEstacionLayout = new javax.swing.GroupLayout(editarAgregarEstacion.getContentPane());
        editarAgregarEstacion.getContentPane().setLayout(editarAgregarEstacionLayout);
        editarAgregarEstacionLayout.setHorizontalGroup(
            editarAgregarEstacionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        editarAgregarEstacionLayout.setVerticalGroup(
            editarAgregarEstacionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        eliminarEstacionCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Seleccionar estación..." }));
        eliminarEstacionCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                eliminarEstacionComboActionPerformed(evt);
            }
        });

        eliminar.setText(" Eliminar estación ");
        eliminar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                eliminarActionPerformed(evt);
            }
        });

        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel3.setText("Estación a eliminar:");

        javax.swing.GroupLayout editarEliminarEstacionLayout = new javax.swing.GroupLayout(editarEliminarEstacion.getContentPane());
        editarEliminarEstacion.getContentPane().setLayout(editarEliminarEstacionLayout);
        editarEliminarEstacionLayout.setHorizontalGroup(
            editarEliminarEstacionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(editarEliminarEstacionLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(editarEliminarEstacionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(eliminar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(eliminarEstacionCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        editarEliminarEstacionLayout.setVerticalGroup(
            editarEliminarEstacionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(editarEliminarEstacionLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(editarEliminarEstacionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(eliminarEstacionCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(eliminar)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        editarModificarTiempoJFrame.setTitle("Modificar tiempo");

        labelEA.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labelEA.setText("Estación A:");

        labelEB.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labelEB.setText("Estación B:");

        labelT.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labelT.setText("Tiempo (min.)");

        tiempoIn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tiempoInActionPerformed(evt);
            }
        });

        modificarTiempoEA.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Seleccionar estación..." }));
        modificarTiempoEA.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                modificarTiempoEAActionPerformed(evt);
            }
        });

        modificarTiempoGuardar.setText("Guardar cambios");
        modificarTiempoGuardar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                modificarTiempoGuardarActionPerformed(evt);
            }
        });

        modificarTiempoEB.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Seleccionar estación..." }));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(labelT)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tiempoIn, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(modificarTiempoGuardar, javax.swing.GroupLayout.DEFAULT_SIZE, 152, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(labelEA, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(labelEB, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(modificarTiempoEA, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(modificarTiempoEB, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(modificarTiempoEA, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labelEA, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(modificarTiempoEB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labelEB, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelT)
                    .addComponent(tiempoIn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(modificarTiempoGuardar))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout editarModificarTiempoJFrameLayout = new javax.swing.GroupLayout(editarModificarTiempoJFrame.getContentPane());
        editarModificarTiempoJFrame.getContentPane().setLayout(editarModificarTiempoJFrameLayout);
        editarModificarTiempoJFrameLayout.setHorizontalGroup(
            editarModificarTiempoJFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        editarModificarTiempoJFrameLayout.setVerticalGroup(
            editarModificarTiempoJFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        algoritmoRuta.setTitle("Calcular Ruta");

        origenLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        origenLabel.setText("Estación de origen:");

        rutaOrigen.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Estación A" }));
        rutaOrigen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rutaOrigenActionPerformed(evt);
            }
        });

        destinoLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        destinoLabel.setText("Estación de Destino:");
        destinoLabel.setToolTipText("");

        rutaDestino.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Estación B" }));

        rutaResultado.setEditable(false);
        rutaResultado.setColumns(20);
        rutaResultado.setRows(5);
        rutaScroll.setViewportView(rutaResultado);

        rutaCalcular.setText("Calcular");
        rutaCalcular.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rutaCalcularActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout algoritmoRutaLayout = new javax.swing.GroupLayout(algoritmoRuta.getContentPane());
        algoritmoRuta.getContentPane().setLayout(algoritmoRutaLayout);
        algoritmoRutaLayout.setHorizontalGroup(
            algoritmoRutaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(algoritmoRutaLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(algoritmoRutaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(rutaScroll, javax.swing.GroupLayout.DEFAULT_SIZE, 305, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, algoritmoRutaLayout.createSequentialGroup()
                        .addGroup(algoritmoRutaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(origenLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(destinoLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(algoritmoRutaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(rutaDestino, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(rutaOrigen, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, algoritmoRutaLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(rutaCalcular, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        algoritmoRutaLayout.setVerticalGroup(
            algoritmoRutaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, algoritmoRutaLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(algoritmoRutaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rutaOrigen, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(origenLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(algoritmoRutaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rutaDestino, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(destinoLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(rutaScroll, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(rutaCalcular)
                .addContainerGap())
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("METROMET");
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        panelGrafoLienzo.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Visualización de la red multimodal:", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION));
        panelGrafoLienzo.setToolTipText("Visualización de la red multimodal");
        panelGrafoLienzo.setLayout(new java.awt.BorderLayout());
        getContentPane().add(panelGrafoLienzo, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 900, 480));

        Archivo.setText("Archivo");

        cargarCSV.setText("Cargar .CSV");
        cargarCSV.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cargarCSVActionPerformed(evt);
            }
        });
        Archivo.add(cargarCSV);

        guardarCSV.setText("Guardar .CSV");
        guardarCSV.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                guardarCSVActionPerformed(evt);
            }
        });
        Archivo.add(guardarCSV);

        Menu.add(Archivo);

        Editar.setText("Editar");

        botonAgregarEstacion.setText("Agregar estación");
        botonAgregarEstacion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botonAgregarEstacionActionPerformed(evt);
            }
        });
        Editar.add(botonAgregarEstacion);

        botonEliminarEstacion.setText("Eliminar estación");
        botonEliminarEstacion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botonEliminarEstacionActionPerformed(evt);
            }
        });
        Editar.add(botonEliminarEstacion);

        modificarTiempo.setText("Modificar Tiempo");
        modificarTiempo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                modificarTiempoActionPerformed(evt);
            }
        });
        Editar.add(modificarTiempo);

        Menu.add(Editar);

        Algoritmos.setText("Algoritmos y análisis");

        calcularRuta.setText("Calcular ruta (Algoritmo de Dijkstra)");
        calcularRuta.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                calcularRutaActionPerformed(evt);
            }
        });
        Algoritmos.add(calcularRuta);

        detectarSubredes.setText("Detectar Sub-redes");
        Algoritmos.add(detectarSubredes);

        identificarHubs.setText("Identificar Hubs");
        Algoritmos.add(identificarHubs);

        Menu.add(Algoritmos);

        setJMenuBar(Menu);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Guarda la red actual desde la opcion Archivo > Guardar CSV.
     * El selector permite elegir el archivo de destino.
     *
     * @param evt evento del menu
     */
    private void guardarCSVActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_guardarCSVActionPerformed
        guardarRedConSelector();
    }//GEN-LAST:event_guardarCSVActionPerformed

    /**
     * Permite seleccionar otro CSV. Si ya hay una red abierta, ofrece
     * guardarla antes de reemplazar su contenido.
     *
     * @param evt evento del menu
     */
    private void cargarCSVActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cargarCSVActionPerformed
        JFileChooser selector = new JFileChooser();
        selector.setDialogTitle("Cargar red de transporte");
        selector.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Archivos CSV (*.csv)", "csv"));

        if (selector.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        java.io.File siguienteArchivo = selector.getSelectedFile();

        if (!grafo.getEstaciones().estaVacia()) {
            int respuesta = JOptionPane.showConfirmDialog(
                    this,
                    "Ya hay una red cargada en memoria.\n"
                    + "¿Deseas guardarla antes de cargar el nuevo CSV?",
                    "Guardar antes de cargar",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE);

            if (respuesta == JOptionPane.CANCEL_OPTION
                    || respuesta == JOptionPane.CLOSED_OPTION) {
                return;
            }
            if (respuesta == JOptionPane.YES_OPTION && !guardarRedConSelector(siguienteArchivo)) {
                // Si se cancela o falla el guardado, se conserva la red anterior.
                return;
            }
        }

        // Cargar primero en un grafo nuevo: no mezclar ambas redes.
        Grafo nuevaRed = new Grafo();
        try {
            GestorArchivo.cargarDesdeCSVConErrores(siguienteArchivo, nuevaRed);
        } catch (java.io.IOException | IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo cargar el CSV.\n" + ex.getMessage(),
                    "Error al cargar", JOptionPane.ERROR_MESSAGE);
            return;
        }

        grafo = nuevaRed;
        archivoActual = siguienteArchivo;
        actualizarVistaGrafo();
        JOptionPane.showMessageDialog(this,
                "Archivo cargado correctamente.",
                "Carga completada", JOptionPane.INFORMATION_MESSAGE);
    }//GEN-LAST:event_cargarCSVActionPerformed

    /**
     * Abre el formulario para agregar una estacion.
     * 
     * @param evt evento de la interfaz
     */
    private void botonAgregarEstacionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botonAgregarEstacionActionPerformed
        prepararFormularioAgregarEstacion();

        editarAgregarEstacion.pack();
        editarAgregarEstacion.setLocationRelativeTo(this);
        editarAgregarEstacion.setVisible(true);
    }//GEN-LAST:event_botonAgregarEstacionActionPerformed

    /**
     * Abre el formulario para cambiar el tiempo de una conexion.
     * 
     * @param evt evento de la interfaz
     */
    private void modificarTiempoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_modificarTiempoActionPerformed

        cargarEstacionesModificarTiempo();

        tiempoIn.setText("");

        editarModificarTiempoJFrame.pack();
        editarModificarTiempoJFrame.setLocationRelativeTo(this);
        editarModificarTiempoJFrame.setVisible(true);
    }//GEN-LAST:event_modificarTiempoActionPerformed

    private void tiempoInActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tiempoInActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tiempoInActionPerformed

    /**
     * Abre el formulario para buscar la ruta de menor tiempo.
     * 
     * @param evt evento de la interfaz
     */
    private void calcularRutaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_calcularRutaActionPerformed
        prepararFormularioRuta();

        algoritmoRuta.pack();
        algoritmoRuta.setLocationRelativeTo(this);
        algoritmoRuta.setVisible(true);        // TODO add your handling code here:
    }//GEN-LAST:event_calcularRutaActionPerformed

    private void nuevaTiempoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nuevaTiempoActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_nuevaTiempoActionPerformed

    /**
     * Agrega una conexion a la lista temporal de la estacion nueva.
     * 
     * @param evt evento de la interfaz
     */
    private void nuevaAgregarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nuevaAgregarActionPerformed
        if (nuevaConexion.getSelectedIndex() <= 0) {
            JOptionPane.showMessageDialog(
                    editarAgregarEstacion,
                    "Selecciona una estación existente."
            );
            return;
        }

        String seleccion
                = nuevaConexion.getSelectedItem().toString();

        String idDestino = seleccion.substring(
                0, seleccion.indexOf(" - ")
        );

        int tiempo;

        try {
            tiempo = Integer.parseInt(
                    nuevaTiempo.getText().trim()
            );

            if (tiempo <= 0) {
                JOptionPane.showMessageDialog(
                        editarAgregarEstacion,
                        "El tiempo debe ser mayor que cero."
                );
                return;
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(
                    editarAgregarEstacion,
                    "Introduce un número entero válido."
            );
            return;
        }

        // Comprobar que la conexion no esté repetida
        ListaEnlazada.NodoLista<ConexionPendiente> actual
                = conexionesPendientes.getCabeza();

        while (actual != null) {

            if (actual.dato.idDestino.equalsIgnoreCase(idDestino)) {
                JOptionPane.showMessageDialog(
                        editarAgregarEstacion,
                        "Esta conexión ya fue agregada."
                );
                return;
            }

            actual = actual.siguiente;
        }

        conexionesPendientes.agregar(
                new ConexionPendiente(idDestino, tiempo)
        );

        actualizarResumenConexiones();
        nuevaTiempo.setText("");
    }//GEN-LAST:event_nuevaAgregarActionPerformed

    private void modificarTiempoEAActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_modificarTiempoEAActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_modificarTiempoEAActionPerformed

    /**
     * Valida el tiempo nuevo y actualiza la conexion en el grafo.
     * 
     * @param evt evento de la interfaz
     */
    private void modificarTiempoGuardarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_modificarTiempoGuardarActionPerformed
        // Verificar que se seleccionaron dos estaciones
        if (modificarTiempoEA.getSelectedIndex() <= 0
                || modificarTiempoEB.getSelectedIndex() <= 0) {

            JOptionPane.showMessageDialog(
                    editarModificarTiempoJFrame,
                    "Debes seleccionar ambas estaciones.",
                    "Advertencia",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        // Obtener las estaciones seleccionadas
        String seleccionA
                = modificarTiempoEA.getSelectedItem().toString();

        String seleccionB
                = modificarTiempoEB.getSelectedItem().toString();

        // Extraer los identificadores de las estaciones
        String idA = seleccionA.substring(
                0, seleccionA.indexOf(" - ")
        );

        String idB = seleccionB.substring(
                0, seleccionB.indexOf(" - ")
        );

        // Impedir seleccionar dos veces la misma estacion
        if (idA.equals(idB)) {
            JOptionPane.showMessageDialog(
                    editarModificarTiempoJFrame,
                    "Debes seleccionar dos estaciones diferentes."
            );
            return;
        }

        int nuevoTiempo;

        // Validar el tiempo introducido
        try {

            nuevoTiempo = Integer.parseInt(
                    tiempoIn.getText().trim()
            );

            if (nuevoTiempo <= 0) {
                JOptionPane.showMessageDialog(
                        editarModificarTiempoJFrame,
                        "El tiempo debe ser mayor que cero."
                );
                return;
            }

        } catch (NumberFormatException e) {

            JOptionPane.showMessageDialog(
                    editarModificarTiempoJFrame,
                    "Introduce un numero entero valido."
            );
            return;
        }

        // Solicitar al grafo la modificacion
        boolean modificado = grafo.modificarTiempo(
                idA, idB, nuevoTiempo
        );

        if (modificado) {

            JOptionPane.showMessageDialog(
                    editarModificarTiempoJFrame,
                    "Tiempo de traslado actualizado correctamente."
            );

            // Refrescar la visualizacion del grafo
            JPanel lienzo = visualizador.renderizarGrafo(grafo);

            panelGrafoLienzo.removeAll();
            panelGrafoLienzo.add(
                    lienzo,
                    java.awt.BorderLayout.CENTER
            );

            panelGrafoLienzo.revalidate();
            panelGrafoLienzo.repaint();

            // Cerrar la ventana secundaria
            editarModificarTiempoJFrame.setVisible(false);

        } else {

            JOptionPane.showMessageDialog(
                    editarModificarTiempoJFrame,
                    "Las estaciones no tienen una conexion directa activa.",
                    "Conexion no encontrada",
                    JOptionPane.WARNING_MESSAGE
            );
        }        // TODO add your handling code here:
    }//GEN-LAST:event_modificarTiempoGuardarActionPerformed

    /**
     * Valida y registra la nueva estacion y sus conexiones en el grafo.
     * 
     * @param evt evento de la interfaz
     */
    private void nuevaGuardarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nuevaGuardarActionPerformed
        // Obtener los datos introducidos
        String id = nuevaID.getText().trim().toUpperCase();
        String nombre = nuevaNombre.getText().trim();
        String zona = nuevaZona.getText().trim();

        Object seleccionLinea = nuevaLinea.getSelectedItem();

        String linea = seleccionLinea == null
                ? ""
                : seleccionLinea.toString().trim();

        // Verificar que los campos obligatorios estén completos
        if (id.isEmpty() || nombre.isEmpty() || zona.isEmpty()) {

            JOptionPane.showMessageDialog(
                    editarAgregarEstacion,
                    "Debes completar el ID, nombre y zona de la estación.",
                    "Campos incompletos",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        // Verificar que se haya seleccionado una línea válida
        if (linea.isEmpty()
                || linea.equals("Seleccionar línea...")
                || linea.equals("+ Nueva línea...")) {

            JOptionPane.showMessageDialog(
                    editarAgregarEstacion,
                    "Debes seleccionar o crear una línea válida.",
                    "Línea inválida",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        // Evitar valores incompatibles con el CSV
        if (id.contains(",") || nombre.contains(",")
                || linea.contains(",") || zona.contains(",")
                || id.contains("\n") || nombre.contains("\n")
                || linea.contains("\n") || zona.contains("\n")
                || id.contains("\r") || nombre.contains("\r")
                || linea.contains("\r") || zona.contains("\r")) {

            JOptionPane.showMessageDialog(
                    editarAgregarEstacion,
                    "Los campos no pueden contener comas ni saltos de línea.",
                    "Datos inválidos",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        // Comprobar que el ID no esté registrado
        if (grafo.getIndexEstaciones().buscar(id) != null) {

            JOptionPane.showMessageDialog(
                    editarAgregarEstacion,
                    "Ya existe una estación con ese ID.",
                    "Estación duplicada",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        // Validar la capacidad de pasajeros por hora
        int capacidad;

        try {

            capacidad = Integer.parseInt(
                    nuevaCapacidad.getText().trim()
            );

            if (capacidad <= 0) {

                JOptionPane.showMessageDialog(
                        editarAgregarEstacion,
                        "La capacidad debe ser mayor que cero.",
                        "Capacidad inválida",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

        } catch (NumberFormatException e) {

            JOptionPane.showMessageDialog(
                    editarAgregarEstacion,
                    "Introduce un número entero válido para la capacidad.",
                    "Capacidad inválida",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        // Determinar si ya existen estaciones en la red
        boolean hayEstaciones
                = !grafo.getEstaciones().estaVacia();

        // Si ya hay estaciones, exigir al menos una conexión
        // La primera estación puede crearse sin conexiones
        if (hayEstaciones && conexionesPendientes.estaVacia()) {

            JOptionPane.showMessageDialog(
                    editarAgregarEstacion,
                    "Debes agregar al menos una conexión "
                    + "con una estación existente.",
                    "Conexiones requeridas",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        // Verificar todas las conexiones antes de modificar el grafo
        ListaEnlazada.NodoLista<ConexionPendiente> actual
                = conexionesPendientes.getCabeza();

        while (actual != null) {

            ConexionPendiente conexion = actual.dato;

            // No permitir conexiones consigo misma
            if (id.equalsIgnoreCase(conexion.idDestino)) {

                JOptionPane.showMessageDialog(
                        editarAgregarEstacion,
                        "Una estación no puede conectarse consigo misma.",
                        "Conexión inválida",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            // Verificar que el destino exista y esté activo
            Estacion destino = grafo.getIndexEstaciones()
                    .buscar(conexion.idDestino);

            if (destino == null || !destino.isActiva()) {

                JOptionPane.showMessageDialog(
                        editarAgregarEstacion,
                        "Una de las estaciones de destino no existe "
                        + "o está inactiva.",
                        "Conexión inválida",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            // Validar el tiempo de traslado
            if (conexion.tiempo <= 0) {

                JOptionPane.showMessageDialog(
                        editarAgregarEstacion,
                        "El tiempo de traslado debe ser mayor que cero.",
                        "Tiempo inválido",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            actual = actual.siguiente;
        }

        // Crear la nueva estación
        Estacion nuevaEstacion = new Estacion(
                id,
                nombre,
                linea,
                zona,
                capacidad
        );

        // Registrar la estación en el grafo y la tabla hash
        grafo.agregarEstacion(nuevaEstacion);

        // Registrar las conexiones pendientes
        actual = conexionesPendientes.getCabeza();

        while (actual != null) {

            ConexionPendiente conexion = actual.dato;

            grafo.agregarConexion(
                    id,
                    conexion.idDestino,
                    conexion.tiempo
            );

            actual = actual.siguiente;
        }

        // Actualizar la visualización del grafo
        JPanel lienzo = visualizador.renderizarGrafo(grafo);

        panelGrafoLienzo.removeAll();

        panelGrafoLienzo.add(
                lienzo,
                java.awt.BorderLayout.CENTER
        );

        panelGrafoLienzo.revalidate();
        panelGrafoLienzo.repaint();

        // Informar al usuario
        if (!hayEstaciones) {

            JOptionPane.showMessageDialog(
                    editarAgregarEstacion,
                    "Primera estación registrada correctamente.\n"
                    + "Ahora puedes agregar nuevas estaciones "
                    + "y conectarlas.",
                    "Estación creada",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } else {

            JOptionPane.showMessageDialog(
                    editarAgregarEstacion,
                    "Estación y conexiones agregadas correctamente.",
                    "Estación creada",
                    JOptionPane.INFORMATION_MESSAGE
            );
        }

        // Limpiar las conexiones pendientes
        conexionesPendientes = new ListaEnlazada<>();

        resumenConexiones.setText("");

        // Cerrar la ventana secundaria
        editarAgregarEstacion.setVisible(false);
    }//GEN-LAST:event_nuevaGuardarActionPerformed

    /**
     * Descarta las conexiones pendientes y cierra el formulario.
     * 
     * @param evt evento de la interfaz
     */
    private void nuevaCancelarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nuevaCancelarActionPerformed
        conexionesPendientes = new ListaEnlazada<>();
        resumenConexiones.setText("");
        editarAgregarEstacion.setVisible(false);
    }//GEN-LAST:event_nuevaCancelarActionPerformed

    private void nuevaConexionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nuevaConexionActionPerformed

    }//GEN-LAST:event_nuevaConexionActionPerformed

    /**
     * Permite elegir una linea existente o introducir una nueva.
     * 
     * @param evt evento de la interfaz
     */
    private void nuevaLineaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nuevaLineaActionPerformed
        if (actualizandoLineas) {
            return;
        }

        Object seleccion = nuevaLinea.getSelectedItem();

        if (seleccion == null) {
            return;
        }

        // Evitar procesar selecciones nulas
        if (seleccion == null) {
            return;
        }

        if (seleccion.toString().equals("+ Nueva línea...")) {

            String nombreLinea = JOptionPane.showInputDialog(
                    editarAgregarEstacion,
                    "Introduce el nombre de la nueva línea:",
                    "Nueva línea",
                    JOptionPane.QUESTION_MESSAGE
            );

            // Si el usuario cancela
            if (nombreLinea == null) {
                nuevaLinea.setSelectedIndex(0);
                return;
            }

            nombreLinea = nombreLinea.trim();

            // Validar que no esté vacío
            if (nombreLinea.isEmpty()) {

                JOptionPane.showMessageDialog(
                        editarAgregarEstacion,
                        "El nombre de la línea no puede estar vacío."
                );

                nuevaLinea.setSelectedIndex(0);
                return;
            }

            // Verificar que la línea no exista
            for (int i = 1; i < nuevaLinea.getItemCount(); i++) {

                if (nombreLinea.equalsIgnoreCase(
                        nuevaLinea.getItemAt(i).toString())) {

                    JOptionPane.showMessageDialog(
                            editarAgregarEstacion,
                            "Esta línea ya existe."
                    );

                    nuevaLinea.setSelectedIndex(i);
                    return;
                }
            }

            // Agregar la nueva línea al JComboBox
            nuevaLinea.insertItemAt(
                    nombreLinea,
                    nuevaLinea.getItemCount() - 1
            );

            nuevaLinea.setSelectedItem(nombreLinea);
        }
    }//GEN-LAST:event_nuevaLineaActionPerformed

    private void rutaOrigenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rutaOrigenActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_rutaOrigenActionPerformed

    /**
     * Ejecuta Dijkstra y muestra el recorrido encontrado.
     * 
     * @param evt evento de la interfaz
     */
    private void rutaCalcularActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rutaCalcularActionPerformed
        // Validar que se hayan seleccionado las estaciones
        if (rutaOrigen.getSelectedIndex() <= 0
                || rutaDestino.getSelectedIndex() <= 0) {

            JOptionPane.showMessageDialog(
                    algoritmoRuta,
                    "Debes seleccionar las estaciones de origen y destino.",
                    "Selección incompleta",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        Estacion origen = obtenerEstacionRuta(
                rutaOrigen.getSelectedIndex()
        );

        Estacion destino = obtenerEstacionRuta(
                rutaDestino.getSelectedIndex()
        );

        // Comprobar que ambas estaciones sean validas
        if (origen == null || destino == null
                || !origen.isActiva() || !destino.isActiva()) {

            JOptionPane.showMessageDialog(
                    algoritmoRuta,
                    "Las estaciones seleccionadas no están disponibles.",
                    "Error",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        // Evitar calcular una ruta hacia la misma estacion
        if (origen.equals(destino)) {

            JOptionPane.showMessageDialog(
                    algoritmoRuta,
                    "Selecciona dos estaciones diferentes.",
                    "Selección inválida",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        // Ejecutar Dijkstra
        AnalizadorRed.ResultadoRuta resultado
                = AnalizadorRed.calcularRutaDetallada(
                        grafo,
                        origen.getId(),
                        destino.getId()
                );

        // Comprobar si existe una ruta
        if (resultado == null) {

            rutaResultado.setText(
                    "No existe una ruta disponible entre:\n"
                    + origen.getNombre() + "\n"
                    + "y\n"
                    + destino.getNombre()
            );

            rutaResultado.setCaretPosition(0);

            // Restaurar visualizacion sin resaltar rutas
            actualizarVisualizacionRuta(null);

            return;
        }

        // Construir el resultado
        StringBuilder texto = new StringBuilder();

        texto.append("RUTA ÓPTIMA\n\n");

        texto.append("Origen: ")
                .append(origen.getNombre())
                .append("\n");

        texto.append("Destino: ")
                .append(destino.getNombre())
                .append("\n\n");

        texto.append("Tiempo mínimo: ")
                .append(resultado.getTiempoTotal())
                .append(" minutos\n\n");

        texto.append("RECORRIDO:\n");

        ListaEnlazada.NodoLista<Estacion> actual
                = resultado.getEstaciones().getCabeza();

        int numero = 1;

        while (actual != null) {

            texto.append(numero)
                    .append(". ")
                    .append(actual.dato.getNombre())
                    .append(" (")
                    .append(actual.dato.getId())
                    .append(")");

            texto.append("\n");

            numero++;
            actual = actual.siguiente;
        }

        texto.append("\nTotal de estaciones: ")
                .append(numero - 1);

        // Mostrar el resultado en el JTextArea
        rutaResultado.setText(texto.toString());
        rutaResultado.setCaretPosition(0);

        // Resaltar las estaciones y conexiones del recorrido
        actualizarVisualizacionRuta(
                resultado.getEstaciones()
        );        // TODO add your handling code here:
    }//GEN-LAST:event_rutaCalcularActionPerformed

    /**
     * Abre el formulario para seleccionar una estacion que se eliminara.
     * 
     * @param evt evento de la interfaz
     */
    private void botonEliminarEstacionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botonEliminarEstacionActionPerformed
        cargarEstacionesEliminar();

        editarEliminarEstacion.pack();
        editarEliminarEstacion.setLocationRelativeTo(this);
        editarEliminarEstacion.setVisible(true);
    }//GEN-LAST:event_botonEliminarEstacionActionPerformed

    private void eliminarEstacionComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_eliminarEstacionComboActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_eliminarEstacionComboActionPerformed

    /**
     * Pide confirmacion y elimina la estacion seleccionada.
     * 
     * @param evt evento de la interfaz
     */
    private void eliminarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_eliminarActionPerformed
        // Comprobar que se seleccionó una estación
        if (eliminarEstacionCombo.getSelectedIndex() <= 0) {

            JOptionPane.showMessageDialog(
                    editarEliminarEstacion,
                    "Debes seleccionar una estación para eliminar.",
                    "Selección requerida",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        // Obtener la estación seleccionada
        String seleccion
                = eliminarEstacionCombo.getSelectedItem().toString();

        // Separar el ID del nombre
        int separador = seleccion.indexOf(" - ");

        if (separador < 0) {
            JOptionPane.showMessageDialog(
                    editarEliminarEstacion,
                    "La estación seleccionada no es válida.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        String id = seleccion.substring(0, separador);

        // Buscar la estación en la tabla hash
        Estacion estacion
                = grafo.getIndexEstaciones().buscar(id);

        if (estacion == null || !estacion.isActiva()) {

            JOptionPane.showMessageDialog(
                    editarEliminarEstacion,
                    "La estación ya no se encuentra disponible.",
                    "Estación no encontrada",
                    JOptionPane.WARNING_MESSAGE
            );

            cargarEstacionesEliminar();
            return;
        }

        // Solicitar confirmación mediante un popup
        int confirmacion = JOptionPane.showConfirmDialog(
                editarEliminarEstacion,
                "¿Estás seguro de que deseas eliminar la estación?\n\n"
                + "Estación: " + estacion.getNombre()
                + "\nID: " + estacion.getId()
                + "\n\nTambién quedarán inhabilitadas sus conexiones.",
                "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        // Si el usuario selecciona No o cierra el popup
        if (confirmacion != JOptionPane.YES_OPTION) {
            return;
        }

        // Eliminar la estación del grafo
        boolean eliminada = grafo.eliminarEstacion(id);

        if (!eliminada) {

            JOptionPane.showMessageDialog(
                    editarEliminarEstacion,
                    "No se pudo eliminar la estación.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        // Actualizar la visualización del grafo
        JPanel lienzo = visualizador.renderizarGrafo(grafo);

        panelGrafoLienzo.removeAll();

        panelGrafoLienzo.setLayout(
                new java.awt.BorderLayout()
        );

        panelGrafoLienzo.add(
                lienzo,
                java.awt.BorderLayout.CENTER
        );

        panelGrafoLienzo.revalidate();
        panelGrafoLienzo.repaint();

        // Informar que la operación se completó
        JOptionPane.showMessageDialog(
                editarEliminarEstacion,
                "La estación " + estacion.getNombre()
                + " fue eliminada correctamente.",
                "Eliminación completada",
                JOptionPane.INFORMATION_MESSAGE
        );

        // Actualizar el JComboBox
        cargarEstacionesEliminar();

        // Cerrar la ventana secundaria
        editarEliminarEstacion.setVisible(false);
    }//GEN-LAST:event_eliminarActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(VentanaPrincipal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(VentanaPrincipal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(VentanaPrincipal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(VentanaPrincipal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new VentanaPrincipal().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenu Algoritmos;
    private javax.swing.JMenu Archivo;
    private javax.swing.JMenu Editar;
    private javax.swing.JMenuBar Menu;
    private javax.swing.JFrame algoritmoRuta;
    private javax.swing.JMenuItem botonAgregarEstacion;
    private javax.swing.JMenuItem botonEliminarEstacion;
    private javax.swing.JMenuItem calcularRuta;
    private javax.swing.JMenuItem cargarCSV;
    private javax.swing.JLabel destinoLabel;
    private javax.swing.JMenuItem detectarSubredes;
    private javax.swing.JFrame editarAgregarEstacion;
    private javax.swing.JFrame editarEliminarEstacion;
    private javax.swing.JFrame editarModificarTiempoJFrame;
    private javax.swing.JButton eliminar;
    private javax.swing.JComboBox<String> eliminarEstacionCombo;
    private javax.swing.JMenuItem guardarCSV;
    private javax.swing.JMenuItem identificarHubs;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JLabel labelCapacidad;
    private javax.swing.JLabel labelConexiones;
    private javax.swing.JLabel labelDatos;
    private javax.swing.JLabel labelEA;
    private javax.swing.JLabel labelEB;
    private javax.swing.JLabel labelEstacion;
    private javax.swing.JLabel labelID;
    private javax.swing.JLabel labelNombre;
    private javax.swing.JLabel labelT;
    private javax.swing.JLabel labelTiempo;
    private javax.swing.JLabel labelZona;
    private javax.swing.JLabel laberLinea;
    private javax.swing.JMenuItem modificarTiempo;
    private javax.swing.JComboBox<String> modificarTiempoEA;
    private javax.swing.JComboBox<String> modificarTiempoEB;
    private javax.swing.JButton modificarTiempoGuardar;
    private javax.swing.JButton nuevaAgregar;
    private javax.swing.JButton nuevaCancelar;
    private javax.swing.JTextField nuevaCapacidad;
    private javax.swing.JComboBox<String> nuevaConexion;
    private javax.swing.JButton nuevaGuardar;
    private javax.swing.JTextField nuevaID;
    private javax.swing.JComboBox<String> nuevaLinea;
    private javax.swing.JTextField nuevaNombre;
    private javax.swing.JTextField nuevaTiempo;
    private javax.swing.JTextField nuevaZona;
    private javax.swing.JLabel origenLabel;
    private javax.swing.JPanel panelGrafoLienzo;
    private javax.swing.JTextArea resumenConexiones;
    private javax.swing.JButton rutaCalcular;
    private javax.swing.JComboBox<String> rutaDestino;
    private javax.swing.JComboBox<String> rutaOrigen;
    private javax.swing.JTextArea rutaResultado;
    private javax.swing.JScrollPane rutaScroll;
    private javax.swing.JTextField tiempoIn;
    // End of variables declaration//GEN-END:variables
}
