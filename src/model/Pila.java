package model;

public class Pila {
    private Nodo tope;

    public void apilar(int f, int c) {
        Nodo nuevo = new Nodo(f, c);
        nuevo.sig = tope;
        tope = nuevo;
    }

    public Nodo desapilar() {
        if (tope == null) return null;
        Nodo aux = tope;
        tope = tope.sig;
        return aux;
    }

    public boolean esVacia() { return tope == null; }
}