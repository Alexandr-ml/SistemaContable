/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package utilidades;

import ModeloContable.Cuenta;
import ModeloContable.Registro;
import ModeloContable.Tipo;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

/**
 *
 * @author pc
 */
public class DialogoCrearRegistroDeIVA extends JDialog implements ActionListener{
    private List<Cuenta> cuentasIVA;
    private JPanel contenedor;
    private DefaultListModel<String> modeloListaIVA ;
    private JList<String> listaIVA;
    private Registro registroIncompleto;
    JRadioButton btnCargoIVA;
    JRadioButton btnAbonoIVA;
    ButtonGroup grupo;
    Tipo tipo;
    
    public DialogoCrearRegistroDeIVA(Frame owner, boolean modal,List<Cuenta> cuentasIVA,Registro regitroIncompleto) {
        super(owner, modal);
        this.cuentasIVA = cuentasIVA;
        this.registroIncompleto = regitroIncompleto;
        
        contenedor = new JPanel();
        
        modeloListaIVA = new DefaultListModel<>();
        List<String> listadoCuentasIVA = cuentasIVA.stream()
                            .map(Cuenta::getNombre)
                            .map(String::toLowerCase)
                            .filter(nombreCuenta -> nombreCuenta.startsWith("iva"))
                            .toList();
        
                    
        modeloListaIVA.addAll(listadoCuentasIVA);
                    
                    
        listaIVA = new JList<>();
        contenedor.add(listaIVA);
                
        listaIVA.setModel(modeloListaIVA);
                
        crearSeleccionTipoTransaccionIVA(contenedor);
        crearBotonSeleccionIVA(contenedor,this);
        
        add(contenedor);
        setSize(new Dimension(300,300));
    }
    
        final public void crearBotonSeleccionIVA(JPanel contenedor,JDialog dialogo){
        JButton btnOpcionOK = new JButton("OK");
        
        btnOpcionOK.addActionListener(this);
        
        contenedor.add(btnOpcionOK);
        
        
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String comando = e.getActionCommand();
        
        if(comando.equals("OK")){
            
            if(!listaIVA.isSelectionEmpty() && grupo.getSelection().isSelected() ){
                String nombreCuentaIVAseleccionada = listaIVA.getSelectedValue();
                Cuenta cuentaIVA = cuentasIVA.stream()
                        .filter(cuenta -> cuenta.getNombre().equalsIgnoreCase(nombreCuentaIVAseleccionada))
                        .findFirst()
                        .get();
                
                registroIncompleto.setCuenta(cuentaIVA);
                registroIncompleto.setTipo(tipo);
                dispose();
            }
        }
        
        if(comando.equals("Cargo")){
            tipo = Tipo.DEBE;
        }
        if(comando.equals("Abono")){
            tipo = Tipo.HABER;
        }
        
    }
    
    
        final public void crearSeleccionTipoTransaccionIVA(JPanel contenedor){
        btnCargoIVA = new JRadioButton();
        btnAbonoIVA = new JRadioButton();
        btnCargoIVA.setText("Cargo");
        btnAbonoIVA.setText("Abono");
        
        btnAbonoIVA.addActionListener(this);
        btnCargoIVA.addActionListener(this);
        
        grupo = new ButtonGroup();
        grupo.add(btnCargoIVA);
        grupo.add(btnAbonoIVA);
        
        contenedor.add(btnCargoIVA);
        contenedor.add(btnAbonoIVA);
        
        
        
        
       
        
    }
    
}
