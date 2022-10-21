/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ModeloContable;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author pc
 */
public class InformacionContable extends Serializacion{
    private  LibroMayor libroMayor;
    private  LibroDiario libroDiario ;
    
    
    public  InformacionContable(){}

    public  LibroMayor getLibroMayor() {
        return libroMayor;
    }

    public LibroDiario getLibroDiario() {
        return libroDiario;
    }

    public void setLibroMayor(LibroMayor libroMayor) {
        this.libroMayor = libroMayor;
    }

    public void setLibroDiario(LibroDiario libroDiario) {
        this.libroDiario = libroDiario;
    }

    @Override
    public String toString() {
        return "InformacionContable{" + "libroMayor=" + libroMayor + ", libroDiario=" + libroDiario + '}';
    }
    
    
}
