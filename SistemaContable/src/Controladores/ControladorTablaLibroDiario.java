/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controladores;

import ModeloContable.Registro;
import ModeloContable.Tipo;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author pc
 */
public class ControladorTablaLibroDiario extends AbstractTableModel{
    private List<Registro> registros;

    public ControladorTablaLibroDiario(List<Registro> registros) {
        this.registros = registros;
        
    }
    
    

    @Override
    public int getRowCount() {
        return registros.size();
    }
    
    
    @Override
    public int getColumnCount() {
        return 4;
    }

    //Este metodo va estar dificil de implementar, dios me ha abandonado
    //Supuestamente ya esta implementado
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        
        Registro registro = registros.get(rowIndex);
        
        switch(columnIndex){
            case 0:
                return registro.getFechaRegistroFormateada();
            case 1:
                return registro.getCuenta().getNombre();
            case 2:
                return (registro.getTipo().equals(Tipo.DEBE))?registro.getValor():"";
            case 3:
                return (registro.getTipo().equals(Tipo.HABER))?registro.getValor():"";
            default:
                return null;
            
        }
    }
    
    
    /***
     * Añade uno o mas registros al modelo y notifica a la tabla de los cambios
     * @param nuevosRegistros 
     */
    public void añadirRegistros(List<Registro> nuevosRegistros){
        registros.addAll(nuevosRegistros);
        fireTableDataChanged();
        
    }
    
}
