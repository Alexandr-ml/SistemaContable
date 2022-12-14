/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package sistemacontable;


import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JOptionPane;


import Controladores.ControladorTablaLibroDiario;
import ModeloContable.Categoria;
import ModeloContable.Cuenta;
import ModeloContable.Registro;
import ModeloContable.Tipo;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.AbstractTableModel;
import utilidades.DialogoCrearRegistroDeIVA;
import utilidades.Nodo;

/**
 *
 * @author pc
 */
public class RegistroAsiento extends javax.swing.JFrame implements PropertyChangeListener,ActionListener{
    private ControladorTablaLibroDiario controladorTablaLibroDiario;
    private ControladorTablaRegistro controladorTablaRegistro;
    private List<Registro> nuevosRegistros;
    private List<Cuenta> cuentasDisp;
    private Cuenta cuentaSeleccionada;
    private JTree arbolDeCuentas;
    private JDialog dialogoSeleccionarCuenta;
    private PropertyChangeSupport cambioListaRegistros;
  
    
    //Debo usar este tipo aqui ya que no es posible saber si un rdio button ha sido seleccionado
    // al hacer click a cualquier radio button asi se cambiara 
    private Tipo tipoTransaccion;
    //Contiene el valor del ultimo registro que fue a??adido

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        List<Registro> source = (List<Registro>)evt.getSource();
        if(source.isEmpty()){
            btnGuardarNuevasTransacciones.setEnabled(true);
        }
    }

    ///ELIMINARRRR ---------------------------------

    

    


    @Override
    public void actionPerformed(ActionEvent e) {
        
        if(e.getActionCommand().equals("OK")) System.out.println("A presionado el boton de OK");
        
    }
    
    class VerificadorValorRegistro extends InputVerifier{

        @Override
        public boolean verify(JComponent input) {
            try{
                Double.valueOf(txtValor.getText());
            }catch(NumberFormatException e){
                JOptionPane.showMessageDialog(rootPane,"Introduzca un valor valido para el monto de la transacci??n.","Error en el valor.",JOptionPane.ERROR_MESSAGE);
                return false;
            }
            
            return true;
           
        }
        
    }
    
 
    public RegistroAsiento(ControladorTablaLibroDiario controladorTablaLibroDiario,List<Cuenta> cuentasDisp) {
        
        
        initComponents();
        controladorTablaRegistro = new ControladorTablaRegistro();
        this.setVisible(true);
        this.setAlwaysOnTop(false);
        this.nuevosRegistros = new ArrayList<>();
        this.controladorTablaLibroDiario = controladorTablaLibroDiario;
        this.cuentasDisp = cuentasDisp;
        
        btngTipoTransaccion.add(rbtnAbono);
        btngTipoTransaccion.add(rbtnCargo);
        txtValor.setInputVerifier( new VerificadorValorRegistro());
        
        lblTotalDebe.setText(null);
        lblTotalHaber.setText(null);
        
        configurarTablaRegistro();
        cambioListaRegistros = new PropertyChangeSupport(nuevosRegistros);
        cambioListaRegistros.addPropertyChangeListener(this);
        
       
        
        tablaRegistrosNuevos.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
            @Override
            public void valueChanged(ListSelectionEvent e) {
                btnEliminarSeleccionTransaccion.setEnabled(true);
                btnGuardarTransaccionModificada.setEnabled(true);
                btnEliminarSeleccion.setEnabled(true);
                DefaultListSelectionModel listModel = (DefaultListSelectionModel)e.getSource();
                if(!e.getValueIsAdjusting()){
                int indiceRegistroSeleccionado = tablaRegistrosNuevos.getSelectedRow();
                
                Registro seleccionado = nuevosRegistros.get(indiceRegistroSeleccionado);
                
                txtCuentaSeleccionada.setText(seleccionado.getCuenta().getNombre());
                txtValor.setText(String.valueOf(seleccionado.getValor()));
                txtFecha.setText(seleccionado.getFechaRegistro().toString());
                txtDescripcion.setText(seleccionado.getDescripcion().toString());
                
                if(seleccionado.getTipo() == Tipo.DEBE){
                    rbtnCargo.setSelected(true);
                }
                else if(seleccionado.getTipo() == Tipo.HABER){
                    rbtnAbono.setSelected(true);
                }
                
                
                } 
            }
        });

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        btngTipoTransaccion = new javax.swing.ButtonGroup();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tablaRegistrosNuevos = new javax.swing.JTable();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        txtValor = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        rbtnCargo = new javax.swing.JRadioButton();
        rbtnAbono = new javax.swing.JRadioButton();
        jLabel1 = new javax.swing.JLabel();
        tieneIVA = new javax.swing.JCheckBox();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtDescripcion = new javax.swing.JTextArea();
        btnA??adirTransaccion = new javax.swing.JButton();
        btnEliminarSeleccionTransaccion = new javax.swing.JButton();
        btnGuardarNuevasTransacciones = new javax.swing.JButton();
        btnArbolDeCuentas = new javax.swing.JButton();
        txtFecha = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        txtCuentaSeleccionada = new javax.swing.JTextField();
        btnGuardarTransaccionModificada = new javax.swing.JButton();
        btnEliminarSeleccion = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        lblTotalDebe = new javax.swing.JLabel();
        lblTotalHaber = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setAlwaysOnTop(true);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent evt) {
                formWindowClosed(evt);
            }
        });

        tablaRegistrosNuevos.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "Fecha", "Cuenta", "Descripci??n", "Debe", "Haber"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Double.class, java.lang.Double.class
            };
            boolean[] canEdit = new boolean [] {
                true, false, false, true, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tablaRegistrosNuevos.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane2.setViewportView(tablaRegistrosNuevos);

        jLabel5.setText("Cuenta:");

        jLabel6.setText("Valor:");

        jLabel7.setText("Tipo de movimiento:");

        rbtnCargo.setText("Cargo");
        rbtnCargo.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                rbtnCargoMouseClicked(evt);
            }
        });
        rbtnCargo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rbtnCargoActionPerformed(evt);
            }
        });

        rbtnAbono.setText("Abono");
        rbtnAbono.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                rbtnAbonoMouseClicked(evt);
            }
        });

        jLabel1.setText("Incluye IVA:");

        tieneIVA.setText("IVA");
        tieneIVA.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                tieneIVAItemStateChanged(evt);
            }
        });

        jLabel2.setText("Descripci??n de la transacci??n:");

        txtDescripcion.setColumns(20);
        txtDescripcion.setRows(5);
        jScrollPane1.setViewportView(txtDescripcion);

        btnA??adirTransaccion.setText("A??adir transacci??n.");
        btnA??adirTransaccion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnA??adirTransaccionActionPerformed(evt);
            }
        });

        btnEliminarSeleccionTransaccion.setText("Eliminar transacci??n seleccionada.");
        btnEliminarSeleccionTransaccion.setEnabled(false);
        btnEliminarSeleccionTransaccion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEliminarSeleccionTransaccionActionPerformed(evt);
            }
        });

        btnGuardarNuevasTransacciones.setText("Guardar.");
        btnGuardarNuevasTransacciones.setEnabled(false);
        btnGuardarNuevasTransacciones.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGuardarNuevasTransaccionesActionPerformed(evt);
            }
        });

        btnArbolDeCuentas.setText("Q");
        btnArbolDeCuentas.setToolTipText("Click para buscar una cuenta");
        btnArbolDeCuentas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnArbolDeCuentasActionPerformed(evt);
            }
        });

        jLabel3.setText("Fecha:");

        txtCuentaSeleccionada.setEditable(false);

        btnGuardarTransaccionModificada.setText("Guardar modificaci??n.");
        btnGuardarTransaccionModificada.setEnabled(false);
        btnGuardarTransaccionModificada.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGuardarTransaccionModificadaActionPerformed(evt);
            }
        });

        btnEliminarSeleccion.setText("Olvidar selecci??n.");
        btnEliminarSeleccion.setEnabled(false);
        btnEliminarSeleccion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEliminarSeleccionActionPerformed(evt);
            }
        });

        jLabel4.setText("Totales:");

        lblTotalDebe.setText("jLabel8");

        lblTotalHaber.setText("jLabel8");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2)
                    .addComponent(jScrollPane1)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(txtValor, javax.swing.GroupLayout.PREFERRED_SIZE, 157, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(jPanel2Layout.createSequentialGroup()
                                        .addComponent(txtCuentaSeleccionada, javax.swing.GroupLayout.PREFERRED_SIZE, 214, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(btnArbolDeCuentas)))
                                .addGap(22, 22, 22)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel2Layout.createSequentialGroup()
                                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(rbtnCargo, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGap(28, 28, 28)
                                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(rbtnAbono, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(tieneIVA, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                    .addGroup(jPanel2Layout.createSequentialGroup()
                                        .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 122, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))))
                            .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 168, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(22, 22, 22)
                        .addComponent(txtFecha, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(btnA??adirTransaccion)
                        .addGap(18, 18, 18)
                        .addComponent(btnEliminarSeleccionTransaccion)
                        .addGap(28, 28, 28)
                        .addComponent(btnGuardarTransaccionModificada)
                        .addGap(18, 18, 18)
                        .addComponent(btnGuardarNuevasTransacciones)
                        .addGap(18, 18, 18)
                        .addComponent(btnEliminarSeleccion)
                        .addGap(0, 35, Short.MAX_VALUE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(lblTotalDebe, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(131, 131, 131)
                        .addComponent(lblTotalHaber, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(57, 57, 57)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 211, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(1, 1, 1)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(lblTotalDebe)
                    .addComponent(lblTotalHaber))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel5)
                            .addComponent(jLabel7)
                            .addComponent(btnArbolDeCuentas)
                            .addComponent(jLabel3)
                            .addComponent(txtFecha, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtCuentaSeleccionada, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(5, 5, 5)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(rbtnCargo)
                            .addComponent(rbtnAbono))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1)
                            .addComponent(tieneIVA)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addGap(71, 71, 71)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel6)
                            .addComponent(txtValor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(18, 18, 18)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnA??adirTransaccion)
                    .addComponent(btnEliminarSeleccionTransaccion)
                    .addComponent(btnGuardarNuevasTransacciones)
                    .addComponent(btnGuardarTransaccionModificada)
                    .addComponent(btnEliminarSeleccion))
                .addContainerGap(26, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
        
    }//GEN-LAST:event_formWindowClosed

    private void btnA??adirTransaccionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnA??adirTransaccionActionPerformed
        Registro registroNuevo = new Registro();
        double valor = Double.parseDouble(txtValor.getText());
        if(rbtnAbono.isSelected()){
            tipoTransaccion = Tipo.HABER;
        }
        else if(rbtnCargo.isSelected()){
            tipoTransaccion = Tipo.DEBE;
        }
        
        boolean camposVacios = (txtValor.getText().isBlank() || txtCuentaSeleccionada.getText().isBlank()) ||
                (txtFecha.getText().isBlank() || (tipoTransaccion==null));
        
        if (camposVacios){
            JOptionPane.showInternalMessageDialog(null,"Ingrese todos los datos necesarios","Error",JOptionPane.ERROR_MESSAGE);
            return;
        }

        
        cuentaSeleccionada = cuentasDisp.stream()
                .filter(cuenta -> cuenta.getNombre().equals(txtCuentaSeleccionada.getText()))
                .findFirst()
                .get();
        //Si la cuenta tiene un valor de IVA relacionado se crear?? un registro especial para este.
         if(tieneIVA.isSelected()){
             Registro registroIVA =  new Registro();
             registroIVA.setValor(valor*0.13);
             registroIVA.setFechaRegistro(LocalDate.parse(txtFecha.getText()));
             registroIVA.getDescripcion().append(txtDescripcion.getText());
             DialogoCrearRegistroDeIVA dialogoCrearRegistroDeIVA = new DialogoCrearRegistroDeIVA(this, true, cuentasDisp,registroIVA);
             dialogoCrearRegistroDeIVA.setVisible(true);
             dialogoCrearRegistroDeIVA.setTitle("Seleccione la cuenta de IVA y el tipo de transacci??n.");
            controladorTablaRegistro.a??adirNuevoRegistro(registroIVA);
            
        }
         
        registroNuevo.setCuenta(cuentaSeleccionada);
        registroNuevo.setTipo(tipoTransaccion);
        registroNuevo.setValor(valor);
        registroNuevo.setFechaRegistro(LocalDate.parse(txtFecha.getText()));
        registroNuevo.getDescripcion().append(txtDescripcion.getText());
        
        
        controladorTablaRegistro.a??adirNuevoRegistro(registroNuevo);
        limpiarParametros();
        //Actualiza el contador de registros
        
    }//GEN-LAST:event_btnA??adirTransaccionActionPerformed

    private void rbtnCargoMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_rbtnCargoMouseClicked
       
    }//GEN-LAST:event_rbtnCargoMouseClicked

    private void rbtnAbonoMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_rbtnAbonoMouseClicked
      
        
    }//GEN-LAST:event_rbtnAbonoMouseClicked

    private void btnGuardarNuevasTransaccionesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGuardarNuevasTransaccionesActionPerformed
       controladorTablaLibroDiario.a??adirRegistros(nuevosRegistros);
       dispose();
        
    }//GEN-LAST:event_btnGuardarNuevasTransaccionesActionPerformed

    private void btnArbolDeCuentasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnArbolDeCuentasActionPerformed
        
        Nodo<String> raiz = new Nodo<>();
        raiz.setContenido("Seleccione la cuenta");
        
        Map<Categoria,List<Cuenta>> agrupacionCuentarPorCategorias = agruparCuentasPorCategorias();
        
        Iterator<Categoria> iterCategorias = agrupacionCuentarPorCategorias.keySet().iterator();
        
        a??adirHijosDesdeIterador(iterCategorias, raiz);
        
        //categorias.stream().forEach(a -> System.out.println(a.getContenido()));
        a??adirHijosDesdeLista(raiz, agrupacionCuentarPorCategorias);
       
        arbolDeCuentas = new JTree(raiz);
        arbolDeCuentas.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                
                Nodo<String> seleccion =(Nodo<String>) e.getNewLeadSelectionPath().getLastPathComponent();
                
                switch(seleccion.getContenido()){
                    case "ACTIVO":break;
                    case "PASIVO":break;
                    case "INGRESOS":break;
                    case "COSTOS_y_GASTOS":break;
                    default:txtCuentaSeleccionada.setText(seleccion.getContenido());dialogoSeleccionarCuenta.dispose();
                }
                
                
                
            }
        });
        

        
        dialogoSeleccionarCuenta = new JDialog(this,true);
        dialogoSeleccionarCuenta.setSize(new Dimension(300,300));
        dialogoSeleccionarCuenta.setTitle("Seleccionar cuenta");
        
        
        dialogoSeleccionarCuenta.add(arbolDeCuentas);
        
        dialogoSeleccionarCuenta.setVisible(true);
    }//GEN-LAST:event_btnArbolDeCuentasActionPerformed

    private void tieneIVAItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_tieneIVAItemStateChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_tieneIVAItemStateChanged

    private void btnEliminarSeleccionTransaccionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEliminarSeleccionTransaccionActionPerformed
        int indiceElementoSeleccionado = tablaRegistrosNuevos.getSelectedRow();
        controladorTablaRegistro.removerTransaccion(indiceElementoSeleccionado);
        
    }//GEN-LAST:event_btnEliminarSeleccionTransaccionActionPerformed

    private void btnGuardarTransaccionModificadaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGuardarTransaccionModificadaActionPerformed
        int indiceNuevoRegistroModificado = tablaRegistrosNuevos.getSelectedRow();
        
        if(indiceNuevoRegistroModificado == -1) return;
        
        Registro registroParaModificar = nuevosRegistros.get(indiceNuevoRegistroModificado);
        Cuenta cuenta = cuentasDisp.stream()
                .filter(cuentaB -> cuentaB.getNombre().equals(txtCuentaSeleccionada.getText()))
                .findFirst()
                .get();
        
        registroParaModificar.setCuenta(cuenta);
        registroParaModificar.setValor(txtValor.getText().transform(Double::parseDouble));
        registroParaModificar.getDescripcion().delete(0,registroParaModificar.getDescripcion().length());
        registroParaModificar.getDescripcion().append(txtDescripcion.getText());
        registroParaModificar.setTipo(tipoTransaccion);
        registroParaModificar.setFechaRegistro(LocalDate.parse(txtFecha.getText()));
        
        limpiarParametros();
        controladorTablaRegistro.fireTableDataChanged();
    }//GEN-LAST:event_btnGuardarTransaccionModificadaActionPerformed

    private void btnEliminarSeleccionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEliminarSeleccionActionPerformed
        limpiarParametros();
        btnEliminarSeleccion.setEnabled(false);
        btnGuardarTransaccionModificada.setEnabled(false);
    }//GEN-LAST:event_btnEliminarSeleccionActionPerformed

    private void rbtnCargoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rbtnCargoActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_rbtnCargoActionPerformed

    public void a??adirHijosDesdeLista(Nodo<String> nodoRaiz,Map<Categoria,List<Cuenta>> agrupacionPorCategoria){
        
        List<Nodo<String>> hijosNodoRaiz = nodoRaiz.getNodosHijos();
       
        
        List<List<Nodo<String>>> listaDeListaNodosHijos = new ArrayList<>();
        
        Nodo<String> nodoAnt = null;
        
        for(var listaDeListaDeCuentas: agrupacionPorCategoria.values()){
             List<Nodo<String>> hijosNodo = new ArrayList<>();
            for (var cuenta : listaDeListaDeCuentas){
                Nodo<String> nodo = new Nodo<>();
                nodo.setContenido(cuenta.getNombre());
                nodo.setNodoAnterior(nodoAnt);
                
                nodoAnt = nodo;
                hijosNodo.add(nodo);
            }
            listaDeListaNodosHijos.add(hijosNodo);
           
        }
        
        Iterator<Nodo<String>> iterator = hijosNodoRaiz.iterator();
        
        var iter = listaDeListaNodosHijos.iterator();
        while(iterator.hasNext()){
            iterator.next().a??adirHijos(iter.next());
        }
        
      
    }
    
    public Map<Categoria,List<Cuenta>> agruparCuentasPorCategorias(){
        return cuentasDisp.stream()
                          .collect(Collectors.groupingBy(cuenta -> cuenta.getCategoria()));
    }
    
    public void a??adirHijosDesdeIterador(Iterator<Categoria> iterator,Nodo<String> raiz){
         List<Nodo<String>> categorias = new ArrayList<>();
        Nodo<String> nodoAnte = null;
        
        while(iterator.hasNext()){
            Nodo<String> nodo = new Nodo<>();
            
            Categoria categoria = iterator.next();
            nodo.setContenido(categoria.toString());
            nodo.setNodoAnterior(nodoAnte);
            nodoAnte = nodo;
            categorias.add(nodo);
        }
        
        raiz.a??adirHijos(categorias);
    }
    
    public Registro crearRegistroConIVA(Tipo tipoTransaccionIva,double porcentajeIVA,double valor,String nombreCuentaIVA){
        Cuenta cuentaIVA  = cuentasDisp.stream()
                .filter(cuenta -> cuenta.getNombre().equals(nombreCuentaIVA))
                .findFirst()
                .get();
      
        
        Registro registroIVA = new Registro();
        
        registroIVA.setCuenta(cuentaIVA);
        registroIVA.setTipo(tipoTransaccionIva);
        registroIVA.setValor(porcentajeIVA*valor);
        registroIVA.setFechaRegistro(LocalDate.parse(txtFecha.getText()));
        registroIVA.getDescripcion().append(txtDescripcion.getText());
        
        return registroIVA;
    }
   
    /***
     * Limpia todos los parametros a rellenar en la interfaz para crear una transaccion
     */
    public void limpiarParametros(){
        btngTipoTransaccion.clearSelection();
        txtDescripcion.setText(null);
        txtFecha.setText(null);
        txtValor.setText(null);
        txtCuentaSeleccionada.setText(null);
        tieneIVA.setSelected(false);
        
    }
    
    public final void configurarTablaRegistro(){
        tablaRegistrosNuevos.setModel(controladorTablaRegistro);
        tablaRegistrosNuevos.setDragEnabled(false);
        
        int numCols = tablaRegistrosNuevos.getColumnCount();
        
        for(int i=0;i<numCols;i++){
            if(i==0){
                tablaRegistrosNuevos.getColumnModel().getColumn(i).setHeaderValue("Fecha");
            }
            if(i==1){
                tablaRegistrosNuevos.getColumnModel().getColumn(i).setHeaderValue("Cuenta");
            }
            if(i==2){
                tablaRegistrosNuevos.getColumnModel().getColumn(i).setHeaderValue("Descripci??n");
            }
            if(i==3){
                tablaRegistrosNuevos.getColumnModel().getColumn(i).setHeaderValue("Debe");
            }
            if(i==4){
                tablaRegistrosNuevos.getColumnModel().getColumn(i).setHeaderValue("Haber");
            }
            
        }
        
        
        controladorTablaRegistro.addTableModelListener(cambioEnTablaEvent->{
        
            ControladorTablaRegistro tablaOrigenEvento = (ControladorTablaRegistro)cambioEnTablaEvent.getSource();
            double totalDebe = Double.parseDouble(lblTotalDebe.getText());
            double totalHaber = Double.parseDouble(lblTotalHaber.getText());
            boolean seCumplePartidaDoble = totalDebe == totalHaber;
           
            if(tablaOrigenEvento.getRowCount()>=2 & seCumplePartidaDoble  ){
                btnGuardarNuevasTransacciones.setEnabled(true);
            }else{
                btnGuardarNuevasTransacciones.setEnabled(false);
            }
        
        });
        
        
    }
    
    
    class ControladorTablaRegistro extends AbstractTableModel{

     
        
        @Override
        public int getRowCount() {
            return (nuevosRegistros!=null )?nuevosRegistros.size():0;
        }

        @Override
        public int getColumnCount() {
            return 5;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) { 
        
        
            Registro registro = nuevosRegistros.get(rowIndex);
            
            double debe = (registro.getTipo().equals(Tipo.DEBE))?registro.getValor():0;
            double haber = (registro.getTipo().equals(Tipo.HABER))?registro.getValor():0;
            
           
           
            
            switch(columnIndex){
                case 0:
                    return registro.getFechaRegistroFormateada();
                case 1:
                    return registro.getCuenta().getNombre();
                case 2:
                    return registro.getDescripcion().toString();
                case 3:
                    return (debe==0)?"":debe;
                case 4:
                    return (haber==0)?"":haber;
                default:
                    return null;
                
            }
            
            
        }
        
        public void a??adirNuevoRegistro(Registro nuevo){
            nuevosRegistros.add(nuevo);
            double totalDebe = nuevosRegistros.stream()
                    .filter(registro -> registro.getTipo()==Tipo.DEBE)
                    .mapToDouble(registro -> registro.getValor())
                    .sum();
            
            double totalHaber = nuevosRegistros.stream()
                    .filter(registro -> registro.getTipo() == Tipo.HABER)
                    .mapToDouble(registro -> registro.getValor())
                    .sum();
            
            lblTotalDebe.setText(String.valueOf(totalDebe));
            lblTotalHaber.setText(String.valueOf(totalHaber));
            
            fireTableDataChanged();
        }
        
        public void removerTransaccion(int indice){
            nuevosRegistros.remove(indice);
            fireTableDataChanged();
        }
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnArbolDeCuentas;
    private javax.swing.JButton btnA??adirTransaccion;
    private javax.swing.JButton btnEliminarSeleccion;
    private javax.swing.JButton btnEliminarSeleccionTransaccion;
    private javax.swing.JButton btnGuardarNuevasTransacciones;
    private javax.swing.JButton btnGuardarTransaccionModificada;
    private javax.swing.ButtonGroup btngTipoTransaccion;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel lblTotalDebe;
    private javax.swing.JLabel lblTotalHaber;
    private javax.swing.JRadioButton rbtnAbono;
    private javax.swing.JRadioButton rbtnCargo;
    private javax.swing.JTable tablaRegistrosNuevos;
    private javax.swing.JCheckBox tieneIVA;
    private javax.swing.JTextField txtCuentaSeleccionada;
    private javax.swing.JTextArea txtDescripcion;
    private javax.swing.JTextField txtFecha;
    private javax.swing.JTextField txtValor;
    // End of variables declaration//GEN-END:variables
}