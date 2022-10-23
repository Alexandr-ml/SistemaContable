/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controladores;

import ModeloContable.Cuenta;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author pc
 */
public class ControladorTablaBalanzaComprobacion extends AbstractTableModel{
    private List<Cuenta> cuentasSaldadas;

    public ControladorTablaBalanzaComprobacion(List<Cuenta> cuentasSaldadas) {
        this.cuentasSaldadas = cuentasSaldadas; 
    }
    

    
    @Override
    public int getRowCount() {
        return cuentasSaldadas.size();
    }

    @Override
    public int getColumnCount() {
        return 4;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        
        Cuenta cuenta = cuentasSaldadas.get(rowIndex);
        double saldoCuenta = cuenta.getSaldo();
        switch (columnIndex) {
            case 0: return cuenta.getCodCuenta();
            case 1: return cuenta.getNombre();
            case 2: return (saldoCuenta > 0)? saldoCuenta:""; 
            case 3: return (saldoCuenta < 0)? Math.abs(saldoCuenta): "";
            default: return null;
        }
        
    }
    
}
