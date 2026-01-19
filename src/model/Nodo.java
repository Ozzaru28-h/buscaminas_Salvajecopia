package model;

public class Nodo {
    // Datos genéricos para que sirva para todo
    private int fila;
    private int col;
    // Punteros
    Nodo sig; // Siguiente
    Nodo ant; // Anterior (para lista doble)

    public Nodo(int fila, int col) {
        this.fila = fila;
        this.col = col;
        this.sig = null;
        this.ant = null;
}

    public int getFila() {
        return fila;
    }

    public void setFila(int fila) {
        this.fila = fila;
    }

    public int getCol() {
        return col;
    }

    public void setCol(int col) {
        this.col = col;
    }

    public Nodo getSig() {
        return sig;
    }

    public void setSig(Nodo sig) {
        this.sig = sig;
    }

    public Nodo getAnt() {
        return ant;
    }

    public void setAnt(Nodo ant) {
        this.ant = ant;
    }
}
