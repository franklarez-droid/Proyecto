Java
package vista;

import controlador.AnalizadorRed;
import estructura.Grafo;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.FlowLayout;

public class SubnetDetectorFrame extends JFrame {

    private Grafo grafo;
    private JTextArea txtResultado;
    private JButton btnCalcular;

    public SubnetDetectorFrame(Grafo grafo) {
        this.grafo = grafo;

        setTitle("Detección de Sub-redes");
        setSize(360, 240);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new FlowLayout());

        btnCalcular = new JButton("Detectar Sub-redes");
        txtResultado = new JTextArea(7, 26);
        txtResultado.setEditable(false);

        btnCalcular.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCalcularActionPerformed(evt);
            }
        });

        add(btnCalcular);
        add(new JScrollPane(txtResultado));
    }

    private void btnCalcularActionPerformed(java.awt.event.ActionEvent evt) {
        if (this.grafo == null || this.grafo.getEstaciones().estaVacia()) {
            JOptionPane.showMessageDialog(
                this,
                "No hay información de la red cargada.",
                "Advertencia",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        int cantidad = AnalizadorRed.contarComponentesConexos(this.grafo);
        txtResultado.setText("Sub-redes independientes encontradas: " + cantidad);
    }
}
