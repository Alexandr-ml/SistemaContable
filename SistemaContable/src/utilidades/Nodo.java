/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package utilidades;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javax.swing.tree.TreeNode;

/**
 *
 * @author pc
 */
public class Nodo<T> implements TreeNode{
    
    private T contenido;
    private Nodo<T> nodoAnterior,padre;
    private NodosHijosAdapater nodosHijosAdapater;
    private boolean esHoja;
    private int indice;

    public Nodo() {
        nodosHijosAdapater = new NodosHijosAdapater();
        setIndice();
    }

    public Nodo<T> getNodoAnterior() {
        return nodoAnterior;
    }

    public void setNodoAnterior(Nodo<T> nodoAnterior) {
        this.nodoAnterior = nodoAnterior;
    }

    public T getContenido() {
        return contenido;
    }

    public void setContenido(T contenido) {
        this.contenido = contenido;
    }
    
    public void añadirHijos(List<Nodo<T>> hijos ){
        nodosHijosAdapater.nodosHijos = hijos;
    }
    
    public List<Nodo<T>> getNodosHijos(){
        return nodosHijosAdapater.getHijos();
    }

    @Override
    public TreeNode getChildAt(int childIndex) {
        return nodosHijosAdapater.getHijoEn(childIndex);
    }

    @Override
    public int getChildCount() {
        return nodosHijosAdapater.getCantidadHijos();
    }

    @Override
    public TreeNode getParent() {
        return padre;
    }

    @Override
    public int getIndex(TreeNode node) {
        return indice;
    }

    @Override
    public boolean getAllowsChildren() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public boolean isLeaf() {
        esHoja = nodosHijosAdapater.getHijos().isEmpty();
        return esHoja;
    }

    @Override
    public Enumeration<? extends TreeNode> children() {
        return nodosHijosAdapater;
    }

    @Override
    public String toString() {
        return contenido.toString();
    }
    
    
    private void setIndice(){
        if(nodoAnterior == null){
            indice = 0;
        }else{
          indice = nodoAnterior.indice+1;  
        }
        
        
    }
    
    public class NodosHijosAdapater implements Enumeration<Nodo>{
        private List<Nodo<T>> nodosHijos;
        
        public NodosHijosAdapater(){
            nodosHijos = new ArrayList<>();
           
            
        }
        
        public void añadirHijos(Nodo<T> hijo){
            nodosHijos.add(hijo);
            
        }
        
        @Override
        public boolean hasMoreElements() {
            return nodosHijos.iterator().hasNext();
        }

        @Override
        public Nodo nextElement() {
            return nodosHijos.iterator().next();
        }
        
        public Nodo getHijoEn(int indx){
            return nodosHijos.get(indx);
        }
        
        public int getCantidadHijos(){
            return nodosHijos.size();
        }
        
        
        
        public List<Nodo<T>> getHijos(){
            return nodosHijos;
        }
    }
    
}