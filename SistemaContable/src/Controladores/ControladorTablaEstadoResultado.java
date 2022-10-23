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
public class ControladorTablaEstadoResultado extends AbstractTableModel{
    private List<Cuenta> cuentasEstadoResultado;
    
    public ControladorTablaEstadoResultado(List<Cuenta> cuentasEstadoResultado){
        this.cuentasEstadoResultado = cuentasEstadoResultado;
    }
    
    @Override
    public int getRowCount() {
        return cuentasEstadoResultado.size();
    }

    @Override
    public int getColumnCount() {
        return 4;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Cuenta cuenta = cuentasEstadoResultado.get(rowIndex);
        double saldo = cuenta.getSaldo();
        
        
        return switch (columnIndex) {
            case 0 -> cuenta.getCodCuenta();
            case 1 -> cuenta.getNombre();
            case 2 -> (saldo > 0)? saldo: "";
            case 3 -> (saldo < 0)? saldo: "";
            case 4 -> cuenta.getNaturaleza();
            default -> null;
        };
    }
    
}
