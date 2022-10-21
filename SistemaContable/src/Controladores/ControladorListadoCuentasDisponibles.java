/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controladores;

import ModeloContable.Cuenta;
import java.util.List;
import static java.util.stream.Collectors.toList;
import javax.swing.AbstractListModel;
import javax.swing.DefaultListModel;

/**
 *
 * @author pc
 */
public class ControladorListadoCuentasDisponibles extends AbstractListModel<String>{
    List<Cuenta> listadoCuentas;

  
    
    
    public List<Cuenta> getListadoCuentas() {
        return listadoCuentas;
    }

    public void setListadoCuentas(List<Cuenta> listadoCuentas) {
        this.listadoCuentas = listadoCuentas;
    }

    @Override
    public int getSize() {
        return listadoCuentas.size();
    }

    @Override
    public String getElementAt(int index) {
        return listadoCuentas.get(index).getNombre();
    }

    public void añadirNuevaCuenta(Cuenta cuentaNueva){
        listadoCuentas.add(cuentaNueva);
        fireIntervalAdded(this, 0, 0);
        
        
    }
    
    
}
