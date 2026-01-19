package view;

import controller.Tablero;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class Interfaz extends JFrame {
    private Tablero tablero;
    private JButton[][] botones;
    private Timer timerInestabilidad;
    private JPanel panelTablero;

    // NUEVO: Bandera para saber si el jugador explotó pero el juego sigue corriendo
    private boolean juegoTerminado = false;

    public Interfaz() {
        int filas = 10;
        int cols = 10;
        int minas = 10;

        tablero = new Tablero(filas, cols, minas);
        botones = new JButton[filas][cols];

        setTitle("Buscaminas Inestable - Grupo B");
        setSize(600, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        panelTablero = new JPanel(new GridLayout(filas, cols));
        inicializarBotones();
        add(panelTablero, BorderLayout.CENTER);

        JPanel panelControl = new JPanel();

        JButton btnDeshacer = new JButton("Deshacer (Pila)");
        btnDeshacer.setBackground(Color.CYAN);
        btnDeshacer.addActionListener(e -> {
            tablero.deshacerJugada();
            // Si estábamos muertos, "revivimos" al deshacer
            juegoTerminado = false;
            actualizarVista();
        });

        JButton btnReiniciar = new JButton("Reiniciar Juego");
        btnReiniciar.setBackground(Color.ORANGE);
        btnReiniciar.addActionListener(e -> {
            reiniciarTodo();
        });

        panelControl.add(btnDeshacer);
        panelControl.add(btnReiniciar);
        add(panelControl, BorderLayout.SOUTH);

        // CAMBIO 1: Velocidad a 3000ms (3 segundos)
        timerInestabilidad = new Timer(3000, e -> {
            tablero.moverMinaAleatoria();
            actualizarVista();

            // Detalle visual para que veas en consola que sigue vivo
            if(juegoTerminado) {
                System.out.println("¡Las minas siguen moviéndose sobre tu cadáver!");
            }
        });
        timerInestabilidad.start();
    }

    private void inicializarBotones() {
        panelTablero.removeAll();
        int filas = tablero.getFilas();
        int cols = tablero.getCols();

        for (int i = 0; i < filas; i++) {
            for (int j = 0; j < cols; j++) {
                JButton btn = new JButton();
                btn.setFont(new Font("Arial", Font.BOLD, 16));
                final int f = i;
                final int c = j;

                btn.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        // Si perdiste, bloqueamos los CLICS, pero NO el timer
                        if (juegoTerminado) return;

                        if (SwingUtilities.isRightMouseButton(e)) {
                            tablero.alternarBandera(f, c);
                            actualizarVista();
                        } else {
                            if (!tablero.banderas[f][c]) {
                                tablero.descubrirCasilla(f, c);
                                actualizarVista();
                                checkEstadoJuego(f, c);
                            }
                        }
                    }
                });
                botones[i][j] = btn;
                panelTablero.add(btn);
            }
        }
        panelTablero.revalidate();
        panelTablero.repaint();
    }

    private void reiniciarTodo() {
        tablero.reiniciarJuego();
        juegoTerminado = false; // Resucitar estado

        for (int i = 0; i < tablero.getFilas(); i++) {
            for (int j = 0; j < tablero.getCols(); j++) {
                botones[i][j].setText("");
                botones[i][j].setBackground(null);
                botones[i][j].setEnabled(true);
            }
        }
        // Asegurarnos que el timer corra
        if (!timerInestabilidad.isRunning()) timerInestabilidad.start();
    }

    private void actualizarVista() {
        for (int i = 0; i < tablero.getFilas(); i++) {
            for (int j = 0; j < tablero.getCols(); j++) {
                JButton btn = botones[i][j];
                int valor = tablero.matriz[i][j];

                // CAMBIO 2: Lógica de "Rayos X"
                // Si el juego terminó Y hay una mina ahí, la mostramos aunque no esté "destapada"
                boolean mostrarMinaPorDerrota = (juegoTerminado && valor == -1);

                // 1. BANDERAS
                if (tablero.banderas[i][j]) {
                    btn.setText("F");
                    btn.setForeground(Color.WHITE);
                    btn.setBackground(Color.BLUE);
                    continue;
                }

                // 2. CASILLAS DESTAPADAS O MODO DERROTA
                if (tablero.visible[i][j] || mostrarMinaPorDerrota) {

                    if (tablero.visible[i][j]) {
                        btn.setEnabled(false); // Solo desactivamos si fue clic real
                    }

                    if (valor == -1) {
                        btn.setText("X");
                        btn.setBackground(Color.RED);
                        // Si es modo derrota, quizás quieras que el botón parezca activo
                        // para ver mejor el movimiento
                    } else if (valor > 0) {
                        btn.setText(String.valueOf(valor));
                        btn.setBackground(Color.LIGHT_GRAY);
                    } else {
                        btn.setText("");
                        btn.setBackground(Color.GRAY);
                    }
                }
                // 3. CASILLAS OCULTAS (Restaurar estado para el Deshacer/Movimiento)
                else {
                    btn.setEnabled(true);
                    btn.setText("");
                    btn.setBackground(null);
                }
            }
        }
    }

    private void checkEstadoJuego(int f, int c) {
        if (tablero.matriz[f][c] == -1) {
            // CAMBIO 3: YA NO PARAMOS EL TIMER
            // timerInestabilidad.stop(); <--- BORRADO

            juegoTerminado = true; // Activamos modo "ver minas moviéndose"

            // Actualizamos vista inmediatamente para ver la explosión
            actualizarVista();

            JOptionPane.showMessageDialog(this, "¡BOOM! Explotaste.\n(Pero las minas siguen vivas...)");
        }
        else if (tablero.haGanado()) {
            timerInestabilidad.stop(); // Si ganas, ahí sí paramos todo
            JOptionPane.showMessageDialog(this, "¡FELICIDADES! Has ganado limpiando el campo minado.");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Interfaz().setVisible(true));
    }
}