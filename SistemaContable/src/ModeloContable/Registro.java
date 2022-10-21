/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ModeloContable;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 *
 * @author pc
 */
public class Registro extends Serializacion{
    private LocalDate fechaRegistro;
    private Cuenta cuenta;
    private Tipo tipo;
    private double valor;
    private StringBuilder descripcion = new StringBuilder();
    private int numRegistro;
    private static int contadorRegistros;

    public Registro(){
    
    }

    /***
     * 
     * @param fechaRegistro
     * @param cuenta
     * @param tipo
     * @param valor 
     */
    public Registro(LocalDate fechaRegistro, Cuenta cuenta, Tipo tipo, double valor) {
        this.fechaRegistro = fechaRegistro;
        this.cuenta = cuenta;
        this.tipo = tipo;
        this.valor = valor;
    }

    public int getNumRegistro() {
        return numRegistro;
    }

    public void setNumRegistro(int numRegistro){
        this.numRegistro = numRegistro;
    }

    public static int getContadorRegistros() {
        return contadorRegistros;
    }

    public static void setContadorRegistros(int contadorRegistros) {
        Registro.contadorRegistros = contadorRegistros;
    }
    
    

    public Tipo getTipo() {
        return tipo;
    }

    public void setTipo(Tipo tipo) {
        this.tipo = tipo;
    }

    public double getValor() {
        return valor;
    }

    public void setValor(double valor) {
        this.valor = valor;
    }

    public LocalDate getFechaRegistro() {
        return fechaRegistro;
    }
    
    public String getFechaRegistroFormateada(){
        return fechaRegistro.format(DateTimeFormatter.ofPattern("dd/MM/YYYY", Locale.FRENCH));
    }

    public void setFechaRegistro(LocalDate fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public Cuenta getCuenta() {
        return cuenta;
    }

    public void setCuenta(Cuenta cuenta) {
        this.cuenta = cuenta;
    }

    public StringBuilder getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(StringBuilder descripcion) {
        this.descripcion = descripcion;
    }
    
    

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Registro{");
        sb.append("tipo=").append(tipo);
        sb.append(", valor=").append(valor);
        sb.append('}');
        return sb.toString();
    }
    
    
    
}
