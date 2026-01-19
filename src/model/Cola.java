package model;

public class Cola {
    private Nodo inicio;
    private Nodo fin;

    public Cola() {
        this.inicio = null;
        this.fin = null;
    }

    public boolean esVacia() {
        return inicio == null;
    }

    public void encolar(int f, int c) {
        Nodo nuevo = new Nodo(f, c);
        if (esVacia()) {
            inicio = nuevo;
            fin = nuevo;
        } else {
            fin.sig = nuevo;
            fin = nuevo;
        }
    }

    public Nodo desencolar() {
        if (esVacia()) return null;

        Nodo aux = inicio;
        inicio = inicio.sig;
        if (inicio == null) {
            fin = null;
        }
        return aux;
    }
}