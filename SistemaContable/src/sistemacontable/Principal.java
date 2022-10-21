/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package sistemacontable;



import Controladores.*;

import ModeloContable.*;

import ModeloContable.Tipo;
import java.awt.Dimension;
import java.util.ArrayList;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;

import Persistencia.PersistenciaDeDatos;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.Map;
import javax.swing.JFileChooser;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;


/**
 *
 * @author pc
 */
public class Principal extends javax.swing.JFrame  implements ListSelectionListener{
    //controladores
    ControladorListadoCuentasDisponibles controladorCuentasDisp;
    ControladorTablaLibroDiario controladorTablaLibroDiario;
    ControladorTablaLibroMayor controladorTablaLibroMayor;
    
    //modelos contables
    InformacionContable informacionContable;
    LibroMayor libroMayor;
    List<Registro> asientos;
    LibroDiario libroDiario;
    List<Cuenta> cuentas;
   
    //Escritura-lectura de archivos
    final PersistenciaDeDatos persistenciaDeDatos = PersistenciaDeDatos.getPersistenciaDeDatos();
            
    
    //componentes graficos auxiliares
    JDialog inicializacionDeDatosDialog;
    
    class LectorArchivos extends SwingWorker<InformacionContable, Integer>{

        @Override
        protected InformacionContable doInBackground() throws Exception {
            return persistenciaDeDatos.recuperarDatos();
        }
        
        @Override
        protected void done(){
            
            inicializacionDeDatosDialog.dispose();
            
            try {
                informacionContable = get();
            } catch (InterruptedException | ExecutionException ex) {
                Logger.getLogger(Principal.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            if(informacionContable != null){
                libroMayor = (LibroMayor)informacionContable.getLibroMayor();
                cuentas = libroMayor.getCuentas();
                libroDiario = (LibroDiario)informacionContable.getLibroDiario();
                asientos = libroDiario.getAsientos();
            }else{
                informacionContable = new InformacionContable();
                libroMayor = new LibroMayor();
                libroDiario = new LibroDiario();
                
                
                informacionContable.setLibroMayor(libroMayor);
                informacionContable.setLibroDiario(libroDiario);
                cuentas = libroMayor.getCuentas();
                asientos = libroDiario.getAsientos();
            }
            
           
            //debe ser el ultimo metodo a llamar
            //cuentas de prueba
            
            
            ////-------------------
            /*
            cuentas.add(new Cuenta(1,"Caja",Categoria.ACTIVO));
            cuentas.add(new Cuenta(2,"Inventario",Categoria.ACTIVO));
            cuentas.add(new Cuenta(3,"Ventas",Categoria.INGRESOS));
            cuentas.add(new Cuenta(4,"Compras",Categoria.COSTOS_y_GASTOS));
            cuentas.add(new Cuenta(5,"Gastos sobre compras",Categoria.COSTOS_y_GASTOS));
            cuentas.add(new Cuenta(5,"IVA credito fiscal",Categoria.ACTIVO));
            cuentas.add(new Cuenta(6,"IVA debito fiscal",Categoria.PASIVO));
            
            
            
            try {
                persistenciaDeDatos.guardarDatos(informacionContable);
            } catch (IOException ex) {
                Logger.getLogger(Principal.class.getName()).log(Level.SEVERE, null, ex);
            }
            */
            
            
            configurarListViewCuentasDisponibles(cuentas);
            
            configurarTablaLibroDiario(asientos);
            //configurarSeleccionCuenta();
            
        }
    }

    
    class GeneradorLibroMayor extends SwingWorker<List<Cuenta>, Integer>{
        Map<Cuenta,List<Registro>> registrosAgrupadosPorCuentas ;
        
        @Override
        protected List<Cuenta> doInBackground() throws Exception {
            /*
            registrosAgrupadosPorCuentas = asientos
                    .stream()
                    .collect(Collectors.groupingBy(Registro::getCuenta));
            
           Map<Boolean,List<Registro>> registrosAgrupadosPorTipoTransaccion = asientos.stream()
                                        .collect(Collectors.partitioningBy(registro -> registro.getTipo() == Tipo.DEBE));
                
            
           cuentas.stream()
                   .map(cuenta -> {
                        cuenta.setSaldo(asientos.stream()
                                           .filter(asientos-> asientos.getCuenta().equals(cuenta))
                                           .filter(asientoCuentaActual -> asientoCuentaActual.getTipo().equals(Tipo.DEBE))
                                           .mapToDouble(asiento -> asiento.getValor())
                                           .sum()
                        );
                       return cuenta;
                   }).toList().forEach(System.out::println);
           */
            //solucion imperativa
            
            for(Cuenta cuenta: cuentas){
                double totalHaber = 0,totalDebe = 0;
                for(Registro registro: asientos){
                    if(registro.getCuenta().equals(cuenta)){
                        
                       switch(registro.getTipo()){
                           case DEBE:
                               totalDebe += registro.getValor();
                               break;
                           case HABER:
                               totalHaber += registro.getValor();
                       } 
                    } 
                }
                
                cuenta.setTotalDebe(totalDebe);
                cuenta.setTotalHaber(totalHaber);
                cuenta.setSaldo(totalDebe-totalHaber);
                if(cuenta.getSaldo()>0){
                    cuenta.setNaturaleza(Tipo.ACREEDORA);
                }
                else if(cuenta.getSaldo()<0){
                cuenta.setNaturaleza(Tipo.DEUDORA);
                }else{
                    cuenta.setNaturaleza(null);
                }
            }
            
            //cuentas.stream().filter(cuenta -> cuenta.getSaldo()!=0).forEach(System.out::println);
            return null;
        }
        
        @Override
        public void done(){
            configurarTablaLibroMayor(cuentas);
            
        }
        
    }
    /**
     * Creates new form Principal
     */
    public Principal() {
        initComponents();
        configurarSeleccionCuenta();

    }
    

    /*
    * Define el modelo que el JList utilizara para mostrar las cuentas disponibles y
    * configura los listeners necesarios
    */
    
    public void configurarListViewCuentasDisponibles(List<Cuenta> listadoCuentas){
        
        controladorCuentasDisp = new ControladorListadoCuentasDisponibles();
        
        if(listadoCuentas == null) return;
        controladorCuentasDisp.setListadoCuentas(listadoCuentas);
        
        lstCuentasDisponibles.setModel(controladorCuentasDisp);
        lstCuentasDisponibles.addListSelectionListener(this);
    }
    
    public void configurarTablaLibroDiario(List<Registro> asientos){
       int numColumnas = tablaLibroDiario.getColumnModel().getColumnCount();
       
       if(asientos == null) return;
       
       controladorTablaLibroDiario = new ControladorTablaLibroDiario(asientos);
       tablaLibroDiario.setModel(controladorTablaLibroDiario);   
       
       for(int a = 0; a< numColumnas;a++){
           if(a == 0) tablaLibroDiario.getColumnModel().getColumn(a).setHeaderValue("Fecha");
           if(a == 1) tablaLibroDiario.getColumnModel().getColumn(a).setHeaderValue("Cuenta");
           if(a == 2) tablaLibroDiario.getColumnModel().getColumn(a).setHeaderValue("Debe");
           if(a == 3) tablaLibroDiario.getColumnModel().getColumn(a).setHeaderValue("Haber");
       }
    }
    
    public void configurarSeleccionCuenta(){
        DefaultComboBoxModel<String> modeloSeleccionCuentas = new DefaultComboBoxModel<>();
        
        int cantidadCategorias = Categoria.values().length;
        String [] categorias = new String[cantidadCategorias];
        
        for(int i=0;i< cantidadCategorias;i++) categorias[i] = Categoria.values()[i].toString();
        
        modeloSeleccionCuentas.addAll(List.of(categorias));
        
        cmbSeleccionarCuenta.setModel(modeloSeleccionCuentas);
    }
    
    public void configurarTablaLibroMayor(List<Cuenta> cuentas){
          var cuentasSaldadas = cuentas.parallelStream()
                    .filter(cuenta-> cuenta.getSaldo()!=0)
                    .toList();
            controladorTablaLibroMayor = new ControladorTablaLibroMayor(cuentasSaldadas);
            
            var modeloCols = tablaLibroMayor.getColumnModel();
            tablaLibroMayor.setModel(controladorTablaLibroMayor);
            for(int i = 0; i< modeloCols.getColumnCount();i++){
                
                if(i ==0)  modeloCols.getColumn(i).setHeaderValue("Código");
                if(i ==1)  modeloCols.getColumn(i).setHeaderValue("Cuenta");
                if(i ==2)  modeloCols.getColumn(i).setHeaderValue("Debe");
                if(i ==3)  modeloCols.getColumn(i).setHeaderValue("Haber");
                if(i ==4)  modeloCols.getColumn(i).setHeaderValue("Saldo");
                if(i ==5)  modeloCols.getColumn(i).setHeaderValue("Tipo saldo");
            }
            
            
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        contenedorPestañas = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        lstCuentasDisponibles = new javax.swing.JList<>();
        btnAnadirCuenta = new javax.swing.JButton();
        btnModificarCuenta = new javax.swing.JButton();
        txtNombreCuenta = new javax.swing.JTextField();
        txtCodigoCuenta = new javax.swing.JTextField();
        jLabel51 = new javax.swing.JLabel();
        cmbSeleccionarCuenta = new javax.swing.JComboBox<>();
        btnAbrirArchivo = new javax.swing.JButton();
        btnCrearNuevoArchivoInfc = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tablaLibroDiario = new javax.swing.JTable();
        btnAnadirTransaccion = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane6 = new javax.swing.JScrollPane();
        tablaLibroMayor = new javax.swing.JTable();
        jButton6 = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        tablaBalanzaComprobacion = new javax.swing.JTable();
        btnGenerarBalanzaComprobacion = new javax.swing.JButton();
        jLabel7 = new javax.swing.JLabel();
        txtHaber = new javax.swing.JTextField();
        txtDebe = new javax.swing.JTextField();
        lblInforBalanzaComprobacion = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        tblEstadoResultado = new javax.swing.JTable();
        jScrollPane5 = new javax.swing.JScrollPane();
        tblUtilidadPerdida = new javax.swing.JTable();
        txtResultado = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jPanel7 = new javax.swing.JPanel();
        jPanel8 = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("ShieldSystem's Sistema Contable");
        setPreferredSize(new java.awt.Dimension(1000, 800));
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                onClosing(evt);
            }
        });

        contenedorPestañas.setEnabled(false);
        contenedorPestañas.setMinimumSize(new java.awt.Dimension(1000, 800));
        contenedorPestañas.setName(""); // NOI18N
        contenedorPestañas.setPreferredSize(new java.awt.Dimension(1000, 800));

        jPanel1.setMinimumSize(new java.awt.Dimension(1000, 800));
        jPanel1.setPreferredSize(new java.awt.Dimension(1000, 800));

        jLabel1.setText("Nombre de la cuenta:");

        jLabel2.setText("Código de la cuenta:");

        jLabel3.setText("Cuentas disponibles:");

        jScrollPane1.setViewportView(lstCuentasDisponibles);

        btnAnadirCuenta.setText("Añadir cuentas");
        btnAnadirCuenta.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAnadirCuentaActionPerformed(evt);
            }
        });

        btnModificarCuenta.setText("Modificar cuenta");
        btnModificarCuenta.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnModificarCuentaActionPerformed(evt);
            }
        });

        jLabel51.setText("Categoria:");

        btnAbrirArchivo.setText("Abrir archivo");
        btnAbrirArchivo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAbrirArchivoActionPerformed(evt);
            }
        });

        btnCrearNuevoArchivoInfc.setText("Crear nuevo archivo.");
        btnCrearNuevoArchivoInfc.setToolTipText("Crea un nuevo registro sino se dispone de un archivo de información contable.");
        btnCrearNuevoArchivoInfc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCrearNuevoArchivoInfcActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(btnAbrirArchivo)
                        .addGap(18, 18, 18)
                        .addComponent(btnCrearNuevoArchivoInfc))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 124, Short.MAX_VALUE)
                                .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addComponent(btnAnadirCuenta)
                            .addComponent(jLabel51, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(btnModificarCuenta)
                            .addComponent(txtNombreCuenta, javax.swing.GroupLayout.DEFAULT_SIZE, 206, Short.MAX_VALUE)
                            .addComponent(txtCodigoCuenta, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cmbSeleccionarCuenta, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(80, 80, 80)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(6, 6, 6)
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 281, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(13, 13, 13)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel3)
                    .addComponent(txtNombreCuenta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel51)
                            .addComponent(cmbSeleccionarCuenta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(10, 10, 10)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(txtCodigoCuenta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(24, 24, 24)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnAnadirCuenta)
                            .addComponent(btnModificarCuenta)))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 290, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAbrirArchivo)
                    .addComponent(btnCrearNuevoArchivoInfc))
                .addGap(17, 17, 17))
        );

        contenedorPestañas.addTab("Cuentas", jPanel1);

        jLabel4.setText("Transacciones:");

        tablaLibroDiario.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Fecha", "Cuenta", "Debe", "Haber"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Object.class, java.lang.Double.class, java.lang.Double.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        tablaLibroDiario.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tablaLibroDiario.getTableHeader().setReorderingAllowed(false);
        jScrollPane2.setViewportView(tablaLibroDiario);

        btnAnadirTransaccion.setText("Añadir asiento.");
        btnAnadirTransaccion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAnadirTransaccionActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnAnadirTransaccion)
                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 811, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 287, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(110, 110, 110)
                .addComponent(btnAnadirTransaccion)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        contenedorPestañas.addTab("Libro diario", jPanel2);

        tablaLibroMayor.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "Código", "Cuenta", "Debe", "Haber", "Saldo", "Tipo de saldo"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tablaLibroMayor.getTableHeader().setReorderingAllowed(false);
        jScrollPane6.setViewportView(tablaLibroMayor);

        jButton6.setText("Generar libro mayor");
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(29, 29, 29)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton6, javax.swing.GroupLayout.PREFERRED_SIZE, 197, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 804, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 344, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jButton6)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        contenedorPestañas.addTab("Libro mayor", jPanel3);

        tablaBalanzaComprobacion.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Código", "Cuenta", "Debe", "Haber"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.Integer.class, java.lang.Integer.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane3.setViewportView(tablaBalanzaComprobacion);

        btnGenerarBalanzaComprobacion.setText("Generar balanza de comprobación.");
        btnGenerarBalanzaComprobacion.setEnabled(false);
        btnGenerarBalanzaComprobacion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGenerarBalanzaComprobacionActionPerformed(evt);
            }
        });

        jLabel7.setText("TOTAL");

        txtHaber.setEditable(false);

        txtDebe.setEditable(false);

        lblInforBalanzaComprobacion.setText("Debe generar el libro mayor primero.");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(160, 160, 160)
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 367, Short.MAX_VALUE)
                .addComponent(txtDebe, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(txtHaber, javax.swing.GroupLayout.PREFERRED_SIZE, 209, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnGenerarBalanzaComprobacion)
                .addGap(18, 18, 18)
                .addComponent(lblInforBalanzaComprobacion)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 952, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(21, 21, 21))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(40, 40, 40)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 328, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel7)
                    .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(txtHaber, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(txtDebe, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(20, 20, 20)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnGenerarBalanzaComprobacion)
                    .addComponent(lblInforBalanzaComprobacion))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        contenedorPestañas.addTab("Balance comprobación", jPanel4);

        tblEstadoResultado.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "Codigo", "Nombre de la cuenta", "Debe", "Haber", "Saldo deudor", "Saldo acreedor"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane4.setViewportView(tblEstadoResultado);

        tblUtilidadPerdida.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Codigo", "Nombre de la cuenta", "Saldo deudor", "Saldo acreedor"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.Integer.class, java.lang.Integer.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane5.setViewportView(tblUtilidadPerdida);

        txtResultado.setEditable(false);

        jLabel6.setText("RESULTADO DE LA OPERACION:");

        jButton1.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jButton1.setText("ACTUALIZAR");

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 656, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, 331, Short.MAX_VALUE))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel5Layout.createSequentialGroup()
                                .addComponent(jLabel6)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtResultado, javax.swing.GroupLayout.PREFERRED_SIZE, 313, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 981, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 271, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(32, 32, 32)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, 67, Short.MAX_VALUE)
                    .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addGap(32, 32, 32)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(txtResultado, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(46, Short.MAX_VALUE))
        );

        contenedorPestañas.addTab("Estado de resultado", jPanel5);

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1005, Short.MAX_VALUE)
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 501, Short.MAX_VALUE)
        );

        contenedorPestañas.addTab("CIF", jPanel7);

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1005, Short.MAX_VALUE)
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 501, Short.MAX_VALUE)
        );

        contenedorPestañas.addTab("Costo total", jPanel8);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(contenedorPestañas, javax.swing.GroupLayout.DEFAULT_SIZE, 1005, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(contenedorPestañas, javax.swing.GroupLayout.PREFERRED_SIZE, 532, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnAnadirCuentaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAnadirCuentaActionPerformed
      
        int codigoCuenta = Integer.parseInt(txtCodigoCuenta.getText());
        String nombreCuenta = txtNombreCuenta.getText();
        
        int seleccion = JOptionPane.showConfirmDialog(this,"¿Desea agregar esta nueva cuenta?","Agregar cuenta nueva",JOptionPane.OK_CANCEL_OPTION,JOptionPane.INFORMATION_MESSAGE);
        
        if(seleccion == JOptionPane.OK_OPTION)
        {
            //CAMBIAR POR LA CATEGORIA ----------------------------------------------------
            Categoria categoria = Categoria.valueOf(cmbSeleccionarCuenta.getSelectedItem().toString());
            controladorCuentasDisp.añadirNuevaCuenta(new Cuenta(codigoCuenta, nombreCuenta,categoria));
            
        }
        
        limpiarTxtPestañaCuentas();
    }//GEN-LAST:event_btnAnadirCuentaActionPerformed

    private void btnAnadirTransaccionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAnadirTransaccionActionPerformed
        RegistroAsiento ra = new RegistroAsiento(controladorTablaLibroDiario,cuentas);
        

    }//GEN-LAST:event_btnAnadirTransaccionActionPerformed

    private void btnAbrirArchivoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAbrirArchivoActionPerformed
        JFileChooser seleccionadorArchivo = new JFileChooser();
        seleccionadorArchivo.setFileSelectionMode(JFileChooser.FILES_ONLY);
        seleccionadorArchivo.setFileFilter(new FileNameExtensionFilter("Informacion contable","txt"));
        int opcion = seleccionadorArchivo.showOpenDialog(this);
        LectorArchivos lectorArchivos = new LectorArchivos();
        
        
        if(opcion == JFileChooser.APPROVE_OPTION){
            persistenciaDeDatos.configurarArchivo(seleccionadorArchivo.getSelectedFile().getPath());
        }
        
               
        inicializacionDeDatosDialog = new JDialog(this);
        JPanel panelDeCarga = new JPanel();
         
        //barra de progreso
        JProgressBar barraDeProgreso = new JProgressBar();
        barraDeProgreso.setIndeterminate(true);
        
        //añadiendo la barra de progreso al panel
        panelDeCarga.add(barraDeProgreso);
        
        
        inicializacionDeDatosDialog.setSize(new Dimension(400,400));
        inicializacionDeDatosDialog.setContentPane(panelDeCarga);
        inicializacionDeDatosDialog.pack();
        //centra el cuadro de carga
        inicializacionDeDatosDialog.setLocationRelativeTo(null);
        inicializacionDeDatosDialog.setVisible(true);
        
        lectorArchivos.execute();
        contenedorPestañas.setEnabled(true);
    }//GEN-LAST:event_btnAbrirArchivoActionPerformed

    private void onClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_onClosing
         if(persistenciaDeDatos == null || informacionContable == null) return;
        
        try {
            persistenciaDeDatos.guardarDatos(informacionContable);
        } catch (IOException ex) {
            Logger.getLogger(Principal.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }//GEN-LAST:event_onClosing

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
       GeneradorLibroMayor generadorLibroMayor = new GeneradorLibroMayor();
       generadorLibroMayor.execute();
       btnGenerarBalanzaComprobacion.setEnabled(true);
       lblInforBalanzaComprobacion.setVisible(false);
    }//GEN-LAST:event_jButton6ActionPerformed

    private void btnCrearNuevoArchivoInfcActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCrearNuevoArchivoInfcActionPerformed
    
        JFileChooser seleccionadorRutaAlmacenamiento = new JFileChooser();
        seleccionadorRutaAlmacenamiento.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        seleccionadorRutaAlmacenamiento.setDialogTitle("Seleccione la ruta de almacenamiento.");
        seleccionadorRutaAlmacenamiento.setApproveButtonText("Aceptar");
        int seleccion = seleccionadorRutaAlmacenamiento.showSaveDialog(this);
        
        if(seleccion == JFileChooser.APPROVE_OPTION){
            String ruta = seleccionadorRutaAlmacenamiento.getSelectedFile().getAbsolutePath();
            String nombreArchivo = JOptionPane.showInputDialog(this,"Ingrese el nombre del archivo.");
            persistenciaDeDatos.configurarArchivoNuevoParaAlmacenar(ruta, nombreArchivo);
            
            contenedorPestañas.setEnabled(true);
        }
        
        

    }//GEN-LAST:event_btnCrearNuevoArchivoInfcActionPerformed

    private void btnGenerarBalanzaComprobacionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGenerarBalanzaComprobacionActionPerformed
        var cuentasSaldoReal = cuentas.stream()
                .filter(cuenta -> cuenta.getSaldo()!= 0)
                .toList();
        ControladorTablaBalanzaComprobacion controladorTablaBalanzaComprobacion = new ControladorTablaBalanzaComprobacion(cuentasSaldoReal);
        tablaBalanzaComprobacion.setModel(controladorTablaBalanzaComprobacion);
        
        var colModel = tablaBalanzaComprobacion.getColumnModel();
        
        for(int j=0;j<colModel.getColumnCount();j++){
            if(j==0) colModel.getColumn(j).setHeaderValue("Código");
            if(j==1) colModel.getColumn(j).setHeaderValue("Cuenta");
            if(j==2) colModel.getColumn(j).setHeaderValue("Debe");
            if(j==3) colModel.getColumn(j).setHeaderValue("Haber");
        }
        
        double totalDebeBalanzaComprobacion = cuentasSaldoReal.stream()
                                                .filter(cuenta -> cuenta.getSaldo()> 0)
                                                .mapToDouble(Cuenta::getSaldo)
                                                .sum();
        double totalHaberBalanzaComprobacion = cuentasSaldoReal.stream()
                                                .filter(cuenta -> cuenta.getSaldo()< 0)
                                                .mapToDouble(Cuenta::getSaldo)
                                                .sum();
      
    txtDebe.setText(String.valueOf(totalDebeBalanzaComprobacion));
    txtHaber.setText(String.valueOf(Math.abs(totalHaberBalanzaComprobacion)));
    }//GEN-LAST:event_btnGenerarBalanzaComprobacionActionPerformed

    private void btnModificarCuentaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnModificarCuentaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnModificarCuentaActionPerformed

    //metodos utilitarios
    public void limpiarTxtPestañaCuentas(){
        txtCodigoCuenta.setText("");
        txtNombreCuenta.setText("");
    }
    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Metal".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Principal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Principal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Principal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Principal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Principal().setVisible(true);
               
            }
        });
        
    }
    
    

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAbrirArchivo;
    private javax.swing.JButton btnAnadirCuenta;
    private javax.swing.JButton btnAnadirTransaccion;
    private javax.swing.JButton btnCrearNuevoArchivoInfc;
    private javax.swing.JButton btnGenerarBalanzaComprobacion;
    private javax.swing.JButton btnModificarCuenta;
    private javax.swing.JComboBox<String> cmbSeleccionarCuenta;
    private javax.swing.JTabbedPane contenedorPestañas;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton6;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel51;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JLabel lblInforBalanzaComprobacion;
    private javax.swing.JList<String> lstCuentasDisponibles;
    private javax.swing.JTable tablaBalanzaComprobacion;
    private javax.swing.JTable tablaLibroDiario;
    private javax.swing.JTable tablaLibroMayor;
    private javax.swing.JTable tblEstadoResultado;
    private javax.swing.JTable tblUtilidadPerdida;
    private javax.swing.JTextField txtCodigoCuenta;
    private javax.swing.JTextField txtDebe;
    private javax.swing.JTextField txtHaber;
    private javax.swing.JTextField txtNombreCuenta;
    private javax.swing.JTextField txtResultado;
    // End of variables declaration//GEN-END:variables

    @Override
    public void valueChanged(ListSelectionEvent e) {
        JList<String> listadoCuentas = (JList) e.getSource();
        int indiceCuentaSeleccionada = listadoCuentas.getSelectedIndex();
        int codigoCuentaSeleccionada = controladorCuentasDisp.getListadoCuentas().get(indiceCuentaSeleccionada).getCodCuenta();
        
        txtCodigoCuenta.setText(String.valueOf(codigoCuentaSeleccionada));
        cmbSeleccionarCuenta.setSelectedItem(cuentas.get(indiceCuentaSeleccionada).getCategoria().toString());
        txtNombreCuenta.setText(listadoCuentas.getSelectedValue());
    }
}