/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ModeloContable;

import java.util.List;
import java.time.LocalDate;
import java.util.ArrayList;
/**
 *
 * @author pc
 */
public class LibroDiario extends Serializacion{
    private List<Registro> asientos = new ArrayList<>();
    private LocalDate fecha;

  

    public List<Registro> getAsientos() {
        return asientos;
    }
    
    public void añadirRegistros(List<Registro> registros){
        asientos.addAll(registros);
    }




    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("LibroDiario{");
        sb.append("asientos=").append(asientos);
        sb.append('}');
        return sb.toString();
    }
    
    
    
}
