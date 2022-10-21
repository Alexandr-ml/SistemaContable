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
public class LibroMayor extends Serializacion{
   private final List<Cuenta> cuentas = new ArrayList<>();
   List<LibroDiario> libroDiarios;
   List<Registro> ajustes;

   public LibroMayor(){}
   
   


    public List<Cuenta> getCuentas() {
        return cuentas;
    }

    public void añadirCuentas(List<Cuenta> cuentas){
        this.cuentas.addAll(cuentas);
    }


    public List<LibroDiario> getLibroDiarios() {
        return libroDiarios;
    }

    public void setLibroDiarios(List<LibroDiario> libroDiarios) {
        this.libroDiarios = libroDiarios;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("LibroMayor{");
        sb.append("cuentas=").append(cuentas);
        sb.append(", libroDiarios=").append(libroDiarios);
        sb.append(", ajustes=").append(ajustes);
        sb.append('}');
        return sb.toString();
    }
    
    

  
   
   
   
}
