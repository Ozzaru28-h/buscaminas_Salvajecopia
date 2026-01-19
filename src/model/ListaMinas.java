package model;

public class ListaMinas {
    Nodo cabecera;

    public ListaMinas() {
        this.cabecera = null;
    }

    public void insertar(int f, int c) {
        Nodo nuevo = new Nodo(f, c);
        if (cabecera == null) {
            cabecera = nuevo;
        } else {
            // Insertar al inicio (más rápido O(1))
            nuevo.sig = cabecera;
            cabecera.ant = nuevo;
            cabecera = nuevo;
        }
    }

    //* Método para actualizar una mina cuando se mueve (Innovación)
    public void moverMina(int filaVieja, int colVieja, int filaNueva, int colNueva) {
        Nodo aux = cabecera;
        while (aux != null) {
            if (aux.getFila() == filaVieja && aux.getCol() == colVieja) {
                aux.setFila(filaNueva);
                aux.setCol(colNueva);
                return;
            }
            aux = aux.sig;
        }

    }

    public Nodo getCabecera() { return cabecera; }
}