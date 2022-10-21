/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controladores;

import ModeloContable.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author pc
 */
public class ControladorTablaLibroMayor extends AbstractTableModel{
    private List<Cuenta> datos;

    public ControladorTablaLibroMayor(List<Cuenta> datos) {
        this.datos = datos;
    }
    
    

    @Override
    public int getRowCount() {
        return datos.size();
    }

    @Override
    public int getColumnCount() {
        return 6;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        
        switch(columnIndex){
            case 0:
                return datos.get(rowIndex).getCodCuenta();
            case 1:
                return datos.get(rowIndex).getNombre();
            case 2:
                return datos.get(rowIndex).getTotalDebe();
            case 3:
                return datos.get(rowIndex).getTotalHaber();
            case 4:
                return Math.abs(datos.get(rowIndex).getSaldo());
            case 5:
                return datos.get(rowIndex).getNaturaleza();
            default: return null;
        }
    }

   
    
    
    
    
    
}
