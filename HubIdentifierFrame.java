package vista;

import controlador.AnalizadorRed;
import estructura.Grafo;
import modelo.Estacion;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.FlowLayout;

public class HubIdentifierFrame extends JFrame {

    private Grafo grafo;
    private JTextArea txtResultado;
    private JButton btnBuscar;

    public HubIdentifierFrame(Grafo grafo) {
        this.grafo = grafo;

        setTitle("Identificar Hub");
        setSize(360, 240);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new FlowLayout());

        btnBuscar = new JButton("Identificar Hub Principal");
        txtResultado = new JTextArea(7, 26);
        txtResultado.setEditable(false);

        btnBuscar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBuscarActionPerformed(evt);
            }
        });

        add(btnBuscar);
        add(new JScrollPane(txtResultado));
    }

    private void btnBuscarActionPerformed(java.awt.event.ActionEvent evt) {
        if (this.grafo == null || this.grafo.getEstaciones().estaVacia()) {
            JOptionPane.showMessageDialog(
                this,
                "No hay información de la red cargada.",
                "Advertencia",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        Estacion hub = AnalizadorRed.obtenerHubPrincipal(this.grafo);

        if (hub != null) {
            int conexiones = this.grafo.obtenerAdyacentes(hub).getTamaño();
            txtResultado.setText("Hub Principal Detectado:\n\n"
                    + "• ID: " + hub.getId() + "\n"
                    + "• Nombre: " + hub.getNombre() + "\n"
                    + "• Conexiones activas: " + conexiones);
        } else {
            txtResultado.setText("No se encontraron estaciones activas.");
        }
    }
}
