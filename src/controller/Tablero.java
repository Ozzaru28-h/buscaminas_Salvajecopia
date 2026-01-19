package controller;

import model.Cola;
import model.ListaMinas;
import model.Nodo;
import model.Pila;

import java.util.Random;

import java.util.Random;

public class Tablero {
    public int[][] matriz;
    public boolean[][] visible;
    public boolean[][] banderas; // Arreglo para lógica de banderas

    private int filas;
    private int cols;
    private int totalMinas;
    private int casillasSegurasDescubiertas; // Contador para ganar

    // ESTRUCTURAS DE DATOS
    private ListaMinas listaMinas;
    private Pila historial;
    private Cola colaExpansion;

    public Tablero(int filas, int cols, int minas) {
        this.filas = filas;
        this.cols = cols;
        this.totalMinas = minas;
        inicializarJuego();
    }

    // Método público para reiniciar desde la Interfaz
    public void reiniciarJuego() {
        inicializarJuego();
    }

    private void inicializarJuego() {
        matriz = new int[filas][cols];
        visible = new boolean[filas][cols];
        banderas = new boolean[filas][cols];
        listaMinas = new ListaMinas();
        historial = new Pila();
        colaExpansion = new Cola();
        casillasSegurasDescubiertas = 0;

        Random rand = new Random();
        int colocadas = 0;

        while (colocadas < totalMinas) {
            int f = rand.nextInt(filas);
            int c = rand.nextInt(cols);
            if (matriz[f][c] != -1) {
                matriz[f][c] = -1;
                listaMinas.insertar(f, c);
                colocadas++;
            }
        }
        calcularNumeros();
    }

    private void calcularNumeros() {
        for (int i = 0; i < filas; i++) {
            for (int j = 0; j < cols; j++) {
                if (matriz[i][j] != -1) {
                    matriz[i][j] = contarMinasRecursivo(i, j, -1, -1);
                }
            }
        }
    }

    private int contarMinasRecursivo(int f, int c, int df, int dc) {
        if (df > 1) return 0;
        int nextDC = (dc == 1) ? -1 : dc + 1;
        int nextDF = (dc == 1) ? df + 1 : df;
        int cuenta = 0;
        int r = f + df;
        int col = c + dc;
        if (r >= 0 && r < filas && col >= 0 && col < cols) {
            if (matriz[r][col] == -1) cuenta = 1;
        }
        return cuenta + contarMinasRecursivo(f, c, nextDF, nextDC);
    }

    public void descubrirCasilla(int f, int c) {
        if (f < 0 || f >= filas || c < 0 || c >= cols || visible[f][c] || banderas[f][c]) return;

        historial.apilar(f, c); // Guardar en Pila
        visible[f][c] = true;

        if (matriz[f][c] != -1) {
            casillasSegurasDescubiertas++;
        }

        if (matriz[f][c] == 0) {
            colaExpansion.encolar(f, c); // Usar Cola
            ejecutarExpansionCola();
        }
    }

    private void ejecutarExpansionCola() {
        while (!colaExpansion.esVacia()) {
            Nodo actual = colaExpansion.desencolar();
            int f = actual.getFila();
            int c = actual.getCol();

            for (int i = f - 1; i <= f + 1; i++) {
                for (int j = c - 1; j <= c + 1; j++) {
                    if (i >= 0 && i < filas && j >= 0 && j < cols) {
                        if (!visible[i][j] && matriz[i][j] != -1) {
                            visible[i][j] = true;
                            casillasSegurasDescubiertas++; // Contamos también las automáticas
                            if (matriz[i][j] == 0) {
                                colaExpansion.encolar(i, j);
                            }
                        }
                    }
                }
            }
        }
    }

    public void moverMinaAleatoria() {
        if (listaMinas.getCabecera() == null) return;

        Random rand = new Random();

        // 1. ELEGIR UNA MINA AL AZAR (Recorriendo la Lista Enlazada)
        // Generamos un número al azar y avanzamos esos pasos en la lista
        int saltos = rand.nextInt(totalMinas);
        Nodo minaSeleccionada = listaMinas.getCabecera();

        for (int i = 0; i < saltos; i++) {
            if (minaSeleccionada.getSig() != null) {
                minaSeleccionada = minaSeleccionada.getSig();
            } else {
                // Si llegamos al final, volvemos al principio (lista circular simulada)
                minaSeleccionada = listaMinas.getCabecera();
            }
        }

        // 2. VALIDACIONES DE SEGURIDAD
        // Si la mina seleccionada ya explotó o tiene bandera, no la tocamos
        // (Para no mover una mina que el usuario ya marcó)
        if (visible[minaSeleccionada.getFila()][minaSeleccionada.getCol()] ||
                banderas[minaSeleccionada.getFila()][minaSeleccionada.getCol()]) {
            return;
        }

        // 3. MOVER LA MINA (Lógica de Matriz)
        // Quitamos la mina de su posición actual
        matriz[minaSeleccionada.getFila()][minaSeleccionada.getCol()] = 0;

        // Buscamos una nueva posición libre en el tablero
        int nuevaF, nuevaC;
        do {
            nuevaF = rand.nextInt(filas);
            nuevaC = rand.nextInt(cols);
            // Evitamos ponerla donde ya hay mina, donde ya está destapado o donde hay bandera
        } while (matriz[nuevaF][nuevaC] == -1 ||
                visible[nuevaF][nuevaC] ||
                banderas[nuevaF][nuevaC]);

        // Colocamos la mina en la nueva posición
        matriz[nuevaF][nuevaC] = -1;

        // 4. ACTUALIZAR LA LISTA ENLAZADA (Optimización)
        // Como tenemos la referencia directa al objeto 'Nodo' (minaSeleccionada),
        // podemos cambiar sus valores directamente sin buscarla de nuevo.
        minaSeleccionada.setFila(nuevaF);
        minaSeleccionada.setCol(nuevaC);

        // 5. RECALCULAR NÚMEROS
        // Esto es lo que hace que veas los números cambiar en pantalla
        calcularNumeros();
    }

    public void deshacerJugada() {
        if (!historial.esVacia()) {
            Nodo ultimo = historial.desapilar(); // Sacamos la última coordenada

            // Solo si estaba visible hacemos algo
            if (visible[ultimo.getFila()][ultimo.getCol()]) {
                visible[ultimo.getFila()][ultimo.getCol()] = false; // La ocultamos

                // CORRECCIÓN IMPORTANTE:
                // Solo restamos al contador de victoria si NO era una bomba.
                // Si era bomba, no habíamos sumado punto, así que no restamos.
                if (matriz[ultimo.getFila()][ultimo.getCol()] != -1) {
                    casillasSegurasDescubiertas--;
                }
            }
        }
    }

    // Método para alternar bandera
    public void alternarBandera(int f, int c) {
        if (!visible[f][c]) {
            banderas[f][c] = !banderas[f][c];
        }
    }

    // Verificar Victoria
    public boolean haGanado() {
        int totalCasillas = filas * cols;
        int casillasSeguras = totalCasillas - totalMinas;
        return casillasSegurasDescubiertas == casillasSeguras;
    }

    public int getFilas() { return filas; }
    public int getCols() { return cols; }
}