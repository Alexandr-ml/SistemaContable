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
import java.awt.Color;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.Map;
import static java.util.stream.Collectors.partitioningBy;
import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;


/**
 *
 * @author pc
 */
public class Principal extends javax.swing.JFrame  implements ListSelectionListener{
    //controladores
    ControladorListadoCuentasDisponibles controladorCuentasDisp = new ControladorListadoCuentasDisponibles();
    ControladorTablaLibroDiario controladorTablaLibroDiario = new ControladorTablaLibroDiario();
    ControladorTablaLibroMayor controladorTablaLibroMayor;
    ControladorTablaEstadoResultado controladorTablaEstadoResultado;
    //modelos contables
    InformacionContable informacionContable;
    LibroMayor libroMayor;
    List<Registro> asientos;
    LibroDiario libroDiario;
    List<Cuenta> cuentas;
    List<Cuenta> cuentasSaldadas;
    
    List<Cuenta> listadoCuentasIngresoGastos;
   
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
                return;
                /*
                informacionContable = new InformacionContable();
                libroMayor = new LibroMayor();
                libroDiario = new LibroDiario();
                
                
                informacionContable.setLibroMayor(libroMayor);
                informacionContable.setLibroDiario(libroDiario);
                cuentas = libroMayor.getCuentas();
                System.out.println(cuentas.isEmpty());
                asientos = libroDiario.getAsientos();
                */
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
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        txtCodigoCuenta.setInputVerifier(new InputVerifier(){
            @Override
            public boolean verify(JComponent input) {
                JTextField origen = (JTextField)input;
                
                boolean esEntero = false;
                
                try {
                   origen.getText().transform(Integer::parseInt);
                   esEntero = true;
                }catch(NumberFormatException e){
                    JOptionPane.showMessageDialog(null,"Ingrese un código numérico","Ingrese un código de cuenta valido!",JOptionPane.ERROR_MESSAGE);
                }
                
               return esEntero; 
            }
        });
    }
    
    
    //Metodo aux
                                                       
    public double convierte(float porcentaje){
        double numero;
        
        numero = porcentaje/100;
        return numero;
    }

    /*
    * Define el modelo que el JList utilizara para mostrar las cuentas disponibles y
    * configura los listeners necesarios
    */
    
    
    public void configurarListViewCuentasDisponibles(List<Cuenta> listadoCuentas){
        
        
        
        if(listadoCuentas == null) return;
        controladorCuentasDisp.setListadoCuentas(listadoCuentas);
        
        lstCuentasDisponibles.setModel(controladorCuentasDisp);
        lstCuentasDisponibles.addListSelectionListener(this);
    }
    
    public void configurarTablaLibroDiario(List<Registro> asientos){
       int numColumnas = tablaLibroDiario.getColumnModel().getColumnCount();
       
       if(asientos == null) return;
       
       controladorTablaLibroDiario.setDatos(asientos);
       
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
        System.out.println(cuentas);
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
        btnOlvidarSeleccionCuenta = new javax.swing.JButton();
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
        tablaEstadoResultado = new javax.swing.JTable();
        jLabel6 = new javax.swing.JLabel();
        txtUtilidadesPerdidas = new javax.swing.JTextField();
        btnGenerarEstadoResultado = new javax.swing.JButton();
        lblResultadoGastosVsIngresos = new javax.swing.JLabel();
        btnCalculoUtilidadesPerdidas = new javax.swing.JButton();
        lblInfoEstadoResultado = new javax.swing.JLabel();
        jScrollPane8 = new javax.swing.JScrollPane();
        jPanel14 = new javax.swing.JPanel();
        jLabel26 = new javax.swing.JLabel();
        jPanel15 = new javax.swing.JPanel();
        jLabel27 = new javax.swing.JLabel();
        jLabel28 = new javax.swing.JLabel();
        jLabel29 = new javax.swing.JLabel();
        jLabel30 = new javax.swing.JLabel();
        jLabel31 = new javax.swing.JLabel();
        jLabel32 = new javax.swing.JLabel();
        jLabel33 = new javax.swing.JLabel();
        jLabel34 = new javax.swing.JLabel();
        jPanel16 = new javax.swing.JPanel();
        txtSeptimo2 = new javax.swing.JTextField();
        txtAguinaldo2 = new javax.swing.JTextField();
        txtVacacion2 = new javax.swing.JTextField();
        txtSalud = new javax.swing.JTextField();
        txtAFP = new javax.swing.JTextField();
        txtINSAFORP = new javax.swing.JTextField();
        jPanel19 = new javax.swing.JPanel();
        jLabel35 = new javax.swing.JLabel();
        jLabel36 = new javax.swing.JLabel();
        jLabel37 = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        txtTotalHora = new javax.swing.JTextField();
        txtTotalDia = new javax.swing.JTextField();
        txtTotal = new javax.swing.JTextField();
        jPanel23 = new javax.swing.JPanel();
        jLabel38 = new javax.swing.JLabel();
        jLabel39 = new javax.swing.JLabel();
        jLabel40 = new javax.swing.JLabel();
        jLabel41 = new javax.swing.JLabel();
        jLabel42 = new javax.swing.JLabel();
        jPanel24 = new javax.swing.JPanel();
        txtSemana = new javax.swing.JTextField();
        txtHora = new javax.swing.JTextField();
        txtDia = new javax.swing.JTextField();
        txtSalarioDia = new javax.swing.JTextField();
        txtSalarioHora = new javax.swing.JTextField();
        txtSalarioSemana = new javax.swing.JTextField();
        txtFactorHora = new javax.swing.JTextField();
        txtFactorDia = new javax.swing.JTextField();
        txtFactorSemana = new javax.swing.JTextField();
        jLabel43 = new javax.swing.JLabel();
        jLabel44 = new javax.swing.JLabel();
        jLabel45 = new javax.swing.JLabel();
        jPanel25 = new javax.swing.JPanel();
        jLabel46 = new javax.swing.JLabel();
        jLabel47 = new javax.swing.JLabel();
        jLabel48 = new javax.swing.JLabel();
        jLabel49 = new javax.swing.JLabel();
        jLabel50 = new javax.swing.JLabel();
        jPanel26 = new javax.swing.JPanel();
        txtSemanaEficiencia = new javax.swing.JTextField();
        txtHoraEficiencia = new javax.swing.JTextField();
        txtDiaEficiencia = new javax.swing.JTextField();
        txtSalarioDiaEficiencia = new javax.swing.JTextField();
        txtSalarioHoraEficiencia = new javax.swing.JTextField();
        txtSalarioSemanaEficiencia = new javax.swing.JTextField();
        txtFactorHoraEficiencia = new javax.swing.JTextField();
        txtFactorDiaEficiencia = new javax.swing.JTextField();
        txtFactorSemanaEficiencia = new javax.swing.JTextField();
        jLabel74 = new javax.swing.JLabel();
        jLabel75 = new javax.swing.JLabel();
        jLabel76 = new javax.swing.JLabel();
        btnCalcularManoDeObra = new javax.swing.JButton();
        btnLimpiar = new javax.swing.JButton();
        jLabel77 = new javax.swing.JLabel();
        jLabel78 = new javax.swing.JLabel();
        jLabel138 = new javax.swing.JLabel();
        jLabel139 = new javax.swing.JLabel();
        jLabel140 = new javax.swing.JLabel();
        txtSalario = new javax.swing.JTextField();
        txtHoras = new javax.swing.JTextField();
        txtDiasAguinaldo = new javax.swing.JTextField();
        txtDiasVacacion = new javax.swing.JTextField();
        txtDiasTrabajado = new javax.swing.JTextField();
        jLabel141 = new javax.swing.JLabel();
        jLabel142 = new javax.swing.JLabel();
        jLabel143 = new javax.swing.JLabel();
        jLabel144 = new javax.swing.JLabel();
        txtPorcentajeVacacion = new javax.swing.JTextField();
        txtPorcentajeSeguro = new javax.swing.JTextField();
        txtPorcentajeEficiencia = new javax.swing.JTextField();
        txtPorcentajeAFP = new javax.swing.JTextField();
        jLabel145 = new javax.swing.JLabel();
        txtPorcentajeINSAFORP = new javax.swing.JTextField();
        jScrollPane5 = new javax.swing.JScrollPane();
        jPanel9 = new javax.swing.JPanel();
        jPanel30 = new javax.swing.JPanel();
        jLabel173 = new javax.swing.JLabel();
        jLabel174 = new javax.swing.JLabel();
        txtSalariosSup2 = new javax.swing.JTextField();
        txtSalariosOfi2 = new javax.swing.JTextField();
        jLabel175 = new javax.swing.JLabel();
        jLabel176 = new javax.swing.JLabel();
        jLabel177 = new javax.swing.JLabel();
        jLabel178 = new javax.swing.JLabel();
        txtManoDeObraIndirecta2 = new javax.swing.JTextField();
        jLabel179 = new javax.swing.JLabel();
        jLabel180 = new javax.swing.JLabel();
        txtMaterialesIndirectos2 = new javax.swing.JTextField();
        jLabel181 = new javax.swing.JLabel();
        jLabel182 = new javax.swing.JLabel();
        txtOtrosMateriales2 = new javax.swing.JTextField();
        jLabel183 = new javax.swing.JLabel();
        jLabel184 = new javax.swing.JLabel();
        jLabel185 = new javax.swing.JLabel();
        txtDepreciacion2 = new javax.swing.JTextField();
        jLabel186 = new javax.swing.JLabel();
        txtSuministros2 = new javax.swing.JTextField();
        jLabel187 = new javax.swing.JLabel();
        jLabel188 = new javax.swing.JLabel();
        txtHerramientas2 = new javax.swing.JTextField();
        txtImpuestos2 = new javax.swing.JTextField();
        jLabel189 = new javax.swing.JLabel();
        txtServiciosPublicos2 = new javax.swing.JTextField();
        jLabel190 = new javax.swing.JLabel();
        jLabel191 = new javax.swing.JLabel();
        txtServiciosPrivados2 = new javax.swing.JTextField();
        jLabel192 = new javax.swing.JLabel();
        jLabel193 = new javax.swing.JLabel();
        jLabel194 = new javax.swing.JLabel();
        jLabel195 = new javax.swing.JLabel();
        txtTotales2 = new javax.swing.JTextField();
        btnLimpia = new javax.swing.JButton();
        btnGuardar = new javax.swing.JButton();
        jPanel18 = new javax.swing.JPanel();
        jLabel79 = new javax.swing.JLabel();
        jLabel80 = new javax.swing.JLabel();
        jLabel81 = new javax.swing.JLabel();
        jLabel82 = new javax.swing.JLabel();
        jLabel83 = new javax.swing.JLabel();
        txtAnalisisEspacio = new javax.swing.JTextField();
        txtAnalisisConsumo = new javax.swing.JTextField();
        txtAnalisisEmpleado = new javax.swing.JTextField();
        txtAnalisisHora = new javax.swing.JTextField();
        txtDisenoHora = new javax.swing.JTextField();
        txtDisenoEspacio = new javax.swing.JTextField();
        txtDisenoConsumo = new javax.swing.JTextField();
        txtDisenoEmpleado = new javax.swing.JTextField();
        txtDesarrolloHora = new javax.swing.JTextField();
        txtDesarrolloEspacio = new javax.swing.JTextField();
        txtDesarrolloConsumo = new javax.swing.JTextField();
        txtDesarrolloEmpleado = new javax.swing.JTextField();
        txtPruebaHora = new javax.swing.JTextField();
        txtPruebaEspacio = new javax.swing.JTextField();
        txtPruebaConsumo = new javax.swing.JTextField();
        txtPruebaEmpleado = new javax.swing.JTextField();
        txtMantenimientoHora = new javax.swing.JTextField();
        txtMantenimientoEspacio = new javax.swing.JTextField();
        txtMantenimientoConsumo = new javax.swing.JTextField();
        txtMantenimientoEmpleado = new javax.swing.JTextField();
        jLabel84 = new javax.swing.JLabel();
        jLabel85 = new javax.swing.JLabel();
        txtCalidadConsumo = new javax.swing.JTextField();
        txtCalidadEmpleado = new javax.swing.JTextField();
        txtInsumoHora = new javax.swing.JTextField();
        txtInsumoEspacio = new javax.swing.JTextField();
        txtInsumoConsumo = new javax.swing.JTextField();
        txtInsumoEmpleado = new javax.swing.JTextField();
        txtMantenimientoSoftHora = new javax.swing.JTextField();
        txtMantenimientoSoftEspacio = new javax.swing.JTextField();
        txtMantenimientoSoftConsumo = new javax.swing.JTextField();
        txtMantenimientoSoftEmpleado = new javax.swing.JTextField();
        jLabel86 = new javax.swing.JLabel();
        jLabel87 = new javax.swing.JLabel();
        jLabel88 = new javax.swing.JLabel();
        jLabel89 = new javax.swing.JLabel();
        txtCalidadHora = new javax.swing.JTextField();
        txtCalidadEspacio = new javax.swing.JTextField();
        txtMantenimientoServicioHora = new javax.swing.JTextField();
        txtMantenimientoServicioEspacio = new javax.swing.JTextField();
        txtMantenimientoServicioConsumo = new javax.swing.JTextField();
        txtMantenimientoServicioEmpleado = new javax.swing.JTextField();
        jLabel90 = new javax.swing.JLabel();
        txtTotalHoraHombre = new javax.swing.JTextField();
        txtTotalEspacio = new javax.swing.JTextField();
        txtTotalConsumo = new javax.swing.JTextField();
        txtTotalEmpleado = new javax.swing.JTextField();
        jScrollPane7 = new javax.swing.JScrollPane();
        jPanel20 = new javax.swing.JPanel();
        jLabel92 = new javax.swing.JLabel();
        jLabel93 = new javax.swing.JLabel();
        jPanel21 = new javax.swing.JPanel();
        jLabel94 = new javax.swing.JLabel();
        jLabel95 = new javax.swing.JLabel();
        jLabel96 = new javax.swing.JLabel();
        txtSalarioSupAnalisisProductivo = new javax.swing.JTextField();
        txtSalarioOfiAnalisisProductivo = new javax.swing.JTextField();
        txtManoAnalisisProductivo = new javax.swing.JTextField();
        txtMaterialAnalisisProductivo = new javax.swing.JTextField();
        txtSumiAnalisisProductivo = new javax.swing.JTextField();
        txtHerraAnalisisProductivo = new javax.swing.JTextField();
        txtOtrosAnalisisProductivo = new javax.swing.JTextField();
        txtDepreciacionAnalisisProductivo = new javax.swing.JTextField();
        txtImpuestosAnalisisProductivo = new javax.swing.JTextField();
        txtServicioPublicoAnalisisProductivo = new javax.swing.JTextField();
        txtServicioPrivadoAnalisisProductivo = new javax.swing.JTextField();
        txtCalidadAnalisisProductivo = new javax.swing.JTextField();
        txtInsumoAnalisisProductivo = new javax.swing.JTextField();
        txtMantenimientoSoftAnalisisProductivo = new javax.swing.JTextField();
        txtInfraestructuraAnalisisProductivo = new javax.swing.JTextField();
        txtImpuestosDisenoProductivo = new javax.swing.JTextField();
        txtSalarioSupDisenoProductivo = new javax.swing.JTextField();
        txtServicioPublicoDisenoProductivo = new javax.swing.JTextField();
        txtSalarioOfiDisenoProductivo = new javax.swing.JTextField();
        txtServicioPrivadoDisenoProductivo = new javax.swing.JTextField();
        txtManoDisenoProductivo = new javax.swing.JTextField();
        txtMaterialDisenoProductivo = new javax.swing.JTextField();
        txtCalidadDisenoProductivo = new javax.swing.JTextField();
        txtSumiDisenoProductivo = new javax.swing.JTextField();
        txtInsumoDisenoProductivo = new javax.swing.JTextField();
        txtHerraDisenoProductivo = new javax.swing.JTextField();
        txtMantenimientoSoftDisenoProductivo = new javax.swing.JTextField();
        txtOtrosDisenoProductivo = new javax.swing.JTextField();
        txtInfraestructuraDisenoProductivo = new javax.swing.JTextField();
        txtDepreciacionDisenoProductivo = new javax.swing.JTextField();
        txtImpuestosDesarrolloProductivo = new javax.swing.JTextField();
        txtSalarioSupDesarrolloProductivo = new javax.swing.JTextField();
        txtServicioPublicoDesarrolloProductivo = new javax.swing.JTextField();
        txtSalarioOfiDesarrolloProductivo = new javax.swing.JTextField();
        txtServicioPrivadoDesarrolloProductivo = new javax.swing.JTextField();
        txtManoDesarrolloProductivo = new javax.swing.JTextField();
        txtMaterialDesarrolloProductivo = new javax.swing.JTextField();
        txtCalidadDesarrolloProductivo = new javax.swing.JTextField();
        txtSumiDesarrolloProductivo = new javax.swing.JTextField();
        txtInsumoDesarrolloProductivo = new javax.swing.JTextField();
        txtHerraDesarrolloProductivo = new javax.swing.JTextField();
        txtMantenimientoSoftDesarrolloProductivo = new javax.swing.JTextField();
        txtOtrosDesarrolloProductivo = new javax.swing.JTextField();
        txtInfraestructuraDesarrolloProductivo = new javax.swing.JTextField();
        txtDepreciacionDesarrolloProductivo = new javax.swing.JTextField();
        jLabel112 = new javax.swing.JLabel();
        txtServicioPublicoPruebaProductivo = new javax.swing.JTextField();
        txtSalarioOfiPruebaProductivo = new javax.swing.JTextField();
        txtServicioPrivadoPruebaProductivo = new javax.swing.JTextField();
        txtManoPruebaProductivo = new javax.swing.JTextField();
        txtMaterialPruebaProductivo = new javax.swing.JTextField();
        txtCalidadPruebaProductivo = new javax.swing.JTextField();
        txtSumiPruebaProductivo = new javax.swing.JTextField();
        txtInsumoPruebaProductivo = new javax.swing.JTextField();
        txtHerraPruebaProductivo = new javax.swing.JTextField();
        txtMantenimientoSoftPruebaProductivo = new javax.swing.JTextField();
        txtOtrosPruebaProductivo = new javax.swing.JTextField();
        txtInfraestructuraPruebaProductivo = new javax.swing.JTextField();
        txtDepreciacionPruebaProductivo = new javax.swing.JTextField();
        txtImpuestosPruebaProductivo = new javax.swing.JTextField();
        txtSalarioSupPruebaProductivo = new javax.swing.JTextField();
        txtServicioPublicoMantenimientoProductivo = new javax.swing.JTextField();
        txtSalarioOfiMantenimientoProductivo = new javax.swing.JTextField();
        txtServicioPrivadoMantenimientoProductivo = new javax.swing.JTextField();
        txtManoMantenimientoProductivo = new javax.swing.JTextField();
        txtMaterialMantenimientoProductivo = new javax.swing.JTextField();
        txtCalidadMantenimientoProductivo = new javax.swing.JTextField();
        txtSumiMantenimientoProductivo = new javax.swing.JTextField();
        txtInsumoMantenimientoProductivo = new javax.swing.JTextField();
        txtHerraMantenimientoProductivo = new javax.swing.JTextField();
        txtMantenimientoSoftMantenimientoProductivo = new javax.swing.JTextField();
        txtOtrosMantenimientoProductivo = new javax.swing.JTextField();
        txtInfraestructuraMantenimientoProductivo = new javax.swing.JTextField();
        txtDepreciacionMantenimientoProductivo = new javax.swing.JTextField();
        txtImpuestosMantenimientoProductivo = new javax.swing.JTextField();
        txtSalarioSupMantenimientoProductivo = new javax.swing.JTextField();
        jLabel113 = new javax.swing.JLabel();
        jLabel117 = new javax.swing.JLabel();
        txtTotalAnalisis = new javax.swing.JTextField();
        txtTotalDiseno = new javax.swing.JTextField();
        txtTotalDesarrollo = new javax.swing.JTextField();
        txtTotalPrueba = new javax.swing.JTextField();
        txtTotalMantenimiento = new javax.swing.JTextField();
        jLabel97 = new javax.swing.JLabel();
        jLabel98 = new javax.swing.JLabel();
        jLabel99 = new javax.swing.JLabel();
        jLabel100 = new javax.swing.JLabel();
        jLabel101 = new javax.swing.JLabel();
        jLabel102 = new javax.swing.JLabel();
        jLabel103 = new javax.swing.JLabel();
        jLabel104 = new javax.swing.JLabel();
        jLabel105 = new javax.swing.JLabel();
        jLabel106 = new javax.swing.JLabel();
        jLabel107 = new javax.swing.JLabel();
        jLabel108 = new javax.swing.JLabel();
        jLabel109 = new javax.swing.JLabel();
        jLabel110 = new javax.swing.JLabel();
        jLabel111 = new javax.swing.JLabel();
        jLabel114 = new javax.swing.JLabel();
        jLabel115 = new javax.swing.JLabel();
        jLabel116 = new javax.swing.JLabel();
        jLabel120 = new javax.swing.JLabel();
        jLabel121 = new javax.swing.JLabel();
        jLabel122 = new javax.swing.JLabel();
        jLabel123 = new javax.swing.JLabel();
        jLabel124 = new javax.swing.JLabel();
        jLabel126 = new javax.swing.JLabel();
        jLabel127 = new javax.swing.JLabel();
        jLabel125 = new javax.swing.JLabel();
        jLabel119 = new javax.swing.JLabel();
        jLabel128 = new javax.swing.JLabel();
        jLabel129 = new javax.swing.JLabel();
        jLabel130 = new javax.swing.JLabel();
        jLabel131 = new javax.swing.JLabel();
        jLabel132 = new javax.swing.JLabel();
        jPanel22 = new javax.swing.JPanel();
        jLabel118 = new javax.swing.JLabel();
        jLabel133 = new javax.swing.JLabel();
        jLabel134 = new javax.swing.JLabel();
        txtSalarioSupCalidadServicio = new javax.swing.JTextField();
        txtSalarioOfiCalidadServicio = new javax.swing.JTextField();
        txtManoCalidadServicio = new javax.swing.JTextField();
        txtMaterialCalidadServicio = new javax.swing.JTextField();
        txtSumiCalidadServicio = new javax.swing.JTextField();
        txtHerraCalidadServicio = new javax.swing.JTextField();
        txtOtrosCalidadServicio = new javax.swing.JTextField();
        txtDepreciacionCalidadServicio = new javax.swing.JTextField();
        txtImpuestosCalidadServicio = new javax.swing.JTextField();
        txtServicioPublicoCalidadServicio = new javax.swing.JTextField();
        txtServicioPrivadoCalidadServicio = new javax.swing.JTextField();
        txtCalidadCalidadServicio = new javax.swing.JTextField();
        txtInsumoCalidadServicio = new javax.swing.JTextField();
        txtMantenimientoSoftCalidadServicio = new javax.swing.JTextField();
        txtInfraestructuraCalidadServicio = new javax.swing.JTextField();
        txtImpuestosInsumoServicio = new javax.swing.JTextField();
        txtSalarioSupInsumoServicio = new javax.swing.JTextField();
        txtServicioPublicoInsumoServicio = new javax.swing.JTextField();
        txtSalarioOfiInsumosServicio = new javax.swing.JTextField();
        txtServicioPrivadoInsumoServicio = new javax.swing.JTextField();
        txtManoInsumoServicio = new javax.swing.JTextField();
        txtMaterialInsumoServicio = new javax.swing.JTextField();
        txtCalidadInsumoServicio = new javax.swing.JTextField();
        txtSumiInsumoServicio = new javax.swing.JTextField();
        txtInsumoInsumoServicio = new javax.swing.JTextField();
        txtHerraInsumoServicio = new javax.swing.JTextField();
        txtMantenimientoSoftInsumoServicio = new javax.swing.JTextField();
        txtOtrosInsumoServicio = new javax.swing.JTextField();
        txtInfraestructuraInsumoServicio = new javax.swing.JTextField();
        txtDepreciacionInsumoServicio = new javax.swing.JTextField();
        txtImpuestosMantenimientoSoftServicio = new javax.swing.JTextField();
        txtSalarioSupMantenimientoSoftServicio = new javax.swing.JTextField();
        txtServicioPublicoMantenimientoSoftServicio = new javax.swing.JTextField();
        txtSalarioOfiMantenimientoSoftServicio = new javax.swing.JTextField();
        txtServicioPrivadoMantenimientoSoftServicio = new javax.swing.JTextField();
        txtManoMantenimientoSoftServicio = new javax.swing.JTextField();
        txtMaterialMantenimientoSoftServicio = new javax.swing.JTextField();
        txtCalidadMantenimientoSoftServicio = new javax.swing.JTextField();
        txtSumiMantenimientoSoftServicio = new javax.swing.JTextField();
        txtInsumoMantenimientoSoftServicio = new javax.swing.JTextField();
        txtHerraMantenimientoSoftServicio = new javax.swing.JTextField();
        txtMantenimientoSoftMantenimientoSoftServicio = new javax.swing.JTextField();
        txtOtrosMantenimientoSoftServicio = new javax.swing.JTextField();
        txtInfraestructuraMantenimientoSoftServicio = new javax.swing.JTextField();
        txtDepreciacionMantenimientoSoftServicio = new javax.swing.JTextField();
        jLabel135 = new javax.swing.JLabel();
        txtServicioPublicoInfraestructuraServicio = new javax.swing.JTextField();
        txtSalarioOfiInfraestructuraServicio = new javax.swing.JTextField();
        txtServicioPrivadoInfraestructuraServicio = new javax.swing.JTextField();
        txtManoInfraestructuraServicio = new javax.swing.JTextField();
        txtMaterialInfraestructuraServicio = new javax.swing.JTextField();
        txtCalidadInfraestructuraServicio = new javax.swing.JTextField();
        txtSumiInfraestructuraServicio = new javax.swing.JTextField();
        txtInsumoInfraestructuraServicio = new javax.swing.JTextField();
        txtHerraInfraestructuraServicio = new javax.swing.JTextField();
        txtMantenimientoSoftInfraestructuraServicio = new javax.swing.JTextField();
        txtOtrosInfraestructuraServicio = new javax.swing.JTextField();
        txtInfraestructuraInfraestructuraServicio = new javax.swing.JTextField();
        txtDepreciacionInfraestructuraServicio = new javax.swing.JTextField();
        txtImpuestosInfraestructuraServicio = new javax.swing.JTextField();
        txtSalarioSupInfraestructuraServicio = new javax.swing.JTextField();
        txtTotalServicioPublico = new javax.swing.JTextField();
        txtTotalSalarioOfi = new javax.swing.JTextField();
        txtTotalServicioPrivado = new javax.swing.JTextField();
        txtTotalMano = new javax.swing.JTextField();
        txtTotalMaterial = new javax.swing.JTextField();
        txtTotalCalidad = new javax.swing.JTextField();
        txtTotalSumi = new javax.swing.JTextField();
        txtTotalInsumo = new javax.swing.JTextField();
        txtTotalHerra = new javax.swing.JTextField();
        txtTotalMantenimientoSoft = new javax.swing.JTextField();
        txtTotalOtros = new javax.swing.JTextField();
        txtTotalInfraestructura = new javax.swing.JTextField();
        txtTotalDepreciacion = new javax.swing.JTextField();
        txtTotalImpuesto = new javax.swing.JTextField();
        txtTotalSalarioSup = new javax.swing.JTextField();
        jLabel136 = new javax.swing.JLabel();
        jLabel137 = new javax.swing.JLabel();
        txtTotalPresupuesto = new javax.swing.JTextField();
        txtTotalVCalidad = new javax.swing.JTextField();
        txtTotalVInsumo = new javax.swing.JTextField();
        txtTotalTotal = new javax.swing.JTextField();
        txtTotalVInfraestructura = new javax.swing.JTextField();
        txtTotalVMantenimientoSoft = new javax.swing.JTextField();
        jPanel27 = new javax.swing.JPanel();
        jPanel28 = new javax.swing.JPanel();
        jLabel146 = new javax.swing.JLabel();
        jLabel147 = new javax.swing.JLabel();
        jLabel148 = new javax.swing.JLabel();
        txtManoDeObra = new javax.swing.JTextField();
        txtCostoTotal = new javax.swing.JTextField();
        txtCIF = new javax.swing.JTextField();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jLabel149 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("ShieldSystem's Sistema Contable");
        setPreferredSize(new java.awt.Dimension(900, 750));
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

        lstCuentasDisponibles.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jScrollPane1.setViewportView(lstCuentasDisponibles);

        btnAnadirCuenta.setText("Añadir cuentas");
        btnAnadirCuenta.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED), new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED)));
        btnAnadirCuenta.setEnabled(false);
        btnAnadirCuenta.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAnadirCuentaActionPerformed(evt);
            }
        });

        btnModificarCuenta.setText("Modificar cuenta");
        btnModificarCuenta.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED), new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED)));
        btnModificarCuenta.setEnabled(false);
        btnModificarCuenta.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnModificarCuentaActionPerformed(evt);
            }
        });

        txtNombreCuenta.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEtchedBorder(), new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED)));

        txtCodigoCuenta.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEtchedBorder(), new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED)));

        jLabel51.setText("Categoria:");

        cmbSeleccionarCuenta.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEtchedBorder(), new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED)));

        btnAbrirArchivo.setBackground(new java.awt.Color(204, 204, 204));
        btnAbrirArchivo.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        btnAbrirArchivo.setForeground(new java.awt.Color(51, 51, 51));
        btnAbrirArchivo.setText("Abrir archivo");
        btnAbrirArchivo.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED), new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED)));
        btnAbrirArchivo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAbrirArchivoActionPerformed(evt);
            }
        });

        btnCrearNuevoArchivoInfc.setBackground(new java.awt.Color(204, 204, 204));
        btnCrearNuevoArchivoInfc.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        btnCrearNuevoArchivoInfc.setForeground(new java.awt.Color(51, 51, 51));
        btnCrearNuevoArchivoInfc.setText("Crear nuevo archivo.");
        btnCrearNuevoArchivoInfc.setToolTipText("Crea un nuevo registro sino se dispone de un archivo de información contable.");
        btnCrearNuevoArchivoInfc.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED), new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED)));
        btnCrearNuevoArchivoInfc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCrearNuevoArchivoInfcActionPerformed(evt);
            }
        });

        btnOlvidarSeleccionCuenta.setText("Olvidar selección");
        btnOlvidarSeleccionCuenta.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED), new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED)));
        btnOlvidarSeleccionCuenta.setEnabled(false);
        btnOlvidarSeleccionCuenta.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnOlvidarSeleccionCuentaActionPerformed(evt);
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
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabel51, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(btnAnadirCuenta, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGap(18, 18, 18)
                                        .addComponent(btnOlvidarSeleccionCuenta, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGap(18, 18, 18)
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addGap(6, 6, 6)
                                                .addComponent(txtCodigoCuenta, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                .addComponent(txtNombreCuenta)
                                                .addComponent(cmbSeleccionarCuenta, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE))))))
                            .addComponent(btnModificarCuenta, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(68, 68, 68)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 281, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(btnAbrirArchivo, javax.swing.GroupLayout.PREFERRED_SIZE, 165, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(32, 32, 32)
                        .addComponent(btnCrearNuevoArchivoInfc, javax.swing.GroupLayout.PREFERRED_SIZE, 165, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(1057, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(42, 42, 42)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(txtNombreCuenta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
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
                            .addComponent(btnAnadirCuenta, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnOlvidarSeleccionCuenta, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnModificarCuenta, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(152, 152, 152)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAbrirArchivo, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnCrearNuevoArchivoInfc, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(387, Short.MAX_VALUE))
        );

        contenedorPestañas.addTab("Cuentas", new javax.swing.ImageIcon(getClass().getResource("/img/recibo.png")), jPanel1); // NOI18N

        jLabel4.setBackground(new java.awt.Color(204, 204, 204));
        jLabel4.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(51, 51, 51));
        jLabel4.setText("Transacciones:");

        tablaLibroDiario.setBackground(new java.awt.Color(153, 153, 153));
        tablaLibroDiario.setForeground(new java.awt.Color(51, 51, 51));
        tablaLibroDiario.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

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

        btnAnadirTransaccion.setBackground(new java.awt.Color(204, 204, 204));
        btnAnadirTransaccion.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        btnAnadirTransaccion.setForeground(new java.awt.Color(51, 51, 51));
        btnAnadirTransaccion.setText("Añadir asiento.");
        btnAnadirTransaccion.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED), new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED)));
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
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 177, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnAnadirTransaccion, javax.swing.GroupLayout.PREFERRED_SIZE, 171, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 811, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(111, 847, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAnadirTransaccion, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 287, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(410, Short.MAX_VALUE))
        );

        contenedorPestañas.addTab("Libro diario", new javax.swing.ImageIcon(getClass().getResource("/img/libro.png")), jPanel2); // NOI18N

        tablaLibroMayor.setBackground(new java.awt.Color(153, 153, 153));
        tablaLibroMayor.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEtchedBorder(), new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED)));
        tablaLibroMayor.setForeground(new java.awt.Color(51, 51, 51));
        tablaLibroMayor.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

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

        jButton6.setBackground(new java.awt.Color(204, 204, 204));
        jButton6.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jButton6.setForeground(new java.awt.Color(51, 51, 51));
        jButton6.setText("Generar libro mayor");
        jButton6.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED), new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED)));
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap(531, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton6, javax.swing.GroupLayout.PREFERRED_SIZE, 208, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 804, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(340, 340, 340))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(43, 43, 43)
                .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 344, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(31, 31, 31)
                .addComponent(jButton6, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(310, Short.MAX_VALUE))
        );

        contenedorPestañas.addTab("Libro mayor", new javax.swing.ImageIcon(getClass().getResource("/img/libros.png")), jPanel3); // NOI18N

        tablaBalanzaComprobacion.setBackground(new java.awt.Color(153, 153, 153));
        tablaBalanzaComprobacion.setForeground(new java.awt.Color(51, 51, 51));
        tablaBalanzaComprobacion.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

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

        btnGenerarBalanzaComprobacion.setBackground(new java.awt.Color(204, 204, 204));
        btnGenerarBalanzaComprobacion.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        btnGenerarBalanzaComprobacion.setForeground(new java.awt.Color(51, 51, 51));
        btnGenerarBalanzaComprobacion.setText("Generar balanza de comprobación.");
        btnGenerarBalanzaComprobacion.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED), new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED)));
        btnGenerarBalanzaComprobacion.setEnabled(false);
        btnGenerarBalanzaComprobacion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGenerarBalanzaComprobacionActionPerformed(evt);
            }
        });

        jLabel7.setText("TOTAL");

        txtHaber.setEditable(false);
        txtHaber.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEtchedBorder(), new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED)));

        txtDebe.setEditable(false);
        txtDebe.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEtchedBorder(), new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED)));

        lblInforBalanzaComprobacion.setText("Debe generar el libro mayor primero.");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane3)
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addComponent(btnGenerarBalanzaComprobacion, javax.swing.GroupLayout.PREFERRED_SIZE, 232, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lblInforBalanzaComprobacion)
                                .addGap(0, 0, Short.MAX_VALUE))))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                        .addGap(160, 160, 160)
                        .addComponent(jLabel7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 941, Short.MAX_VALUE)
                        .addComponent(txtDebe, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(83, 83, 83)
                        .addComponent(txtHaber, javax.swing.GroupLayout.PREFERRED_SIZE, 209, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(31, 31, 31)))
                .addContainerGap())
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
                    .addComponent(btnGenerarBalanzaComprobacion, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblInforBalanzaComprobacion))
                .addContainerGap(316, Short.MAX_VALUE))
        );

        contenedorPestañas.addTab("Balance comprobación", new javax.swing.ImageIcon(getClass().getResource("/img/hora.png")), jPanel4); // NOI18N

        tablaEstadoResultado.setBackground(new java.awt.Color(153, 153, 153));
        tablaEstadoResultado.setForeground(new java.awt.Color(51, 51, 51));
        tablaEstadoResultado.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Codigo", "Nombre de la cuenta", "Debe", "Haber", "Tipo de saldo"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Object.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane4.setViewportView(tablaEstadoResultado);

        jLabel6.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel6.setText("Utilidades o perdidas:");

        txtUtilidadesPerdidas.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEtchedBorder(), new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED)));

        btnGenerarEstadoResultado.setBackground(new java.awt.Color(204, 204, 204));
        btnGenerarEstadoResultado.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        btnGenerarEstadoResultado.setForeground(new java.awt.Color(51, 51, 51));
        btnGenerarEstadoResultado.setText("Generar estado resultado.");
        btnGenerarEstadoResultado.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED), new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED)));
        btnGenerarEstadoResultado.setEnabled(false);
        btnGenerarEstadoResultado.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGenerarEstadoResultadoActionPerformed(evt);
            }
        });

        btnCalculoUtilidadesPerdidas.setBackground(new java.awt.Color(204, 204, 204));
        btnCalculoUtilidadesPerdidas.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        btnCalculoUtilidadesPerdidas.setForeground(new java.awt.Color(51, 51, 51));
        btnCalculoUtilidadesPerdidas.setText("Calcular utilidades o perdidas");
        btnCalculoUtilidadesPerdidas.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED), new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED)));
        btnCalculoUtilidadesPerdidas.setEnabled(false);
        btnCalculoUtilidadesPerdidas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCalculoUtilidadesPerdidasActionPerformed(evt);
            }
        });

        lblInfoEstadoResultado.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        lblInfoEstadoResultado.setForeground(new java.awt.Color(255, 0, 0));
        lblInfoEstadoResultado.setText("Debe generar el balance de comprobacion antes.");
        lblInfoEstadoResultado.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addGroup(jPanel5Layout.createSequentialGroup()
                            .addComponent(btnGenerarEstadoResultado, javax.swing.GroupLayout.PREFERRED_SIZE, 181, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(btnCalculoUtilidadesPerdidas)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 141, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(18, 18, 18)
                            .addComponent(txtUtilidadesPerdidas, javax.swing.GroupLayout.PREFERRED_SIZE, 189, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(18, 18, 18)
                            .addComponent(lblResultadoGastosVsIngresos, javax.swing.GroupLayout.PREFERRED_SIZE, 103, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 981, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(lblInfoEstadoResultado))
                .addContainerGap(688, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 271, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnCalculoUtilidadesPerdidas, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btnGenerarEstadoResultado, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(txtUtilidadesPerdidas, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(lblResultadoGastosVsIngresos)))
                .addGap(18, 18, 18)
                .addComponent(lblInfoEstadoResultado)
                .addGap(394, 394, 394))
        );

        contenedorPestañas.addTab("Estado de resultado", new javax.swing.ImageIcon(getClass().getResource("/img/resultados.png")), jPanel5); // NOI18N

        jLabel26.setText("COSTO REAL DE MANO DE OBRA DE LOS EMPLEADOS");

        jPanel15.setBackground(new java.awt.Color(204, 204, 204));
        jPanel15.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(51, 204, 255)));

        jLabel27.setText("SALARIOS-PRESTACIONES");

        jLabel28.setText("SEPTIMO DIA");

        jLabel29.setText("TOTAL(SEMANAL)");

        jLabel30.setText("VACACION SEMANAL");

        jLabel31.setText("AGUINALDO SEMANAL");

        jLabel32.setText("SALUD SEMANAL");

        jLabel33.setText("INSAFORP SEMANAL");

        jLabel34.setText("AFP SEMANAL");

        txtSeptimo2.setEditable(false);
        txtSeptimo2.setHorizontalAlignment(javax.swing.JTextField.RIGHT);

        txtAguinaldo2.setEditable(false);
        txtAguinaldo2.setHorizontalAlignment(javax.swing.JTextField.RIGHT);

        txtVacacion2.setEditable(false);
        txtVacacion2.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        txtVacacion2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtVacacion2ActionPerformed(evt);
            }
        });

        txtSalud.setEditable(false);
        txtSalud.setHorizontalAlignment(javax.swing.JTextField.RIGHT);

        txtAFP.setEditable(false);
        txtAFP.setHorizontalAlignment(javax.swing.JTextField.RIGHT);

        txtINSAFORP.setEditable(false);
        txtINSAFORP.setHorizontalAlignment(javax.swing.JTextField.RIGHT);

        javax.swing.GroupLayout jPanel16Layout = new javax.swing.GroupLayout(jPanel16);
        jPanel16.setLayout(jPanel16Layout);
        jPanel16Layout.setHorizontalGroup(
            jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel16Layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtINSAFORP, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtAFP, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtSalud, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtVacacion2, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtAguinaldo2, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtSeptimo2, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(22, Short.MAX_VALUE))
        );
        jPanel16Layout.setVerticalGroup(
            jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel16Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(txtSeptimo2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtAguinaldo2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtVacacion2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtSalud, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtAFP, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtINSAFORP, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(21, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel15Layout = new javax.swing.GroupLayout(jPanel15);
        jPanel15.setLayout(jPanel15Layout);
        jPanel15Layout.setHorizontalGroup(
            jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel15Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel15Layout.createSequentialGroup()
                        .addComponent(jLabel27)
                        .addGap(42, 42, 42)
                        .addComponent(jLabel29))
                    .addGroup(jPanel15Layout.createSequentialGroup()
                        .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(jPanel15Layout.createSequentialGroup()
                                    .addGap(45, 45, 45)
                                    .addComponent(jLabel28))
                                .addComponent(jLabel31)
                                .addComponent(jLabel30, javax.swing.GroupLayout.Alignment.TRAILING))
                            .addComponent(jLabel32, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel34, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel33, javax.swing.GroupLayout.Alignment.TRAILING))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanel16, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(17, Short.MAX_VALUE))
        );
        jPanel15Layout.setVerticalGroup(
            jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel15Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel29)
                    .addComponent(jLabel27))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel15Layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(jLabel28, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel31)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel30)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel32)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel34)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel33)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel15Layout.createSequentialGroup()
                        .addComponent(jPanel16, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())))
        );

        jPanel19.setBackground(new java.awt.Color(204, 204, 204));
        jPanel19.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(51, 204, 255)));
        jPanel19.setForeground(new java.awt.Color(51, 204, 255));

        jLabel35.setText("TOTAL:");

        jLabel36.setText("TOTAL POR DIA:");

        jLabel37.setText("TOTAL POR HORA:");

        txtTotalHora.setEditable(false);
        txtTotalHora.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtTotalHoraActionPerformed(evt);
            }
        });

        txtTotalDia.setEditable(false);

        txtTotal.setEditable(false);

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtTotalHora)
                    .addComponent(txtTotalDia, javax.swing.GroupLayout.DEFAULT_SIZE, 175, Short.MAX_VALUE)
                    .addComponent(txtTotal))
                .addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(txtTotal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(txtTotalDia, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(txtTotalHora, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel19Layout = new javax.swing.GroupLayout(jPanel19);
        jPanel19.setLayout(jPanel19Layout);
        jPanel19Layout.setHorizontalGroup(
            jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel19Layout.createSequentialGroup()
                .addGap(26, 26, 26)
                .addGroup(jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel36, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel35, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel37, javax.swing.GroupLayout.Alignment.TRAILING))
                .addGap(18, 18, 18)
                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel19Layout.setVerticalGroup(
            jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel19Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel19Layout.createSequentialGroup()
                        .addGap(9, 9, 9)
                        .addComponent(jLabel35)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel36)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel37)))
                .addContainerGap(30, Short.MAX_VALUE))
        );

        jPanel23.setBackground(new java.awt.Color(204, 204, 204));
        jPanel23.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(51, 204, 255)));

        jLabel38.setText("FACTOR DE RECARGO");

        jLabel39.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel39.setText("DIA:");

        jLabel40.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel40.setText("HORA:");

        jLabel41.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel41.setText("SEMANA:");

        jLabel42.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel42.setText("TOTAL MANO DE OBRA REAL");

        txtSemana.setEditable(false);

        txtHora.setEditable(false);

        txtDia.setEditable(false);

        txtSalarioDia.setEditable(false);

        txtSalarioHora.setEditable(false);

        txtSalarioSemana.setEditable(false);

        txtFactorHora.setEditable(false);

        txtFactorDia.setEditable(false);

        txtFactorSemana.setEditable(false);

        javax.swing.GroupLayout jPanel24Layout = new javax.swing.GroupLayout(jPanel24);
        jPanel24.setLayout(jPanel24Layout);
        jPanel24Layout.setHorizontalGroup(
            jPanel24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel24Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel24Layout.createSequentialGroup()
                        .addComponent(txtDia)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtSalarioDia, javax.swing.GroupLayout.PREFERRED_SIZE, 148, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtFactorDia, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel24Layout.createSequentialGroup()
                        .addGroup(jPanel24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel24Layout.createSequentialGroup()
                                .addComponent(txtSemana)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                            .addGroup(jPanel24Layout.createSequentialGroup()
                                .addComponent(txtHora)
                                .addGap(7, 7, 7)))
                        .addGroup(jPanel24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtSalarioHora, javax.swing.GroupLayout.PREFERRED_SIZE, 147, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtSalarioSemana, javax.swing.GroupLayout.PREFERRED_SIZE, 147, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(txtFactorHora)
                            .addComponent(txtFactorSemana, javax.swing.GroupLayout.DEFAULT_SIZE, 110, Short.MAX_VALUE))))
                .addContainerGap())
        );
        jPanel24Layout.setVerticalGroup(
            jPanel24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel24Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtDia, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtSalarioDia, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtFactorDia, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtSalarioHora, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(txtHora, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(txtFactorHora, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtSemana, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtSalarioSemana, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtFactorSemana, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(14, 14, 14))
        );

        jLabel43.setText("SALARIO NOMINAL");

        jLabel44.setText("FACTOR");

        javax.swing.GroupLayout jPanel23Layout = new javax.swing.GroupLayout(jPanel23);
        jPanel23.setLayout(jPanel23Layout);
        jPanel23Layout.setHorizontalGroup(
            jPanel23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel23Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel38, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel39, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel40, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel41, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel23Layout.createSequentialGroup()
                        .addComponent(jPanel24, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(jPanel23Layout.createSequentialGroup()
                        .addComponent(jLabel42, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel43)
                        .addGap(52, 52, 52)
                        .addComponent(jLabel44)
                        .addGap(47, 47, 47))))
        );
        jPanel23Layout.setVerticalGroup(
            jPanel23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel23Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel38)
                    .addComponent(jLabel42)
                    .addComponent(jLabel43)
                    .addComponent(jLabel44))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel23Layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(jLabel39)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel40)
                        .addGap(12, 12, 12)
                        .addComponent(jLabel41)
                        .addGap(0, 13, Short.MAX_VALUE))
                    .addComponent(jPanel24, javax.swing.GroupLayout.PREFERRED_SIZE, 91, Short.MAX_VALUE))
                .addContainerGap())
        );

        jLabel45.setText("FACTOR DE RECARGO DIA, HORA, SEMANA");

        jPanel25.setBackground(new java.awt.Color(204, 204, 204));
        jPanel25.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(51, 204, 255)));

        jLabel46.setText("FACTOR DE RECARGO");

        jLabel47.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel47.setText("DIA:");

        jLabel48.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel48.setText("HORA:");

        jLabel49.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel49.setText("SEMANA:");

        jLabel50.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel50.setText("TOTAL MANO DE OBRA REAL");

        txtSemanaEficiencia.setEditable(false);

        txtHoraEficiencia.setEditable(false);

        txtDiaEficiencia.setEditable(false);

        txtSalarioDiaEficiencia.setEditable(false);

        txtSalarioHoraEficiencia.setEditable(false);

        txtSalarioSemanaEficiencia.setEditable(false);

        txtFactorHoraEficiencia.setEditable(false);

        txtFactorDiaEficiencia.setEditable(false);

        txtFactorSemanaEficiencia.setEditable(false);

        javax.swing.GroupLayout jPanel26Layout = new javax.swing.GroupLayout(jPanel26);
        jPanel26.setLayout(jPanel26Layout);
        jPanel26Layout.setHorizontalGroup(
            jPanel26Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel26Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel26Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(txtHoraEficiencia)
                    .addComponent(txtDiaEficiencia)
                    .addComponent(txtSemanaEficiencia))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel26Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(txtSalarioHoraEficiencia, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 147, Short.MAX_VALUE)
                    .addComponent(txtSalarioDiaEficiencia, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(txtSalarioSemanaEficiencia))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel26Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(txtFactorDiaEficiencia, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 111, Short.MAX_VALUE)
                    .addComponent(txtFactorHoraEficiencia, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(txtFactorSemanaEficiencia, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap())
        );
        jPanel26Layout.setVerticalGroup(
            jPanel26Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel26Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel26Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtDiaEficiencia, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtSalarioDiaEficiencia, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtFactorDiaEficiencia, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel26Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel26Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(txtFactorHoraEficiencia, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(txtSalarioHoraEficiencia, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(txtHoraEficiencia, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(jPanel26Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel26Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtFactorSemanaEficiencia, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel26Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtSalarioSemanaEficiencia, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel26Layout.createSequentialGroup()
                        .addGap(4, 4, 4)
                        .addComponent(txtSemanaEficiencia, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(14, 14, 14))
        );

        jLabel74.setText("SALARIO NOMINAL CON EFICIENCIA");

        jLabel75.setText("FACTOR");

        javax.swing.GroupLayout jPanel25Layout = new javax.swing.GroupLayout(jPanel25);
        jPanel25.setLayout(jPanel25Layout);
        jPanel25Layout.setHorizontalGroup(
            jPanel25Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel25Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel25Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel46, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel47, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel48, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel49, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel25Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel25Layout.createSequentialGroup()
                        .addComponent(jPanel26, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(jPanel25Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 50, Short.MAX_VALUE)
                        .addComponent(jLabel50, javax.swing.GroupLayout.PREFERRED_SIZE, 202, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(52, 52, 52)
                        .addComponent(jLabel74, javax.swing.GroupLayout.PREFERRED_SIZE, 207, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel75)
                        .addGap(37, 37, 37))))
        );
        jPanel25Layout.setVerticalGroup(
            jPanel25Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel25Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel25Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel46)
                    .addComponent(jLabel75)
                    .addComponent(jLabel50, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel74))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel25Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel25Layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(jLabel47)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel48)
                        .addGap(12, 12, 12)
                        .addComponent(jLabel49)
                        .addGap(0, 13, Short.MAX_VALUE))
                    .addComponent(jPanel26, javax.swing.GroupLayout.PREFERRED_SIZE, 91, Short.MAX_VALUE))
                .addContainerGap())
        );

        jLabel76.setText("FACTOR DE RECARGO CON EFICIENCIA DIA, HORA, SEMANA");

        btnCalcularManoDeObra.setBackground(new java.awt.Color(204, 204, 204));
        btnCalcularManoDeObra.setFont(new java.awt.Font("Segoe UI Black", 0, 14)); // NOI18N
        btnCalcularManoDeObra.setForeground(new java.awt.Color(51, 51, 51));
        btnCalcularManoDeObra.setText("CALCULAR MANO DE OBRA REAL");
        btnCalcularManoDeObra.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED), new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED)));
        btnCalcularManoDeObra.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCalcularManoDeObraActionPerformed(evt);
            }
        });

        btnLimpiar.setBackground(new java.awt.Color(204, 204, 204));
        btnLimpiar.setFont(new java.awt.Font("Segoe UI Black", 0, 14)); // NOI18N
        btnLimpiar.setForeground(new java.awt.Color(51, 51, 51));
        btnLimpiar.setText("LIMPIAR");
        btnLimpiar.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED), new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED)));
        btnLimpiar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLimpiarActionPerformed(evt);
            }
        });

        jLabel77.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel77.setText("SALARIO DIARIO:");

        jLabel78.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel78.setText("HORAS TRABAJADAS AL DIA:");

        jLabel138.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel138.setText("DIAS A LA SEMANA TRABAJADAS:");

        jLabel139.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel139.setText("DIAS PAGADOS PARA AGUINALDO:");

        jLabel140.setText("DIAS PAGADOS PARA VACACIONES:");

        txtSalario.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEtchedBorder(), new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED)));

        txtHoras.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEtchedBorder(), new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED)));
        txtHoras.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtHorasActionPerformed(evt);
            }
        });

        txtDiasAguinaldo.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEtchedBorder(), new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED)));

        txtDiasVacacion.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEtchedBorder(), new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED)));

        txtDiasTrabajado.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEtchedBorder(), new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED)));

        jLabel141.setText("Porcentaje de recargo para vacaciones:");

        jLabel142.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel142.setText("Porcentaje de recargo para seguro:");

        jLabel143.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel143.setText("Porcentaje de recargo para AFP:");

        jLabel144.setText("Porcentaje de eficiencia del empleado:");

        txtPorcentajeVacacion.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEtchedBorder(), new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED)));

        txtPorcentajeSeguro.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEtchedBorder(), new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED)));

        txtPorcentajeEficiencia.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEtchedBorder(), new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED)));

        txtPorcentajeAFP.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEtchedBorder(), new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED)));

        jLabel145.setText("Porcentaje de recargo para INSAFORP:");

        txtPorcentajeINSAFORP.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEtchedBorder(), new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED)));

        javax.swing.GroupLayout jPanel14Layout = new javax.swing.GroupLayout(jPanel14);
        jPanel14.setLayout(jPanel14Layout);
        jPanel14Layout.setHorizontalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel14Layout.createSequentialGroup()
                .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel14Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel14Layout.createSequentialGroup()
                                .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(jPanel19, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jPanel15, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGap(217, 217, 217)
                                .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(jPanel23, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jPanel25, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel14Layout.createSequentialGroup()
                                        .addComponent(jLabel45)
                                        .addGap(244, 244, 244))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel14Layout.createSequentialGroup()
                                        .addComponent(jLabel76)
                                        .addGap(187, 187, 187))))
                            .addGroup(jPanel14Layout.createSequentialGroup()
                                .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel14Layout.createSequentialGroup()
                                        .addGap(399, 399, 399)
                                        .addComponent(jLabel145))
                                    .addGroup(jPanel14Layout.createSequentialGroup()
                                        .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jLabel139, javax.swing.GroupLayout.PREFERRED_SIZE, 193, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(jLabel77, javax.swing.GroupLayout.PREFERRED_SIZE, 193, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(jLabel78, javax.swing.GroupLayout.PREFERRED_SIZE, 193, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(jLabel138, javax.swing.GroupLayout.PREFERRED_SIZE, 193, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(jLabel140, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 187, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addComponent(txtDiasAguinaldo, javax.swing.GroupLayout.PREFERRED_SIZE, 163, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(txtDiasVacacion, javax.swing.GroupLayout.PREFERRED_SIZE, 163, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addComponent(txtSalario, javax.swing.GroupLayout.PREFERRED_SIZE, 163, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(txtHoras, javax.swing.GroupLayout.PREFERRED_SIZE, 163, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(txtDiasTrabajado, javax.swing.GroupLayout.PREFERRED_SIZE, 163, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                        .addGap(29, 29, 29)
                                        .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jLabel141)
                                            .addGroup(jPanel14Layout.createSequentialGroup()
                                                .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addComponent(jLabel144, javax.swing.GroupLayout.PREFERRED_SIZE, 205, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addComponent(jLabel143, javax.swing.GroupLayout.PREFERRED_SIZE, 205, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addComponent(jLabel142, javax.swing.GroupLayout.PREFERRED_SIZE, 205, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addGap(39, 39, 39)
                                                .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                    .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(txtPorcentajeSeguro, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(txtPorcentajeVacacion, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(txtPorcentajeINSAFORP, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                    .addComponent(txtPorcentajeAFP, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addComponent(txtPorcentajeEficiencia, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE))))))
                                .addGap(122, 122, 122)
                                .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(btnCalcularManoDeObra, javax.swing.GroupLayout.PREFERRED_SIZE, 267, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(btnLimpiar, javax.swing.GroupLayout.PREFERRED_SIZE, 267, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                    .addGroup(jPanel14Layout.createSequentialGroup()
                        .addGap(42, 42, 42)
                        .addComponent(jLabel26)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel14Layout.setVerticalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel14Layout.createSequentialGroup()
                .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel14Layout.createSequentialGroup()
                        .addGap(96, 96, 96)
                        .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel14Layout.createSequentialGroup()
                                .addComponent(txtPorcentajeVacacion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(6, 6, 6)
                                .addComponent(txtPorcentajeSeguro, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(4, 4, 4)
                                .addComponent(txtPorcentajeAFP, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(8, 8, 8)
                                .addComponent(txtPorcentajeEficiencia, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel14Layout.createSequentialGroup()
                                .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(txtSalario, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel77)
                                    .addComponent(jLabel145)
                                    .addComponent(txtPorcentajeINSAFORP, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(6, 6, 6)
                                .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(txtHoras, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel78)
                                    .addComponent(jLabel141))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(txtDiasTrabajado, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel138)
                                    .addComponent(jLabel142))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(txtDiasAguinaldo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel139)
                                    .addComponent(jLabel143))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(txtDiasVacacion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel140)
                                    .addComponent(jLabel144))))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel14Layout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnCalcularManoDeObra, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnLimpiar, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(44, 44, 44)))
                .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel14Layout.createSequentialGroup()
                        .addComponent(jLabel26)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanel15, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel19, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel14Layout.createSequentialGroup()
                        .addComponent(jLabel45)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanel23, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(27, 27, 27)
                        .addComponent(jLabel76)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanel25, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(157, 157, 157))
        );

        jScrollPane8.setViewportView(jPanel14);
        jPanel14.getAccessibleContext().setAccessibleName("");

        contenedorPestañas.addTab("Mano de obra", jScrollPane8);

        jPanel30.setBackground(new java.awt.Color(204, 204, 204));

        jLabel173.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel173.setForeground(new java.awt.Color(51, 51, 51));
        jLabel173.setText("531");

        jLabel174.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel174.setForeground(new java.awt.Color(51, 51, 51));
        jLabel174.setText("Salarios supervisión");

        txtSalariosSup2.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEtchedBorder(), new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED)));

        txtSalariosOfi2.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEtchedBorder(), new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED)));

        jLabel175.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel175.setForeground(new java.awt.Color(51, 51, 51));
        jLabel175.setText("Salarios de oficina");

        jLabel176.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel176.setForeground(new java.awt.Color(51, 51, 51));
        jLabel176.setText("532");

        jLabel177.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel177.setForeground(new java.awt.Color(51, 51, 51));
        jLabel177.setText("533");

        jLabel178.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel178.setForeground(new java.awt.Color(51, 51, 51));
        jLabel178.setText("Mano de obra indirecta");

        txtManoDeObraIndirecta2.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEtchedBorder(), new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED)));

        jLabel179.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel179.setForeground(new java.awt.Color(51, 51, 51));
        jLabel179.setText("534");

        jLabel180.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel180.setForeground(new java.awt.Color(51, 51, 51));
        jLabel180.setText("Materiales indirectos");

        txtMaterialesIndirectos2.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEtchedBorder(), new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED)));

        jLabel181.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel181.setForeground(new java.awt.Color(51, 51, 51));
        jLabel181.setText("537");

        jLabel182.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel182.setForeground(new java.awt.Color(51, 51, 51));
        jLabel182.setText("Otros Materiales");

        txtOtrosMateriales2.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEtchedBorder(), new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED)));

        jLabel183.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel183.setForeground(new java.awt.Color(51, 51, 51));
        jLabel183.setText("538");

        jLabel184.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel184.setForeground(new java.awt.Color(51, 51, 51));
        jLabel184.setText("Depreciación");

        jLabel185.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel185.setForeground(new java.awt.Color(51, 51, 51));
        jLabel185.setText("535");

        txtDepreciacion2.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEtchedBorder(), new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED)));

        jLabel186.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel186.setForeground(new java.awt.Color(51, 51, 51));
        jLabel186.setText("Suministros");

        txtSuministros2.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEtchedBorder(), new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED)));

        jLabel187.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel187.setForeground(new java.awt.Color(51, 51, 51));
        jLabel187.setText("536");

        jLabel188.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel188.setForeground(new java.awt.Color(51, 51, 51));
        jLabel188.setText("Herramientas");

        txtHerramientas2.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEtchedBorder(), new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED)));

        txtImpuestos2.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEtchedBorder(), new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED)));

        jLabel189.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel189.setForeground(new java.awt.Color(51, 51, 51));
        jLabel189.setText("Servicios Públicos");

        txtServiciosPublicos2.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEtchedBorder(), new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED)));

        jLabel190.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel190.setForeground(new java.awt.Color(51, 51, 51));
        jLabel190.setText("613");

        jLabel191.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel191.setForeground(new java.awt.Color(51, 51, 51));
        jLabel191.setText("Servicios Privados");

        txtServiciosPrivados2.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEtchedBorder(), new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED)));

        jLabel192.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel192.setForeground(new java.awt.Color(51, 51, 51));
        jLabel192.setText("539");

        jLabel193.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel193.setForeground(new java.awt.Color(51, 51, 51));
        jLabel193.setText("Impuestos");

        jLabel194.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel194.setForeground(new java.awt.Color(51, 51, 51));
        jLabel194.setText("612");

        jLabel195.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel195.setForeground(new java.awt.Color(51, 51, 51));
        jLabel195.setText("TOTALES");

        txtTotales2.setEditable(false);
        txtTotales2.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEtchedBorder(), new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED)));

        javax.swing.GroupLayout jPanel30Layout = new javax.swing.GroupLayout(jPanel30);
        jPanel30.setLayout(jPanel30Layout);
        jPanel30Layout.setHorizontalGroup(
            jPanel30Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel30Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel30Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel30Layout.createSequentialGroup()
                        .addComponent(jLabel195)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(txtTotales2, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel30Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addGroup(jPanel30Layout.createSequentialGroup()
                            .addComponent(jLabel190)
                            .addGap(26, 26, 26)
                            .addComponent(jLabel191, javax.swing.GroupLayout.PREFERRED_SIZE, 288, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(18, 18, 18)
                            .addComponent(txtServiciosPrivados2, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel30Layout.createSequentialGroup()
                            .addComponent(jLabel194)
                            .addGap(26, 26, 26)
                            .addComponent(jLabel189, javax.swing.GroupLayout.PREFERRED_SIZE, 288, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(18, 18, 18)
                            .addComponent(txtServiciosPublicos2, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel30Layout.createSequentialGroup()
                            .addComponent(jLabel192)
                            .addGap(26, 26, 26)
                            .addComponent(jLabel193, javax.swing.GroupLayout.PREFERRED_SIZE, 288, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(18, 18, 18)
                            .addComponent(txtImpuestos2, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel30Layout.createSequentialGroup()
                            .addComponent(jLabel183)
                            .addGap(26, 26, 26)
                            .addComponent(jLabel184, javax.swing.GroupLayout.PREFERRED_SIZE, 288, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(18, 18, 18)
                            .addComponent(txtDepreciacion2, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel30Layout.createSequentialGroup()
                            .addComponent(jLabel181)
                            .addGap(26, 26, 26)
                            .addComponent(jLabel182, javax.swing.GroupLayout.PREFERRED_SIZE, 288, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(18, 18, 18)
                            .addComponent(txtOtrosMateriales2, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel30Layout.createSequentialGroup()
                            .addComponent(jLabel187)
                            .addGap(26, 26, 26)
                            .addComponent(jLabel188, javax.swing.GroupLayout.PREFERRED_SIZE, 288, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(18, 18, 18)
                            .addComponent(txtHerramientas2, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel30Layout.createSequentialGroup()
                            .addComponent(jLabel185)
                            .addGap(26, 26, 26)
                            .addComponent(jLabel186, javax.swing.GroupLayout.PREFERRED_SIZE, 288, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(18, 18, 18)
                            .addComponent(txtSuministros2, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel30Layout.createSequentialGroup()
                            .addComponent(jLabel179)
                            .addGap(26, 26, 26)
                            .addComponent(jLabel180, javax.swing.GroupLayout.PREFERRED_SIZE, 288, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(18, 18, 18)
                            .addComponent(txtMaterialesIndirectos2, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel30Layout.createSequentialGroup()
                            .addComponent(jLabel177)
                            .addGap(26, 26, 26)
                            .addComponent(jLabel178, javax.swing.GroupLayout.PREFERRED_SIZE, 288, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(18, 18, 18)
                            .addComponent(txtManoDeObraIndirecta2, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel30Layout.createSequentialGroup()
                            .addComponent(jLabel176)
                            .addGap(26, 26, 26)
                            .addComponent(jLabel175, javax.swing.GroupLayout.PREFERRED_SIZE, 288, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(18, 18, 18)
                            .addComponent(txtSalariosOfi2, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel30Layout.createSequentialGroup()
                            .addComponent(jLabel173)
                            .addGap(26, 26, 26)
                            .addComponent(jLabel174, javax.swing.GroupLayout.PREFERRED_SIZE, 288, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(18, 18, 18)
                            .addComponent(txtSalariosSup2, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(49, Short.MAX_VALUE))
        );
        jPanel30Layout.setVerticalGroup(
            jPanel30Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel30Layout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addGroup(jPanel30Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel173)
                    .addComponent(jLabel174)
                    .addComponent(txtSalariosSup2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel30Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel176)
                    .addComponent(jLabel175)
                    .addComponent(txtSalariosOfi2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel30Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel177)
                    .addComponent(jLabel178)
                    .addComponent(txtManoDeObraIndirecta2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel30Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel179)
                    .addComponent(jLabel180)
                    .addComponent(txtMaterialesIndirectos2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel30Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel185)
                    .addComponent(jLabel186)
                    .addComponent(txtSuministros2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel30Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel187)
                    .addComponent(jLabel188)
                    .addComponent(txtHerramientas2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel30Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel181)
                    .addComponent(jLabel182)
                    .addComponent(txtOtrosMateriales2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel30Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel183)
                    .addComponent(jLabel184)
                    .addComponent(txtDepreciacion2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel30Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel192)
                    .addComponent(jLabel193)
                    .addComponent(txtImpuestos2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel30Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel194)
                    .addComponent(jLabel189)
                    .addComponent(txtServiciosPublicos2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel30Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel190)
                    .addComponent(jLabel191)
                    .addComponent(txtServiciosPrivados2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel30Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel195)
                    .addComponent(txtTotales2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        btnLimpia.setBackground(new java.awt.Color(204, 204, 204));
        btnLimpia.setForeground(new java.awt.Color(51, 51, 51));
        btnLimpia.setText("LIMPIAR");
        btnLimpia.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED), new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED)));
        btnLimpia.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLimpiaActionPerformed(evt);
            }
        });

        btnGuardar.setBackground(new java.awt.Color(204, 204, 204));
        btnGuardar.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        btnGuardar.setForeground(new java.awt.Color(51, 51, 51));
        btnGuardar.setText("GUARDAR");
        btnGuardar.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED), new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED)));
        btnGuardar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGuardarActionPerformed(evt);
            }
        });

        jPanel18.setBackground(new java.awt.Color(204, 204, 204));

        jLabel79.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel79.setForeground(new java.awt.Color(51, 51, 51));
        jLabel79.setText("Análisis");

        jLabel80.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel80.setForeground(new java.awt.Color(51, 51, 51));
        jLabel80.setText("Diseño");

        jLabel81.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel81.setForeground(new java.awt.Color(51, 51, 51));
        jLabel81.setText("Desarrollo");

        jLabel82.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel82.setForeground(new java.awt.Color(51, 51, 51));
        jLabel82.setText("Pruebas");

        jLabel83.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel83.setForeground(new java.awt.Color(51, 51, 51));
        jLabel83.setText("Mantenimiento");

        txtAnalisisEspacio.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEtchedBorder(), new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED)));
        txtAnalisisEspacio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtAnalisisEspacioActionPerformed(evt);
            }
        });

        txtAnalisisConsumo.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEtchedBorder(), new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED)));

        txtAnalisisEmpleado.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEtchedBorder(), new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED)));

        txtAnalisisHora.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEtchedBorder(), new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED)));

        txtDisenoHora.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEtchedBorder(), new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED)));

        txtDisenoEspacio.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEtchedBorder(), new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED)));
        txtDisenoEspacio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtDisenoEspacioActionPerformed(evt);
            }
        });

        txtDisenoConsumo.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEtchedBorder(), new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED)));

        txtDisenoEmpleado.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEtchedBorder(), new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED)));

        txtDesarrolloHora.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEtchedBorder(), new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED)));

        txtDesarrolloEspacio.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEtchedBorder(), new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED)));
        txtDesarrolloEspacio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtDesarrolloEspacioActionPerformed(evt);
            }
        });

        txtDesarrolloConsumo.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEtchedBorder(), new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED)));

        txtDesarrolloEmpleado.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEtchedBorder(), new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED)));

        txtPruebaHora.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEtchedBorder(), new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED)));

        txtPruebaEspacio.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEtchedBorder(), new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED)));
        txtPruebaEspacio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtPruebaEspacioActionPerformed(evt);
            }
        });

        txtPruebaConsumo.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEtchedBorder(), new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED)));

        txtPruebaEmpleado.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEtchedBorder(), new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED)));

        txtMantenimientoHora.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEtchedBorder(), new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED)));

        txtMantenimientoEspacio.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEtchedBorder(), new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED)));
        txtMantenimientoEspacio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtMantenimientoEspacioActionPerformed(evt);
            }
        });

        txtMantenimientoConsumo.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEtchedBorder(), new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED)));

        txtMantenimientoEmpleado.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEtchedBorder(), new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED)));

        jLabel84.setText("DEPARTAMENTOS ÁREA PRODUCTIVA.");

        jLabel85.setText("DEPARTAMENTOS ÁREA DE SERVICIOS BRINDADOS");

        txtCalidadConsumo.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEtchedBorder(), new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED)));

        txtCalidadEmpleado.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEtchedBorder(), new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED)));

        txtInsumoHora.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEtchedBorder(), new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED)));

        txtInsumoEspacio.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEtchedBorder(), new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED)));
        txtInsumoEspacio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtInsumoEspacioActionPerformed(evt);
            }
        });

        txtInsumoConsumo.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEtchedBorder(), new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED)));

        txtInsumoEmpleado.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEtchedBorder(), new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED)));

        txtMantenimientoSoftHora.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEtchedBorder(), new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED)));

        txtMantenimientoSoftEspacio.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEtchedBorder(), new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED)));
        txtMantenimientoSoftEspacio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtMantenimientoSoftEspacioActionPerformed(evt);
            }
        });

        txtMantenimientoSoftConsumo.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEtchedBorder(), new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED)));

        txtMantenimientoSoftEmpleado.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEtchedBorder(), new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED)));

        jLabel86.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel86.setForeground(new java.awt.Color(51, 51, 51));
        jLabel86.setText("Calidad");

        jLabel87.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel87.setForeground(new java.awt.Color(51, 51, 51));
        jLabel87.setText("Insumos y materiales");

        jLabel88.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel88.setForeground(new java.awt.Color(51, 51, 51));
        jLabel88.setText("Mantenimiento de software");

        jLabel89.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel89.setForeground(new java.awt.Color(51, 51, 51));
        jLabel89.setText("Mantenimiento");

        txtCalidadHora.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEtchedBorder(), new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED)));

        txtCalidadEspacio.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEtchedBorder(), new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED)));
        txtCalidadEspacio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtCalidadEspacioActionPerformed(evt);
            }
        });

        txtMantenimientoServicioHora.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEtchedBorder(), new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED)));

        txtMantenimientoServicioEspacio.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEtchedBorder(), new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED)));
        txtMantenimientoServicioEspacio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtMantenimientoServicioEspacioActionPerformed(evt);
            }
        });

        txtMantenimientoServicioConsumo.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEtchedBorder(), new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED)));

        txtMantenimientoServicioEmpleado.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEtchedBorder(), new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED)));

        jLabel90.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel90.setForeground(new java.awt.Color(51, 51, 51));
        jLabel90.setText("TOTALES");

        txtTotalHoraHombre.setEditable(false);
        txtTotalHoraHombre.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEtchedBorder(), new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED)));

        txtTotalEspacio.setEditable(false);
        txtTotalEspacio.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEtchedBorder(), new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED)));
        txtTotalEspacio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtTotalEspacioActionPerformed(evt);
            }
        });

        txtTotalConsumo.setEditable(false);
        txtTotalConsumo.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEtchedBorder(), new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED)));

        txtTotalEmpleado.setEditable(false);
        txtTotalEmpleado.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEtchedBorder(), new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED)));

        javax.swing.GroupLayout jPanel18Layout = new javax.swing.GroupLayout(jPanel18);
        jPanel18.setLayout(jPanel18Layout);
        jPanel18Layout.setHorizontalGroup(
            jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel18Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel85)
                .addGap(165, 165, 165))
            .addGroup(jPanel18Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel90)
                    .addGroup(jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addGroup(jPanel18Layout.createSequentialGroup()
                            .addGroup(jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(jPanel18Layout.createSequentialGroup()
                                        .addComponent(jLabel89)
                                        .addGap(69, 69, 69))
                                    .addComponent(jLabel88, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel87, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addComponent(jLabel86))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(jPanel18Layout.createSequentialGroup()
                                    .addComponent(txtMantenimientoSoftEspacio, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(18, 18, 18)
                                    .addComponent(txtMantenimientoSoftConsumo, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(18, 18, 18)
                                    .addComponent(txtMantenimientoSoftEmpleado, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(18, 18, 18)
                                    .addComponent(txtMantenimientoSoftHora, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(jPanel18Layout.createSequentialGroup()
                                    .addComponent(txtInsumoEspacio, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(18, 18, 18)
                                    .addComponent(txtInsumoConsumo, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(18, 18, 18)
                                    .addComponent(txtInsumoEmpleado, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(18, 18, 18)
                                    .addComponent(txtInsumoHora, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(jPanel18Layout.createSequentialGroup()
                                    .addComponent(txtCalidadEspacio, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(18, 18, 18)
                                    .addComponent(txtCalidadConsumo, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(18, 18, 18)
                                    .addComponent(txtCalidadEmpleado, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(18, 18, 18)
                                    .addComponent(txtCalidadHora, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(jPanel18Layout.createSequentialGroup()
                                    .addComponent(txtMantenimientoServicioEspacio, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(18, 18, 18)
                                    .addComponent(txtMantenimientoServicioConsumo, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(18, 18, 18)
                                    .addComponent(txtMantenimientoServicioEmpleado, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(18, 18, 18)
                                    .addComponent(txtMantenimientoServicioHora, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(jPanel18Layout.createSequentialGroup()
                                    .addComponent(txtTotalEspacio, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(18, 18, 18)
                                    .addComponent(txtTotalConsumo, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(18, 18, 18)
                                    .addComponent(txtTotalEmpleado, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(18, 18, 18)
                                    .addComponent(txtTotalHoraHombre, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addGroup(jPanel18Layout.createSequentialGroup()
                            .addGroup(jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel83)
                                .addComponent(jLabel82)
                                .addComponent(jLabel81)
                                .addComponent(jLabel80)
                                .addComponent(jLabel79))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(jPanel18Layout.createSequentialGroup()
                                    .addComponent(txtPruebaEspacio, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(18, 18, 18)
                                    .addComponent(txtPruebaConsumo, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(18, 18, 18)
                                    .addComponent(txtPruebaEmpleado, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(18, 18, 18)
                                    .addComponent(txtPruebaHora, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(jPanel18Layout.createSequentialGroup()
                                    .addComponent(txtDesarrolloEspacio, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(18, 18, 18)
                                    .addComponent(txtDesarrolloConsumo, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(18, 18, 18)
                                    .addComponent(txtDesarrolloEmpleado, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(18, 18, 18)
                                    .addComponent(txtDesarrolloHora, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(jPanel18Layout.createSequentialGroup()
                                    .addComponent(txtDisenoEspacio, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(18, 18, 18)
                                    .addComponent(txtDisenoConsumo, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(18, 18, 18)
                                    .addComponent(txtDisenoEmpleado, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(18, 18, 18)
                                    .addComponent(txtDisenoHora, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(jPanel18Layout.createSequentialGroup()
                                    .addComponent(txtAnalisisEspacio, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(18, 18, 18)
                                    .addGroup(jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel84)
                                        .addGroup(jPanel18Layout.createSequentialGroup()
                                            .addComponent(txtAnalisisConsumo, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addGap(18, 18, 18)
                                            .addComponent(txtAnalisisEmpleado, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addGap(18, 18, 18)
                                            .addComponent(txtAnalisisHora, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE))))
                                .addGroup(jPanel18Layout.createSequentialGroup()
                                    .addComponent(txtMantenimientoEspacio, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(18, 18, 18)
                                    .addComponent(txtMantenimientoConsumo, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(18, 18, 18)
                                    .addComponent(txtMantenimientoEmpleado, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(18, 18, 18)
                                    .addComponent(txtMantenimientoHora, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE))))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel18Layout.setVerticalGroup(
            jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel18Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel84)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel79)
                    .addComponent(txtAnalisisEspacio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtAnalisisConsumo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtAnalisisEmpleado, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtAnalisisHora, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel80)
                    .addComponent(txtDisenoEspacio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtDisenoConsumo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtDisenoEmpleado, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtDisenoHora, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel81)
                    .addComponent(txtDesarrolloEspacio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtDesarrolloConsumo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtDesarrolloEmpleado, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtDesarrolloHora, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel82)
                    .addComponent(txtPruebaEspacio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtPruebaConsumo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtPruebaEmpleado, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtPruebaHora, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel83)
                    .addComponent(txtMantenimientoEspacio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtMantenimientoConsumo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtMantenimientoEmpleado, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtMantenimientoHora, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(31, 31, 31)
                .addComponent(jLabel85)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel86)
                    .addComponent(txtCalidadEspacio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtCalidadConsumo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtCalidadEmpleado, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtCalidadHora, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel87)
                    .addComponent(txtInsumoEspacio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtInsumoConsumo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtInsumoEmpleado, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtInsumoHora, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel88)
                    .addComponent(txtMantenimientoSoftEspacio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtMantenimientoSoftConsumo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtMantenimientoSoftEmpleado, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtMantenimientoSoftHora, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel89)
                    .addComponent(txtMantenimientoServicioEspacio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtMantenimientoServicioConsumo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtMantenimientoServicioEmpleado, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtMantenimientoServicioHora, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 28, Short.MAX_VALUE)
                .addGroup(jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel90, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(txtTotalEspacio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(txtTotalConsumo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(txtTotalEmpleado, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(txtTotalHoraHombre, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel9Layout.createSequentialGroup()
                        .addComponent(btnGuardar, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnLimpia, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel30, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jPanel18, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel18, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel9Layout.createSequentialGroup()
                        .addComponent(jPanel30, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(btnGuardar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnLimpia, javax.swing.GroupLayout.DEFAULT_SIZE, 36, Short.MAX_VALUE))))
                .addContainerGap(281, Short.MAX_VALUE))
        );

        jScrollPane5.setViewportView(jPanel9);

        contenedorPestañas.addTab("CIF", jScrollPane5);

        jScrollPane7.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        jScrollPane7.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        jPanel20.setBackground(new java.awt.Color(204, 204, 204));
        jPanel20.setPreferredSize(new java.awt.Dimension(1631, 800));

        jLabel92.setBackground(new java.awt.Color(153, 255, 255));
        jLabel92.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel92.setForeground(new java.awt.Color(51, 51, 51));
        jLabel92.setText("CUENTA");

        jLabel93.setBackground(new java.awt.Color(153, 255, 255));
        jLabel93.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel93.setForeground(new java.awt.Color(51, 51, 51));
        jLabel93.setText("DESCRIPCIÓN");

        jPanel21.setBackground(new java.awt.Color(153, 153, 153));

        jLabel94.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel94.setText("ÁREA DE COSTOS PRODUCTIVOS");

        jLabel95.setText("DISEÑO");

        jLabel96.setText("ANÁLISIS");

        txtSalarioSupAnalisisProductivo.setEditable(false);

        txtSalarioOfiAnalisisProductivo.setEditable(false);

        txtManoAnalisisProductivo.setEditable(false);

        txtMaterialAnalisisProductivo.setEditable(false);

        txtSumiAnalisisProductivo.setEditable(false);

        txtHerraAnalisisProductivo.setEditable(false);

        txtOtrosAnalisisProductivo.setEditable(false);

        txtDepreciacionAnalisisProductivo.setEditable(false);

        txtImpuestosAnalisisProductivo.setEditable(false);

        txtServicioPublicoAnalisisProductivo.setEditable(false);

        txtServicioPrivadoAnalisisProductivo.setEditable(false);

        txtCalidadAnalisisProductivo.setEditable(false);

        txtInsumoAnalisisProductivo.setEditable(false);

        txtMantenimientoSoftAnalisisProductivo.setEditable(false);

        txtInfraestructuraAnalisisProductivo.setEditable(false);

        txtImpuestosDisenoProductivo.setEditable(false);

        txtSalarioSupDisenoProductivo.setEditable(false);

        txtServicioPublicoDisenoProductivo.setEditable(false);

        txtSalarioOfiDisenoProductivo.setEditable(false);

        txtServicioPrivadoDisenoProductivo.setEditable(false);

        txtManoDisenoProductivo.setEditable(false);

        txtMaterialDisenoProductivo.setEditable(false);

        txtCalidadDisenoProductivo.setEditable(false);

        txtSumiDisenoProductivo.setEditable(false);

        txtInsumoDisenoProductivo.setEditable(false);

        txtHerraDisenoProductivo.setEditable(false);

        txtMantenimientoSoftDisenoProductivo.setEditable(false);

        txtOtrosDisenoProductivo.setEditable(false);

        txtInfraestructuraDisenoProductivo.setEditable(false);

        txtDepreciacionDisenoProductivo.setEditable(false);

        txtImpuestosDesarrolloProductivo.setEditable(false);

        txtSalarioSupDesarrolloProductivo.setEditable(false);

        txtServicioPublicoDesarrolloProductivo.setEditable(false);

        txtSalarioOfiDesarrolloProductivo.setEditable(false);

        txtServicioPrivadoDesarrolloProductivo.setEditable(false);

        txtManoDesarrolloProductivo.setEditable(false);

        txtMaterialDesarrolloProductivo.setEditable(false);

        txtCalidadDesarrolloProductivo.setEditable(false);

        txtSumiDesarrolloProductivo.setEditable(false);

        txtInsumoDesarrolloProductivo.setEditable(false);

        txtHerraDesarrolloProductivo.setEditable(false);

        txtMantenimientoSoftDesarrolloProductivo.setEditable(false);

        txtOtrosDesarrolloProductivo.setEditable(false);

        txtInfraestructuraDesarrolloProductivo.setEditable(false);

        txtDepreciacionDesarrolloProductivo.setEditable(false);

        jLabel112.setText("DESARROLLO");

        txtServicioPublicoPruebaProductivo.setEditable(false);

        txtSalarioOfiPruebaProductivo.setEditable(false);

        txtServicioPrivadoPruebaProductivo.setEditable(false);

        txtManoPruebaProductivo.setEditable(false);

        txtMaterialPruebaProductivo.setEditable(false);

        txtCalidadPruebaProductivo.setEditable(false);

        txtSumiPruebaProductivo.setEditable(false);

        txtInsumoPruebaProductivo.setEditable(false);

        txtHerraPruebaProductivo.setEditable(false);

        txtMantenimientoSoftPruebaProductivo.setEditable(false);

        txtOtrosPruebaProductivo.setEditable(false);

        txtInfraestructuraPruebaProductivo.setEditable(false);

        txtDepreciacionPruebaProductivo.setEditable(false);

        txtImpuestosPruebaProductivo.setEditable(false);

        txtSalarioSupPruebaProductivo.setEditable(false);

        txtServicioPublicoMantenimientoProductivo.setEditable(false);

        txtSalarioOfiMantenimientoProductivo.setEditable(false);

        txtServicioPrivadoMantenimientoProductivo.setEditable(false);

        txtManoMantenimientoProductivo.setEditable(false);

        txtMaterialMantenimientoProductivo.setEditable(false);

        txtCalidadMantenimientoProductivo.setEditable(false);

        txtSumiMantenimientoProductivo.setEditable(false);

        txtInsumoMantenimientoProductivo.setEditable(false);

        txtHerraMantenimientoProductivo.setEditable(false);

        txtMantenimientoSoftMantenimientoProductivo.setEditable(false);

        txtOtrosMantenimientoProductivo.setEditable(false);

        txtInfraestructuraMantenimientoProductivo.setEditable(false);

        txtDepreciacionMantenimientoProductivo.setEditable(false);

        txtImpuestosMantenimientoProductivo.setEditable(false);

        txtSalarioSupMantenimientoProductivo.setEditable(false);

        jLabel113.setText("PRUEBAS");

        jLabel117.setText("MANTENIMIENTO");

        txtTotalAnalisis.setEditable(false);

        txtTotalDiseno.setEditable(false);

        txtTotalDesarrollo.setEditable(false);

        txtTotalPrueba.setEditable(false);

        txtTotalMantenimiento.setEditable(false);

        javax.swing.GroupLayout jPanel21Layout = new javax.swing.GroupLayout(jPanel21);
        jPanel21.setLayout(jPanel21Layout);
        jPanel21Layout.setHorizontalGroup(
            jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel21Layout.createSequentialGroup()
                .addGroup(jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel21Layout.createSequentialGroup()
                        .addGap(41, 41, 41)
                        .addGroup(jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel21Layout.createSequentialGroup()
                                .addGroup(jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(txtServicioPrivadoAnalisisProductivo, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(txtImpuestosAnalisisProductivo)
                                        .addComponent(txtServicioPublicoAnalisisProductivo, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(txtCalidadAnalisisProductivo)
                                        .addComponent(txtInsumoAnalisisProductivo, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(txtMantenimientoSoftAnalisisProductivo)
                                        .addComponent(txtInfraestructuraAnalisisProductivo, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(txtManoAnalisisProductivo)
                                        .addComponent(txtMaterialAnalisisProductivo, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(txtSalarioSupAnalisisProductivo)
                                        .addComponent(txtSalarioOfiAnalisisProductivo, javax.swing.GroupLayout.DEFAULT_SIZE, 85, Short.MAX_VALUE))
                                    .addGroup(jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(txtSumiAnalisisProductivo)
                                        .addComponent(txtHerraAnalisisProductivo, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(txtOtrosAnalisisProductivo)
                                        .addComponent(txtDepreciacionAnalisisProductivo, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(txtTotalAnalisis, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(34, 34, 34)
                                .addGroup(jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(txtServicioPrivadoDisenoProductivo, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(txtImpuestosDisenoProductivo)
                                        .addComponent(txtServicioPublicoDisenoProductivo, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(txtCalidadDisenoProductivo)
                                        .addComponent(txtInsumoDisenoProductivo, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(txtMantenimientoSoftDisenoProductivo)
                                        .addComponent(txtInfraestructuraDisenoProductivo, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(txtManoDisenoProductivo)
                                        .addComponent(txtMaterialDisenoProductivo, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(txtSalarioSupDisenoProductivo)
                                        .addComponent(txtSalarioOfiDisenoProductivo, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(txtSumiDisenoProductivo)
                                        .addComponent(txtHerraDisenoProductivo, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(txtOtrosDisenoProductivo)
                                        .addComponent(txtDepreciacionDisenoProductivo, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(txtTotalDiseno, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(jPanel21Layout.createSequentialGroup()
                                .addGap(14, 14, 14)
                                .addComponent(jLabel96)
                                .addGap(73, 73, 73)
                                .addComponent(jLabel95)))
                        .addGap(30, 30, 30)
                        .addGroup(jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addGroup(jPanel21Layout.createSequentialGroup()
                                    .addGroup(jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(txtServicioPrivadoDesarrolloProductivo, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGroup(jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(txtImpuestosDesarrolloProductivo)
                                            .addComponent(txtServicioPublicoDesarrolloProductivo, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(txtCalidadDesarrolloProductivo)
                                            .addComponent(txtInsumoDesarrolloProductivo, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(txtMantenimientoSoftDesarrolloProductivo)
                                            .addComponent(txtInfraestructuraDesarrolloProductivo, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(txtManoDesarrolloProductivo)
                                            .addComponent(txtMaterialDesarrolloProductivo, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(txtSalarioSupDesarrolloProductivo)
                                            .addComponent(txtSalarioOfiDesarrolloProductivo, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(txtSumiDesarrolloProductivo)
                                            .addComponent(txtHerraDesarrolloProductivo, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(txtOtrosDesarrolloProductivo)
                                            .addComponent(txtDepreciacionDesarrolloProductivo, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                    .addGap(27, 27, 27)
                                    .addGroup(jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(txtServicioPrivadoPruebaProductivo, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGroup(jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(txtImpuestosPruebaProductivo)
                                            .addComponent(txtServicioPublicoPruebaProductivo, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(txtCalidadPruebaProductivo)
                                            .addComponent(txtInsumoPruebaProductivo, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(txtMantenimientoSoftPruebaProductivo)
                                            .addComponent(txtInfraestructuraPruebaProductivo, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(txtManoPruebaProductivo)
                                            .addComponent(txtMaterialPruebaProductivo, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(txtSalarioSupPruebaProductivo)
                                            .addComponent(txtSalarioOfiPruebaProductivo, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(txtSumiPruebaProductivo)
                                            .addComponent(txtHerraPruebaProductivo, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(txtOtrosPruebaProductivo)
                                            .addComponent(txtDepreciacionPruebaProductivo, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addComponent(txtTotalPrueba, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGap(27, 27, 27))
                                .addGroup(jPanel21Layout.createSequentialGroup()
                                    .addComponent(jLabel112)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabel113)
                                    .addGap(48, 48, 48)))
                            .addGroup(jPanel21Layout.createSequentialGroup()
                                .addComponent(txtTotalDesarrollo, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(139, 139, 139)))
                        .addGroup(jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel117, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(txtSalarioSupMantenimientoProductivo)
                            .addComponent(txtSalarioOfiMantenimientoProductivo)
                            .addComponent(txtManoMantenimientoProductivo)
                            .addComponent(txtMaterialMantenimientoProductivo)
                            .addComponent(txtSumiMantenimientoProductivo)
                            .addComponent(txtHerraMantenimientoProductivo)
                            .addComponent(txtOtrosMantenimientoProductivo)
                            .addComponent(txtDepreciacionMantenimientoProductivo)
                            .addComponent(txtImpuestosMantenimientoProductivo)
                            .addComponent(txtServicioPublicoMantenimientoProductivo)
                            .addComponent(txtServicioPrivadoMantenimientoProductivo)
                            .addComponent(txtCalidadMantenimientoProductivo)
                            .addComponent(txtInsumoMantenimientoProductivo)
                            .addComponent(txtMantenimientoSoftMantenimientoProductivo)
                            .addComponent(txtInfraestructuraMantenimientoProductivo)
                            .addComponent(txtTotalMantenimiento)))
                    .addGroup(jPanel21Layout.createSequentialGroup()
                        .addGap(214, 214, 214)
                        .addComponent(jLabel94, javax.swing.GroupLayout.PREFERRED_SIZE, 216, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(39, Short.MAX_VALUE))
        );
        jPanel21Layout.setVerticalGroup(
            jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel21Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel94)
                .addGap(12, 12, 12)
                .addGroup(jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel21Layout.createSequentialGroup()
                        .addComponent(jLabel117)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtSalarioSupMantenimientoProductivo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtSalarioOfiMantenimientoProductivo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtManoMantenimientoProductivo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtMaterialMantenimientoProductivo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtSumiMantenimientoProductivo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtHerraMantenimientoProductivo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtOtrosMantenimientoProductivo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtDepreciacionMantenimientoProductivo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtImpuestosMantenimientoProductivo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtServicioPublicoMantenimientoProductivo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtServicioPrivadoMantenimientoProductivo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(51, 51, 51)
                        .addComponent(txtCalidadMantenimientoProductivo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtInsumoMantenimientoProductivo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtMantenimientoSoftMantenimientoProductivo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(34, 34, 34))
                    .addGroup(jPanel21Layout.createSequentialGroup()
                        .addGroup(jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel96, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel95)
                                .addComponent(jLabel112)
                                .addComponent(jLabel113)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel21Layout.createSequentialGroup()
                                .addComponent(txtSalarioSupAnalisisProductivo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtSalarioOfiAnalisisProductivo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtManoAnalisisProductivo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtMaterialAnalisisProductivo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtSumiAnalisisProductivo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtHerraAnalisisProductivo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtOtrosAnalisisProductivo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtDepreciacionAnalisisProductivo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtImpuestosAnalisisProductivo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtServicioPublicoAnalisisProductivo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtServicioPrivadoAnalisisProductivo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(51, 51, 51)
                                .addComponent(txtCalidadAnalisisProductivo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtInsumoAnalisisProductivo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtMantenimientoSoftAnalisisProductivo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtInfraestructuraAnalisisProductivo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel21Layout.createSequentialGroup()
                                .addComponent(txtSalarioSupDisenoProductivo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtSalarioOfiDisenoProductivo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtManoDisenoProductivo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtMaterialDisenoProductivo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtSumiDisenoProductivo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtHerraDisenoProductivo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtOtrosDisenoProductivo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtDepreciacionDisenoProductivo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtImpuestosDisenoProductivo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtServicioPublicoDisenoProductivo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtServicioPrivadoDisenoProductivo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(51, 51, 51)
                                .addComponent(txtCalidadDisenoProductivo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtInsumoDisenoProductivo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtMantenimientoSoftDisenoProductivo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtInfraestructuraDisenoProductivo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel21Layout.createSequentialGroup()
                                .addComponent(txtSalarioSupDesarrolloProductivo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtSalarioOfiDesarrolloProductivo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtManoDesarrolloProductivo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtMaterialDesarrolloProductivo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtSumiDesarrolloProductivo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtHerraDesarrolloProductivo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtOtrosDesarrolloProductivo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtDepreciacionDesarrolloProductivo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtImpuestosDesarrolloProductivo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtServicioPublicoDesarrolloProductivo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtServicioPrivadoDesarrolloProductivo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(51, 51, 51)
                                .addComponent(txtCalidadDesarrolloProductivo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtInsumoDesarrolloProductivo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtMantenimientoSoftDesarrolloProductivo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtInfraestructuraDesarrolloProductivo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel21Layout.createSequentialGroup()
                                .addComponent(txtSalarioSupPruebaProductivo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtSalarioOfiPruebaProductivo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtManoPruebaProductivo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtMaterialPruebaProductivo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtSumiPruebaProductivo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtHerraPruebaProductivo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtOtrosPruebaProductivo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtDepreciacionPruebaProductivo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtImpuestosPruebaProductivo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtServicioPublicoPruebaProductivo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtServicioPrivadoPruebaProductivo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(51, 51, 51)
                                .addComponent(txtCalidadPruebaProductivo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtInsumoPruebaProductivo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtMantenimientoSoftPruebaProductivo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(txtInfraestructuraPruebaProductivo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(txtInfraestructuraMantenimientoProductivo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))))
                .addGap(29, 29, 29)
                .addGroup(jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtTotalAnalisis, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtTotalDiseno, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtTotalDesarrollo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtTotalPrueba, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtTotalMantenimiento, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(29, Short.MAX_VALUE))
        );

        jLabel97.setBackground(new java.awt.Color(153, 255, 255));
        jLabel97.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel97.setForeground(new java.awt.Color(51, 51, 51));
        jLabel97.setText("Salarios Supervisión");

        jLabel98.setBackground(new java.awt.Color(153, 255, 255));
        jLabel98.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel98.setForeground(new java.awt.Color(51, 51, 51));
        jLabel98.setText("Salarios Oficina");

        jLabel99.setBackground(new java.awt.Color(153, 255, 255));
        jLabel99.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel99.setForeground(new java.awt.Color(51, 51, 51));
        jLabel99.setText("Mano de Obra Indirecta");

        jLabel100.setBackground(new java.awt.Color(153, 255, 255));
        jLabel100.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel100.setForeground(new java.awt.Color(51, 51, 51));
        jLabel100.setText("Materiales Indirectos");

        jLabel101.setBackground(new java.awt.Color(153, 255, 255));
        jLabel101.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel101.setForeground(new java.awt.Color(51, 51, 51));
        jLabel101.setText("Suministros");

        jLabel102.setBackground(new java.awt.Color(153, 255, 255));
        jLabel102.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel102.setForeground(new java.awt.Color(51, 51, 51));
        jLabel102.setText("Harramientas");

        jLabel103.setBackground(new java.awt.Color(153, 255, 255));
        jLabel103.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel103.setForeground(new java.awt.Color(51, 51, 51));
        jLabel103.setText("531");

        jLabel104.setBackground(new java.awt.Color(153, 255, 255));
        jLabel104.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel104.setForeground(new java.awt.Color(51, 51, 51));
        jLabel104.setText("532");

        jLabel105.setBackground(new java.awt.Color(153, 255, 255));
        jLabel105.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel105.setForeground(new java.awt.Color(51, 51, 51));
        jLabel105.setText("533");

        jLabel106.setBackground(new java.awt.Color(153, 255, 255));
        jLabel106.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel106.setForeground(new java.awt.Color(51, 51, 51));
        jLabel106.setText("534");

        jLabel107.setBackground(new java.awt.Color(153, 255, 255));
        jLabel107.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel107.setForeground(new java.awt.Color(51, 51, 51));
        jLabel107.setText("Otros Materiales");

        jLabel108.setBackground(new java.awt.Color(153, 255, 255));
        jLabel108.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel108.setForeground(new java.awt.Color(51, 51, 51));
        jLabel108.setText("Depreciación");

        jLabel109.setBackground(new java.awt.Color(153, 255, 255));
        jLabel109.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel109.setForeground(new java.awt.Color(51, 51, 51));
        jLabel109.setText("Impuestos");

        jLabel110.setBackground(new java.awt.Color(153, 255, 255));
        jLabel110.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel110.setForeground(new java.awt.Color(51, 51, 51));
        jLabel110.setText("Servicios Públicos");

        jLabel111.setBackground(new java.awt.Color(153, 255, 255));
        jLabel111.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel111.setForeground(new java.awt.Color(51, 51, 51));
        jLabel111.setText("Servicios Privados");

        jLabel114.setBackground(new java.awt.Color(153, 255, 255));
        jLabel114.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel114.setForeground(new java.awt.Color(51, 51, 51));
        jLabel114.setText("Insumos y Materiales");

        jLabel115.setBackground(new java.awt.Color(153, 255, 255));
        jLabel115.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel115.setForeground(new java.awt.Color(51, 51, 51));
        jLabel115.setText("Mantenimiento de software");

        jLabel116.setBackground(new java.awt.Color(153, 255, 255));
        jLabel116.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel116.setForeground(new java.awt.Color(51, 51, 51));
        jLabel116.setText("Infraestructura y Limpieza");

        jLabel120.setBackground(new java.awt.Color(153, 255, 255));
        jLabel120.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel120.setForeground(new java.awt.Color(51, 51, 51));
        jLabel120.setText("537");

        jLabel121.setBackground(new java.awt.Color(153, 255, 255));
        jLabel121.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel121.setForeground(new java.awt.Color(51, 51, 51));
        jLabel121.setText("538");

        jLabel122.setBackground(new java.awt.Color(153, 255, 255));
        jLabel122.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel122.setForeground(new java.awt.Color(51, 51, 51));
        jLabel122.setText("535");

        jLabel123.setBackground(new java.awt.Color(153, 255, 255));
        jLabel123.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel123.setForeground(new java.awt.Color(51, 51, 51));
        jLabel123.setText("536");

        jLabel124.setBackground(new java.awt.Color(153, 255, 255));
        jLabel124.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel124.setForeground(new java.awt.Color(51, 51, 51));
        jLabel124.setText("613");

        jLabel126.setBackground(new java.awt.Color(153, 255, 255));
        jLabel126.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel126.setForeground(new java.awt.Color(51, 51, 51));
        jLabel126.setText("539");

        jLabel127.setBackground(new java.awt.Color(153, 255, 255));
        jLabel127.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel127.setForeground(new java.awt.Color(51, 51, 51));
        jLabel127.setText("612");

        jLabel125.setBackground(new java.awt.Color(153, 255, 255));
        jLabel125.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel125.setForeground(new java.awt.Color(51, 51, 51));
        jLabel125.setText("Calidad");

        jLabel119.setBackground(new java.awt.Color(153, 255, 255));
        jLabel119.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel119.setForeground(new java.awt.Color(51, 51, 51));
        jLabel119.setText("TOTALES");

        jLabel128.setBackground(new java.awt.Color(153, 255, 255));
        jLabel128.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel128.setForeground(new java.awt.Color(51, 51, 51));
        jLabel128.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel128.setText("TOTALES");

        jLabel129.setBackground(new java.awt.Color(153, 255, 255));
        jLabel129.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel129.setForeground(new java.awt.Color(51, 51, 51));
        jLabel129.setText("618");

        jLabel130.setBackground(new java.awt.Color(153, 255, 255));
        jLabel130.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel130.setForeground(new java.awt.Color(51, 51, 51));
        jLabel130.setText("619");

        jLabel131.setBackground(new java.awt.Color(153, 255, 255));
        jLabel131.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel131.setForeground(new java.awt.Color(51, 51, 51));
        jLabel131.setText("620");

        jLabel132.setBackground(new java.awt.Color(153, 255, 255));
        jLabel132.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel132.setForeground(new java.awt.Color(51, 51, 51));
        jLabel132.setText("621");

        jPanel22.setBackground(new java.awt.Color(153, 153, 153));

        jLabel118.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel118.setText("ÁREA DE COSTOS SOBRE SERVICIOS");

        jLabel133.setText("INSUMOS Y MATERIALES ");

        jLabel134.setText("CALIDAD");

        txtSalarioSupCalidadServicio.setEditable(false);

        txtSalarioOfiCalidadServicio.setEditable(false);

        txtManoCalidadServicio.setEditable(false);

        txtMaterialCalidadServicio.setEditable(false);

        txtSumiCalidadServicio.setEditable(false);

        txtHerraCalidadServicio.setEditable(false);

        txtOtrosCalidadServicio.setEditable(false);

        txtDepreciacionCalidadServicio.setEditable(false);

        txtImpuestosCalidadServicio.setEditable(false);

        txtServicioPublicoCalidadServicio.setEditable(false);

        txtServicioPrivadoCalidadServicio.setEditable(false);

        txtCalidadCalidadServicio.setEditable(false);

        txtInsumoCalidadServicio.setEditable(false);

        txtMantenimientoSoftCalidadServicio.setEditable(false);

        txtInfraestructuraCalidadServicio.setEditable(false);

        txtImpuestosInsumoServicio.setEditable(false);

        txtSalarioSupInsumoServicio.setEditable(false);

        txtServicioPublicoInsumoServicio.setEditable(false);

        txtSalarioOfiInsumosServicio.setEditable(false);

        txtServicioPrivadoInsumoServicio.setEditable(false);

        txtManoInsumoServicio.setEditable(false);

        txtMaterialInsumoServicio.setEditable(false);

        txtCalidadInsumoServicio.setEditable(false);

        txtSumiInsumoServicio.setEditable(false);

        txtInsumoInsumoServicio.setEditable(false);

        txtHerraInsumoServicio.setEditable(false);

        txtMantenimientoSoftInsumoServicio.setEditable(false);

        txtOtrosInsumoServicio.setEditable(false);

        txtInfraestructuraInsumoServicio.setEditable(false);

        txtDepreciacionInsumoServicio.setEditable(false);

        txtImpuestosMantenimientoSoftServicio.setEditable(false);

        txtSalarioSupMantenimientoSoftServicio.setEditable(false);

        txtServicioPublicoMantenimientoSoftServicio.setEditable(false);

        txtSalarioOfiMantenimientoSoftServicio.setEditable(false);

        txtServicioPrivadoMantenimientoSoftServicio.setEditable(false);

        txtManoMantenimientoSoftServicio.setEditable(false);

        txtMaterialMantenimientoSoftServicio.setEditable(false);

        txtCalidadMantenimientoSoftServicio.setEditable(false);

        txtSumiMantenimientoSoftServicio.setEditable(false);

        txtInsumoMantenimientoSoftServicio.setEditable(false);

        txtHerraMantenimientoSoftServicio.setEditable(false);

        txtMantenimientoSoftMantenimientoSoftServicio.setEditable(false);

        txtOtrosMantenimientoSoftServicio.setEditable(false);

        txtInfraestructuraMantenimientoSoftServicio.setEditable(false);

        txtDepreciacionMantenimientoSoftServicio.setEditable(false);

        jLabel135.setText("MANTENIMI.. SOFTWARE");

        txtServicioPublicoInfraestructuraServicio.setEditable(false);

        txtSalarioOfiInfraestructuraServicio.setEditable(false);

        txtServicioPrivadoInfraestructuraServicio.setEditable(false);

        txtManoInfraestructuraServicio.setEditable(false);

        txtMaterialInfraestructuraServicio.setEditable(false);

        txtCalidadInfraestructuraServicio.setEditable(false);

        txtSumiInfraestructuraServicio.setEditable(false);

        txtInsumoInfraestructuraServicio.setEditable(false);

        txtHerraInfraestructuraServicio.setEditable(false);

        txtMantenimientoSoftInfraestructuraServicio.setEditable(false);

        txtOtrosInfraestructuraServicio.setEditable(false);

        txtInfraestructuraInfraestructuraServicio.setEditable(false);

        txtDepreciacionInfraestructuraServicio.setEditable(false);

        txtImpuestosInfraestructuraServicio.setEditable(false);

        txtSalarioSupInfraestructuraServicio.setEditable(false);

        txtTotalServicioPublico.setEditable(false);

        txtTotalSalarioOfi.setEditable(false);

        txtTotalServicioPrivado.setEditable(false);

        txtTotalMano.setEditable(false);

        txtTotalMaterial.setEditable(false);

        txtTotalCalidad.setEditable(false);

        txtTotalSumi.setEditable(false);

        txtTotalInsumo.setEditable(false);

        txtTotalHerra.setEditable(false);

        txtTotalMantenimientoSoft.setEditable(false);

        txtTotalOtros.setEditable(false);

        txtTotalInfraestructura.setEditable(false);

        txtTotalDepreciacion.setEditable(false);

        txtTotalImpuesto.setEditable(false);

        txtTotalSalarioSup.setEditable(false);

        jLabel136.setText("INFRAESTRUCTURA");

        jLabel137.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel137.setText("TOTALES");

        txtTotalPresupuesto.setEditable(false);

        txtTotalVCalidad.setEditable(false);

        txtTotalVInsumo.setEditable(false);

        txtTotalTotal.setEditable(false);

        txtTotalVInfraestructura.setEditable(false);

        txtTotalVMantenimientoSoft.setEditable(false);

        javax.swing.GroupLayout jPanel22Layout = new javax.swing.GroupLayout(jPanel22);
        jPanel22.setLayout(jPanel22Layout);
        jPanel22Layout.setHorizontalGroup(
            jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel22Layout.createSequentialGroup()
                .addGap(246, 246, 246)
                .addComponent(jLabel118, javax.swing.GroupLayout.PREFERRED_SIZE, 216, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(jPanel22Layout.createSequentialGroup()
                .addGap(44, 44, 44)
                .addGroup(jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel22Layout.createSequentialGroup()
                        .addComponent(txtTotalVCalidad, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel22Layout.createSequentialGroup()
                        .addGroup(jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel22Layout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addComponent(txtTotalPresupuesto, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel22Layout.createSequentialGroup()
                                .addGroup(jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(jPanel22Layout.createSequentialGroup()
                                        .addGap(115, 115, 115)
                                        .addComponent(txtTotalVInsumo, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(jPanel22Layout.createSequentialGroup()
                                        .addGroup(jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(txtServicioPrivadoCalidadServicio, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addGroup(jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                .addComponent(txtImpuestosCalidadServicio)
                                                .addComponent(txtServicioPublicoCalidadServicio, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addGroup(jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                .addComponent(txtCalidadCalidadServicio)
                                                .addComponent(txtInsumoCalidadServicio, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addGroup(jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                .addComponent(txtMantenimientoSoftCalidadServicio)
                                                .addComponent(txtInfraestructuraCalidadServicio, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addGroup(jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                .addComponent(txtManoCalidadServicio)
                                                .addComponent(txtMaterialCalidadServicio, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addGroup(jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                .addComponent(txtSalarioSupCalidadServicio)
                                                .addComponent(txtSalarioOfiCalidadServicio, javax.swing.GroupLayout.DEFAULT_SIZE, 85, Short.MAX_VALUE))
                                            .addGroup(jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                .addComponent(txtSumiCalidadServicio)
                                                .addComponent(txtHerraCalidadServicio, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addGroup(jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                .addComponent(txtOtrosCalidadServicio)
                                                .addComponent(txtDepreciacionCalidadServicio, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addGroup(jPanel22Layout.createSequentialGroup()
                                                .addGap(14, 14, 14)
                                                .addComponent(jLabel134)))
                                        .addGap(13, 13, 13)
                                        .addGroup(jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addComponent(jLabel133, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addGroup(jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                .addComponent(txtServicioPublicoInsumoServicio, javax.swing.GroupLayout.Alignment.TRAILING)
                                                .addComponent(txtImpuestosInsumoServicio, javax.swing.GroupLayout.Alignment.TRAILING)
                                                .addComponent(txtDepreciacionInsumoServicio, javax.swing.GroupLayout.Alignment.TRAILING)
                                                .addComponent(txtOtrosInsumoServicio, javax.swing.GroupLayout.Alignment.TRAILING)
                                                .addComponent(txtHerraInsumoServicio, javax.swing.GroupLayout.Alignment.TRAILING)
                                                .addComponent(txtSumiInsumoServicio, javax.swing.GroupLayout.Alignment.TRAILING)
                                                .addComponent(txtMaterialInsumoServicio, javax.swing.GroupLayout.Alignment.TRAILING)
                                                .addComponent(txtManoInsumoServicio, javax.swing.GroupLayout.Alignment.TRAILING)
                                                .addComponent(txtSalarioOfiInsumosServicio, javax.swing.GroupLayout.Alignment.TRAILING)
                                                .addComponent(txtSalarioSupInsumoServicio, javax.swing.GroupLayout.Alignment.TRAILING)
                                                .addComponent(txtServicioPrivadoInsumoServicio, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 119, Short.MAX_VALUE))
                                            .addGroup(jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                .addComponent(txtInfraestructuraInsumoServicio, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 119, Short.MAX_VALUE)
                                                .addComponent(txtMantenimientoSoftInsumoServicio, javax.swing.GroupLayout.Alignment.TRAILING)
                                                .addComponent(txtInsumoInsumoServicio, javax.swing.GroupLayout.Alignment.TRAILING)
                                                .addComponent(txtCalidadInsumoServicio, javax.swing.GroupLayout.Alignment.TRAILING)))))
                                .addGap(18, 18, 18)
                                .addGroup(jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel22Layout.createSequentialGroup()
                                        .addGroup(jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(jPanel22Layout.createSequentialGroup()
                                                .addGroup(jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                                    .addComponent(txtSalarioSupMantenimientoSoftServicio, javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addComponent(txtSalarioOfiMantenimientoSoftServicio, javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addComponent(txtServicioPublicoMantenimientoSoftServicio, javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addComponent(txtImpuestosMantenimientoSoftServicio, javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addComponent(txtDepreciacionMantenimientoSoftServicio, javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addComponent(txtOtrosMantenimientoSoftServicio, javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addComponent(txtHerraMantenimientoSoftServicio, javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addComponent(txtServicioPrivadoMantenimientoSoftServicio, javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addComponent(txtMaterialMantenimientoSoftServicio, javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addComponent(txtManoMantenimientoSoftServicio)
                                                    .addComponent(txtSumiMantenimientoSoftServicio)
                                                    .addComponent(jLabel135, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 140, Short.MAX_VALUE))
                                                .addGap(25, 25, 25))
                                            .addGroup(jPanel22Layout.createSequentialGroup()
                                                .addComponent(txtTotalVMantenimientoSoft)
                                                .addGap(30, 30, 30)))
                                        .addGroup(jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(txtTotalVInfraestructura, javax.swing.GroupLayout.PREFERRED_SIZE, 103, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addGroup(jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                .addComponent(txtServicioPrivadoInfraestructuraServicio)
                                                .addComponent(txtImpuestosInfraestructuraServicio)
                                                .addComponent(txtServicioPublicoInfraestructuraServicio)
                                                .addComponent(txtCalidadInfraestructuraServicio)
                                                .addComponent(txtInsumoInfraestructuraServicio)
                                                .addComponent(txtMantenimientoSoftInfraestructuraServicio)
                                                .addComponent(txtInfraestructuraInfraestructuraServicio)
                                                .addComponent(txtManoInfraestructuraServicio)
                                                .addComponent(txtMaterialInfraestructuraServicio)
                                                .addComponent(txtSalarioSupInfraestructuraServicio)
                                                .addComponent(txtSalarioOfiInfraestructuraServicio)
                                                .addComponent(txtSumiInfraestructuraServicio)
                                                .addComponent(txtHerraInfraestructuraServicio)
                                                .addComponent(txtOtrosInfraestructuraServicio)
                                                .addComponent(txtDepreciacionInfraestructuraServicio)
                                                .addComponent(jLabel136, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                                        .addGap(32, 32, 32)
                                        .addGroup(jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jLabel137, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(txtTotalMaterial)
                                            .addComponent(txtTotalSumi)
                                            .addComponent(txtTotalHerra)
                                            .addComponent(txtTotalOtros)
                                            .addComponent(txtTotalDepreciacion)
                                            .addComponent(txtTotalImpuesto)
                                            .addComponent(txtTotalServicioPublico)
                                            .addComponent(txtTotalServicioPrivado)
                                            .addComponent(txtTotalCalidad)
                                            .addComponent(txtTotalInsumo)
                                            .addComponent(txtTotalMantenimientoSoft)
                                            .addComponent(txtTotalInfraestructura)
                                            .addComponent(txtTotalSalarioOfi)
                                            .addComponent(txtTotalMano)
                                            .addGroup(jPanel22Layout.createSequentialGroup()
                                                .addGroup(jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addComponent(txtTotalTotal, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addComponent(txtTotalSalarioSup, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addGap(0, 2, Short.MAX_VALUE))))
                                    .addGroup(jPanel22Layout.createSequentialGroup()
                                        .addGroup(jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(txtInsumoMantenimientoSoftServicio, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(txtMantenimientoSoftMantenimientoSoftServicio, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(txtInfraestructuraMantenimientoSoftServicio, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(txtCalidadMantenimientoSoftServicio, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGap(0, 0, Short.MAX_VALUE)))))
                        .addGap(41, 41, 41))))
        );
        jPanel22Layout.setVerticalGroup(
            jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel22Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel118)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel22Layout.createSequentialGroup()
                        .addComponent(jLabel137)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtTotalSalarioSup, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtTotalSalarioOfi, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtTotalMano, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtTotalMaterial, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtTotalSumi, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtTotalHerra, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtTotalOtros, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtTotalDepreciacion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtTotalImpuesto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtTotalServicioPublico, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtTotalServicioPrivado, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtTotalPresupuesto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(17, 17, 17)
                        .addComponent(txtTotalCalidad, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtTotalInsumo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtTotalMantenimientoSoft, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtTotalInfraestructura, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel22Layout.createSequentialGroup()
                        .addGroup(jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel134)
                            .addGroup(jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel133)
                                .addComponent(jLabel135)
                                .addComponent(jLabel136)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel22Layout.createSequentialGroup()
                                .addComponent(txtSalarioSupCalidadServicio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtSalarioOfiCalidadServicio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtManoCalidadServicio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtMaterialCalidadServicio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtSumiCalidadServicio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtHerraCalidadServicio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtOtrosCalidadServicio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtDepreciacionCalidadServicio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtImpuestosCalidadServicio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtServicioPublicoCalidadServicio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtServicioPrivadoCalidadServicio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(51, 51, 51)
                                .addComponent(txtCalidadCalidadServicio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtInsumoCalidadServicio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtMantenimientoSoftCalidadServicio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtInfraestructuraCalidadServicio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel22Layout.createSequentialGroup()
                                .addComponent(txtSalarioSupInsumoServicio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtSalarioOfiInsumosServicio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtManoInsumoServicio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtMaterialInsumoServicio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtSumiInsumoServicio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtHerraInsumoServicio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtOtrosInsumoServicio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtDepreciacionInsumoServicio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtImpuestosInsumoServicio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtServicioPublicoInsumoServicio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtServicioPrivadoInsumoServicio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(51, 51, 51)
                                .addComponent(txtCalidadInsumoServicio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtInsumoInsumoServicio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtMantenimientoSoftInsumoServicio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtInfraestructuraInsumoServicio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel22Layout.createSequentialGroup()
                                .addGroup(jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(txtSalarioSupMantenimientoSoftServicio)
                                    .addComponent(txtSalarioSupInfraestructuraServicio))
                                .addGroup(jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel22Layout.createSequentialGroup()
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(txtSalarioOfiInfraestructuraServicio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(txtManoInfraestructuraServicio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(txtMaterialInfraestructuraServicio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(txtSumiInfraestructuraServicio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(txtHerraInfraestructuraServicio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(txtOtrosInfraestructuraServicio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(txtDepreciacionInfraestructuraServicio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(txtImpuestosInfraestructuraServicio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(txtServicioPublicoInfraestructuraServicio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(txtServicioPrivadoInfraestructuraServicio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(51, 51, 51)
                                        .addComponent(txtCalidadInfraestructuraServicio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(txtInsumoInfraestructuraServicio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(txtMantenimientoSoftInfraestructuraServicio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(txtInfraestructuraInfraestructuraServicio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel22Layout.createSequentialGroup()
                                        .addGap(12, 12, 12)
                                        .addComponent(txtSalarioOfiMantenimientoSoftServicio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(txtManoMantenimientoSoftServicio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(txtMaterialMantenimientoSoftServicio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(txtSumiMantenimientoSoftServicio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(txtHerraMantenimientoSoftServicio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(txtOtrosMantenimientoSoftServicio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(txtDepreciacionMantenimientoSoftServicio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(txtImpuestosMantenimientoSoftServicio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(txtServicioPublicoMantenimientoSoftServicio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(txtServicioPrivadoMantenimientoSoftServicio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(51, 51, 51)
                                        .addComponent(txtCalidadMantenimientoSoftServicio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(txtInsumoMantenimientoSoftServicio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(txtMantenimientoSoftMantenimientoSoftServicio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(txtInfraestructuraMantenimientoSoftServicio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 27, Short.MAX_VALUE)
                .addGroup(jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtTotalVCalidad, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtTotalVInsumo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtTotalTotal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtTotalVInfraestructura, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtTotalVMantenimientoSoft, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(31, 31, 31))
        );

        javax.swing.GroupLayout jPanel20Layout = new javax.swing.GroupLayout(jPanel20);
        jPanel20.setLayout(jPanel20Layout);
        jPanel20Layout.setHorizontalGroup(
            jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel20Layout.createSequentialGroup()
                .addGroup(jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel20Layout.createSequentialGroup()
                        .addGap(31, 31, 31)
                        .addComponent(jLabel92)
                        .addGap(18, 18, 18)
                        .addGroup(jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(jLabel98, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel97, javax.swing.GroupLayout.DEFAULT_SIZE, 133, Short.MAX_VALUE)
                                .addGroup(jPanel20Layout.createSequentialGroup()
                                    .addGap(13, 13, 13)
                                    .addComponent(jLabel93)))
                            .addComponent(jLabel100, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(jLabel102, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel101, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(jLabel108, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel107, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(jLabel110, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel109, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabel111, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel99, javax.swing.GroupLayout.PREFERRED_SIZE, 144, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel20Layout.createSequentialGroup()
                        .addGap(38, 38, 38)
                        .addGroup(jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel124)
                            .addComponent(jLabel119, javax.swing.GroupLayout.PREFERRED_SIZE, 189, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel127)
                            .addComponent(jLabel126)
                            .addComponent(jLabel121)
                            .addComponent(jLabel120)
                            .addComponent(jLabel123)
                            .addComponent(jLabel122)
                            .addComponent(jLabel106)
                            .addComponent(jLabel105)
                            .addComponent(jLabel104)
                            .addComponent(jLabel103)
                            .addComponent(jLabel128, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel20Layout.createSequentialGroup()
                                .addGroup(jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel131)
                                    .addComponent(jLabel130)
                                    .addComponent(jLabel129)
                                    .addComponent(jLabel132))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel116, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel114, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel125, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel115, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE))))))
                .addGap(23, 23, 23)
                .addComponent(jPanel21, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel22, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel20Layout.setVerticalGroup(
            jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel20Layout.createSequentialGroup()
                .addGroup(jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel20Layout.createSequentialGroup()
                        .addGap(38, 38, 38)
                        .addGroup(jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel92)
                            .addComponent(jLabel93))
                        .addGap(23, 23, 23)
                        .addGroup(jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel97)
                            .addComponent(jLabel103))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel98)
                            .addComponent(jLabel104))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel99)
                            .addComponent(jLabel105))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel100)
                            .addComponent(jLabel106))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel101, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel122))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel102)
                            .addComponent(jLabel123))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel107)
                            .addComponent(jLabel120))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel108)
                            .addComponent(jLabel121))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel109)
                            .addComponent(jLabel126))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel110)
                            .addComponent(jLabel127))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel111)
                            .addComponent(jLabel124))
                        .addGap(18, 18, 18)
                        .addComponent(jLabel119, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addGroup(jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel125)
                            .addComponent(jLabel129))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel114)
                            .addComponent(jLabel130))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel115)
                            .addComponent(jLabel131))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel116)
                            .addComponent(jLabel132))
                        .addGap(37, 37, 37)
                        .addComponent(jLabel128, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel20Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jPanel21, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel22, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap(115, Short.MAX_VALUE))
        );

        jScrollPane7.setViewportView(jPanel20);

        contenedorPestañas.addTab("Calculos CIF", new javax.swing.ImageIcon(getClass().getResource("/img/calculadora.png")), jScrollPane7); // NOI18N

        jPanel28.setBackground(new java.awt.Color(255, 255, 255));
        jPanel28.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(51, 204, 255)));
        jPanel28.setForeground(new java.awt.Color(255, 255, 255));

        jLabel146.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel146.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel146.setText("Mano de obra real:");

        jLabel147.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel147.setText("Costos indirectos de fabricacion:");

        jLabel148.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel148.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel148.setText("Costo Total:");

        txtManoDeObra.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEtchedBorder(), new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED)));

        txtCostoTotal.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEtchedBorder(), new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED)));

        txtCIF.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEtchedBorder(), new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED)));

        jButton4.setBackground(new java.awt.Color(204, 204, 204));
        jButton4.setFont(new java.awt.Font("Segoe UI Black", 0, 14)); // NOI18N
        jButton4.setForeground(new java.awt.Color(51, 51, 51));
        jButton4.setText("CALCULAR COSTO TOTAL");
        jButton4.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED), new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED)));
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jButton5.setBackground(new java.awt.Color(204, 204, 204));
        jButton5.setFont(new java.awt.Font("Segoe UI Black", 0, 14)); // NOI18N
        jButton5.setForeground(new java.awt.Color(51, 51, 51));
        jButton5.setText("LIMPIAR");
        jButton5.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED), new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED)));
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel28Layout = new javax.swing.GroupLayout(jPanel28);
        jPanel28.setLayout(jPanel28Layout);
        jPanel28Layout.setHorizontalGroup(
            jPanel28Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel28Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(jPanel28Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel28Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(jLabel148, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel147, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jLabel146, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel28Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(txtCIF, javax.swing.GroupLayout.DEFAULT_SIZE, 123, Short.MAX_VALUE)
                    .addComponent(txtManoDeObra)
                    .addComponent(txtCostoTotal))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 74, Short.MAX_VALUE)
                .addGroup(jPanel28Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jButton4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(30, 30, 30))
        );
        jPanel28Layout.setVerticalGroup(
            jPanel28Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel28Layout.createSequentialGroup()
                .addGroup(jPanel28Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel28Layout.createSequentialGroup()
                        .addGap(38, 38, 38)
                        .addGroup(jPanel28Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel146)
                            .addComponent(txtManoDeObra, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel28Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel147)
                            .addComponent(txtCIF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel28Layout.createSequentialGroup()
                        .addGap(29, 29, 29)
                        .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel28Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel148)
                    .addComponent(txtCostoTotal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel149.setText("COSTO TOTAL");

        javax.swing.GroupLayout jPanel27Layout = new javax.swing.GroupLayout(jPanel27);
        jPanel27.setLayout(jPanel27Layout);
        jPanel27Layout.setHorizontalGroup(
            jPanel27Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel27Layout.createSequentialGroup()
                .addGroup(jPanel27Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel27Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jPanel28, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel27Layout.createSequentialGroup()
                        .addGap(231, 231, 231)
                        .addComponent(jLabel149)))
                .addContainerGap(1021, Short.MAX_VALUE))
        );
        jPanel27Layout.setVerticalGroup(
            jPanel27Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel27Layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addComponent(jLabel149)
                .addGap(18, 18, 18)
                .addComponent(jPanel28, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(546, Short.MAX_VALUE))
        );

        contenedorPestañas.addTab("Costo total", new javax.swing.ImageIcon(getClass().getResource("/img/presupuesto.png")), jPanel27); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(contenedorPestañas, javax.swing.GroupLayout.PREFERRED_SIZE, 1675, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(contenedorPestañas, javax.swing.GroupLayout.DEFAULT_SIZE, 812, Short.MAX_VALUE)
                .addContainerGap())
        );

        setSize(new java.awt.Dimension(1674, 831));
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void onClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_onClosing
         if(persistenciaDeDatos == null || informacionContable == null) return;
        
        try {
            persistenciaDeDatos.guardarDatos(informacionContable);
        } catch (IOException ex) {
            Logger.getLogger(Principal.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }//GEN-LAST:event_onClosing

    private void txtHorasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtHorasActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtHorasActionPerformed
    double valorTotalManoObra, totalCif;
    private void btnCalcularManoDeObraActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCalcularManoDeObraActionPerformed
        //Se obtienen los datos introducidos en los textbox
        DecimalFormat df = new DecimalFormat("#.00");
        double salario;
        int horasTrabajadas, diasAguinaldo, diasVacacion, diasTrabajados;
        float recargoVacacion, recargoSeguro, recargoAFP, eficienciaEmpleado, recargoINSAFORP;

        salario = Double.parseDouble(txtSalario.getText());
        horasTrabajadas = Integer.parseInt(txtHoras.getText());
        diasAguinaldo = Integer.parseInt(txtDiasAguinaldo.getText());
        diasVacacion = Integer.parseInt(txtDiasVacacion.getText());
        diasTrabajados = Integer.parseInt(txtDiasTrabajado.getText());
        recargoVacacion = Float.parseFloat(txtPorcentajeVacacion.getText());
        recargoSeguro = Float.parseFloat(txtPorcentajeSeguro.getText());
        recargoAFP = Float.parseFloat(txtPorcentajeAFP.getText());
        recargoINSAFORP = Float.parseFloat(txtPorcentajeINSAFORP.getText());
        eficienciaEmpleado = Float.parseFloat(txtPorcentajeEficiencia.getText());

        //Respectivas salidas
        //Var de proceso de la primera tabla
        double septimoDia, aguinaldo, vacacion, salud, afp, totalDia, totalHora, total, insaforp;
        //Var de proceso de tabla SIN eficiencia
        double factorDia, factorHora, factorSemana, factorSalarioDia, factorSalarioHora, factorSalarioSemana, totalDiaS, totalHoraS, totalSemanaS;
        //Var de proceso de tabla con eficiencia
        double factorDiaE, factorHoraE, factorSemanaE, factorSalarioDiaE, factorSalarioHoraE, factorSalarioSemanaE, totalDiaSE, totalHoraSE, totalSemanaSE;

        //calculos
        //PRIMERA TABLA
        septimoDia = 7*salario;
        vacacion = ((salario*diasVacacion*(1 + convierte(recargoVacacion))+ (salario*diasVacacion*convierte(recargoAFP)) + (salario*diasVacacion*convierte(recargoSeguro))))/52;
        aguinaldo = (salario*diasAguinaldo)/52;
        salud = (septimoDia + vacacion)*(convierte(recargoSeguro));
        afp = (septimoDia + vacacion)*(convierte(recargoAFP));
        insaforp = (septimoDia + vacacion)*(convierte(recargoINSAFORP));
        total = septimoDia + vacacion + aguinaldo + salud + afp + insaforp;
        valorTotalManoObra = total;
        totalDia = total/5;
        totalHora = totalDia/8;

        //TABLA SIN EFICIENCIA
        //Columna derecha
        factorSemana = (total)/(salario*5);
        factorDia = totalDia/salario;
        factorHora = (totalHora)/(salario/8);
        //columna del medio
        factorSalarioDia = salario;
        factorSalarioHora = salario/8;
        factorSalarioSemana = salario*diasTrabajados;
        //columna izquierda
        totalDiaS = factorSalarioDia*factorSemana;
        totalHoraS = factorSalarioHora*factorHora;
        totalSemanaS = factorSalarioSemana*factorSemana;

        //TABLA CON EFICIENCIA
        //columna derecha
        factorDiaE = (totalDia/(salario*(convierte(eficienciaEmpleado))));
        factorHoraE = (totalHora)/(salario*(convierte(eficienciaEmpleado))/8);
        factorSemanaE = (total)/(salario*5*(convierte(eficienciaEmpleado)));
        //columna izquierda
        totalDiaSE = totalDiaS;
        totalHoraSE = totalHoraS;
        totalSemanaSE = totalSemanaS;
        //columna del medio
        factorSalarioDiaE = totalDiaSE/factorDiaE;
        factorSalarioHoraE = totalHoraSE/factorHoraE;
        factorSalarioSemanaE = totalSemanaSE/factorSemanaE;

        //Impresion en los txtBox de la primera tabla
        txtVacacion2.setText(""+df.format(vacacion));
        txtSeptimo2.setText(""+df.format(septimoDia));
        txtAguinaldo2.setText(""+df.format(aguinaldo));
        txtSalud.setText(""+ df.format(salud));
        txtAFP.setText(""+df.format(afp));
        txtINSAFORP.setText(""+df.format(insaforp));
        txtTotal.setText(df.format(total));
        txtTotalDia.setText(""+df.format(totalDia));
        txtTotalHora.setText(""+df.format(totalHora));

        //Impresion en los txtBox de la tabla SIN EFICIENCIA
        txtFactorDia.setText("" + df.format(factorDia));
        txtFactorHora.setText("" + df.format(factorHora));
        txtFactorSemana.setText("" + df.format(factorSemana));
        txtSalarioDia.setText("" + df.format(factorSalarioDia));
        txtSalarioHora.setText("" + df.format(factorSalarioHora));
        txtSalarioSemana.setText("" + df.format(factorSalarioSemana));
        txtDia.setText("" + df.format(totalDiaS));
        txtHora.setText("" +df.format( totalHoraS));
        txtSemana.setText("" + df.format(totalSemanaS));

        //Impresion de los txtBox en la tabal CON EFICIENCIA
        txtFactorDiaEficiencia.setText("" +df.format(factorDiaE));
        txtFactorHoraEficiencia.setText("" + df.format(factorHoraE));
        txtFactorSemanaEficiencia.setText("" +df.format( factorSemanaE));
        txtSalarioDiaEficiencia.setText("" + df.format(factorSalarioDiaE));
        txtSalarioHoraEficiencia.setText("" + df.format(factorSalarioHoraE));
        txtSalarioSemanaEficiencia.setText("" + df.format(factorSalarioSemanaE));
        txtDiaEficiencia.setText("" + df.format(totalDiaSE));
        txtHoraEficiencia.setText("" +df.format( totalHoraSE));
        txtSemanaEficiencia.setText("" + df.format(totalSemanaSE));
    }//GEN-LAST:event_btnCalcularManoDeObraActionPerformed
    
    private void txtTotalHoraActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtTotalHoraActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtTotalHoraActionPerformed

    private void txtVacacion2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtVacacion2ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtVacacion2ActionPerformed

    private void btnGenerarBalanzaComprobacionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGenerarBalanzaComprobacionActionPerformed
        cuentasSaldadas = cuentas.stream()
        .filter(cuenta -> cuenta.getSaldo()!= 0)
        .toList();
        ControladorTablaBalanzaComprobacion controladorTablaBalanzaComprobacion = new ControladorTablaBalanzaComprobacion(cuentasSaldadas);
        tablaBalanzaComprobacion.setModel(controladorTablaBalanzaComprobacion);

        var colModel = tablaBalanzaComprobacion.getColumnModel();

        for(int j=0;j<colModel.getColumnCount();j++){
            if(j==0) colModel.getColumn(j).setHeaderValue("Código");
            if(j==1) colModel.getColumn(j).setHeaderValue("Cuenta");
            if(j==2) colModel.getColumn(j).setHeaderValue("Debe");
            if(j==3) colModel.getColumn(j).setHeaderValue("Haber");
        }

        double totalDebeBalanzaComprobacion = cuentasSaldadas.stream()
        .filter(cuenta -> cuenta.getSaldo()> 0)
        .mapToDouble(Cuenta::getSaldo)
        .sum();
        double totalHaberBalanzaComprobacion = cuentasSaldadas.stream()
        .filter(cuenta -> cuenta.getSaldo()< 0)
        .mapToDouble(Cuenta::getSaldo)
        .sum();

        txtDebe.setText(String.valueOf(totalDebeBalanzaComprobacion));
        txtHaber.setText(String.valueOf(Math.abs(totalHaberBalanzaComprobacion)));
        
        btnGenerarEstadoResultado.setEnabled(true);
        lblInfoEstadoResultado.setVisible(false);
    }//GEN-LAST:event_btnGenerarBalanzaComprobacionActionPerformed

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        GeneradorLibroMayor generadorLibroMayor = new GeneradorLibroMayor();
        generadorLibroMayor.execute();
        btnGenerarBalanzaComprobacion.setEnabled(true);
        lblInforBalanzaComprobacion.setVisible(false);
    }//GEN-LAST:event_jButton6ActionPerformed

    private void btnAnadirTransaccionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAnadirTransaccionActionPerformed
        RegistroAsiento ra = new RegistroAsiento(controladorTablaLibroDiario,cuentas);
        

    }//GEN-LAST:event_btnAnadirTransaccionActionPerformed

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
            
            informacionContable = new InformacionContable();
            
            libroMayor = new LibroMayor();
            libroDiario = new LibroDiario();
            
            
            informacionContable.setLibroDiario(libroDiario);
            informacionContable.setLibroMayor(libroMayor);
            
            
            cuentas = libroMayor.getCuentas();
            asientos = libroDiario.getAsientos();
            
            configurarListViewCuentasDisponibles(cuentas);
            configurarTablaLibroDiario(asientos);
            
            contenedorPestañas.setEnabled(true);
            btnAnadirCuenta.setEnabled(true);
        }

    }//GEN-LAST:event_btnCrearNuevoArchivoInfcActionPerformed

    private void btnAbrirArchivoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAbrirArchivoActionPerformed
        JFileChooser seleccionadorArchivo = new JFileChooser();
        seleccionadorArchivo.setFileSelectionMode(JFileChooser.FILES_ONLY);
        seleccionadorArchivo.setFileFilter(new FileNameExtensionFilter("Informacion contable","ser"));
        int opcion = seleccionadorArchivo.showOpenDialog(this);
        LectorArchivos lectorArchivos = new LectorArchivos();

        if(opcion == JFileChooser.APPROVE_OPTION){
            persistenciaDeDatos.configurarArchivo(seleccionadorArchivo.getSelectedFile().getPath());
        
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
            btnAnadirCuenta.setEnabled(true);
        }
    }//GEN-LAST:event_btnAbrirArchivoActionPerformed

    private void btnModificarCuentaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnModificarCuentaActionPerformed
      Cuenta cuentaParaModificar;
      int indiceCuentaParaModificar = lstCuentasDisponibles.getSelectedIndex();
      if(indiceCuentaParaModificar == -1) return;
      
      cuentaParaModificar = cuentas.get(indiceCuentaParaModificar);
      Categoria categoria = Categoria.valueOf(cmbSeleccionarCuenta.getSelectedItem().toString());
      cuentaParaModificar.setCategoria(categoria);
      cuentaParaModificar.setNombre(txtNombreCuenta.getText());
      cuentaParaModificar.setCodCuenta(txtCodigoCuenta.getText().transform(Integer::parseInt));
      
      controladorCuentasDisp.actualizarLista();
      JOptionPane.showMessageDialog(this,"Modificación de cuenta","Se modificó la cuenta de "+cuentaParaModificar.getNombre(),JOptionPane.INFORMATION_MESSAGE);
      btnModificarCuenta.setEnabled(false);
    }//GEN-LAST:event_btnModificarCuentaActionPerformed

    private void btnAnadirCuentaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAnadirCuentaActionPerformed

        boolean cuentaYaExiste = false;
        if(txtNombreCuenta.getText().isBlank() ||
                cmbSeleccionarCuenta.getSelectedIndex() == -1 || 
                txtCodigoCuenta.getText().isBlank())
        {       
            JOptionPane.showMessageDialog(this,"Rellene todos los campos","Campos vacios!",JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        
        int codigoCuenta = Integer.parseInt(txtCodigoCuenta.getText());
        String nombreCuenta = txtNombreCuenta.getText();

        if(!cuentas.isEmpty()){
        cuentaYaExiste= cuentas.stream().filter(cuenta -> cuenta.getCodCuenta() == codigoCuenta).map(cuenta -> true).findAny().orElse(false);
        }
        if(cuentaYaExiste){
            JOptionPane.showMessageDialog(this,"Ya existe una cuenta con el código ingresado.","",JOptionPane.ERROR_MESSAGE);
        }else{
            int seleccion = JOptionPane.showConfirmDialog(this,"¿Desea agregar esta nueva cuenta?","Agregar cuenta nueva",JOptionPane.OK_CANCEL_OPTION,JOptionPane.INFORMATION_MESSAGE);
            if(seleccion == JOptionPane.OK_OPTION)
            {
                //CAMBIAR POR LA CATEGORIA ----------------------------------------------------
                Categoria categoria = Categoria.valueOf(cmbSeleccionarCuenta.getSelectedItem().toString());
                controladorCuentasDisp.añadirNuevaCuenta(new Cuenta(codigoCuenta, nombreCuenta,categoria));
            }

            limpiarTxtPestañaCuentas();
        }
    }//GEN-LAST:event_btnAnadirCuentaActionPerformed

    private void btnGenerarEstadoResultadoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGenerarEstadoResultadoActionPerformed
        Map<Boolean,List<Cuenta>> cuentasParticionadasPorIngresosGastos;
        cuentasParticionadasPorIngresosGastos = cuentasSaldadas
                                                .stream()
                                                .filter( (Cuenta cuenta) -> cuenta.getCategoria() == Categoria.INGRESOS || cuenta.getCategoria() == Categoria.COSTOS_y_GASTOS)
                                                .collect(partitioningBy(cuenta -> cuenta.getCategoria()==Categoria.INGRESOS));
       
         listadoCuentasIngresoGastos = cuentasParticionadasPorIngresosGastos.values().stream()
                                            .flatMap(List::stream)
                                            .toList();
        
       controladorTablaEstadoResultado = new ControladorTablaEstadoResultado(listadoCuentasIngresoGastos);
       tablaEstadoResultado.setModel(controladorTablaEstadoResultado);
       var colModel = tablaEstadoResultado.getColumnModel();
       
       for(int i = 0; i<colModel.getColumnCount();i++){
           if(i == 0 ) colModel.getColumn(i).setHeaderValue("Código");
           if(i == 1) colModel.getColumn(i).setHeaderValue("Cuenta");
           if(i == 2) colModel.getColumn(i).setHeaderValue("Debe");
           if(i == 3) colModel.getColumn(i).setHeaderValue("Haber");
           if(i == 4) colModel.getColumn(i).setHeaderValue("Tipo de saldo");
           
           
       }
       
        btnCalculoUtilidadesPerdidas.setEnabled(true);

    }//GEN-LAST:event_btnGenerarEstadoResultadoActionPerformed

    private void btnOlvidarSeleccionCuentaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnOlvidarSeleccionCuentaActionPerformed
        
        lstCuentasDisponibles.clearSelection();
        limpiarTxtPestañaCuentas();
        btnOlvidarSeleccionCuenta.setEnabled(false);
        btnModificarCuenta.setEnabled(false);
    }//GEN-LAST:event_btnOlvidarSeleccionCuentaActionPerformed

    private void txtTotalEspacioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtTotalEspacioActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtTotalEspacioActionPerformed

    private void txtMantenimientoServicioEspacioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtMantenimientoServicioEspacioActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtMantenimientoServicioEspacioActionPerformed

    private void txtCalidadEspacioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtCalidadEspacioActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtCalidadEspacioActionPerformed

    private void txtMantenimientoSoftEspacioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtMantenimientoSoftEspacioActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtMantenimientoSoftEspacioActionPerformed

    private void txtInsumoEspacioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtInsumoEspacioActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtInsumoEspacioActionPerformed

    private void txtMantenimientoEspacioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtMantenimientoEspacioActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtMantenimientoEspacioActionPerformed

    private void txtPruebaEspacioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtPruebaEspacioActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtPruebaEspacioActionPerformed

    private void txtDesarrolloEspacioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtDesarrolloEspacioActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtDesarrolloEspacioActionPerformed

    private void txtDisenoEspacioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtDisenoEspacioActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtDisenoEspacioActionPerformed

    private void txtAnalisisEspacioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtAnalisisEspacioActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtAnalisisEspacioActionPerformed

    private void btnGuardarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGuardarActionPerformed
        // TODO add your handling code here:
        //Variables a utilizar
        //catalogo
        double salarioSup, salarioOfi, manoObraIndirecta, materialesIndirectos, suministros, herramientas, otrosMateriales, depreciacion, impuestos, serviciosPublicos, serviciosPrivados;
        //Departamento
        double analisisEspacio, analisisConsumo, analisisEmpleado, analisisHora;
        double disenoEspacio, disenoConsumo, disenoEmpleado, disenoHora;
        double desarrolloEspacio, desarrolloConsumo, desarrolloEmpleado, desarrolloHora;
        double pruebaEspacio, pruebaConsumo, pruebaEmpleado, pruebaHora;
        double mantenimientoEspacio, mantenimientoConsumo, mantenimientoEmpleado, mantenimientoHora;
        double calidadEspacio, calidadConsumo, calidadEmpleado, calidadHora;
        double insumoEspacio, insumoConsumo, insumoEmpleado, insumoHora;
        double mantenimientoSoftEspacio, mantenimientoSoftConsumo, mantenimientoSoftEmpleado, mantenimientoSoftHora;
        double mantenimientoDepaEspacio, mantenimientoDepaConsumo, mantenimientoDepaEmpleado, mantenimientoDepaHora;
        //salidas
        double sumaCatalogo, sumaEspacio, sumaConsumo, sumaEmpleado, sumaHora;
        //Recojo los datos de los textBox
        salarioSup = Double.parseDouble(txtSalariosSup2.getText());
        salarioOfi = Double.parseDouble(txtSalariosOfi2.getText());
        manoObraIndirecta = Double.parseDouble(txtManoDeObraIndirecta2.getText());
        materialesIndirectos = Double.parseDouble(txtMaterialesIndirectos2.getText());
        suministros = Double.parseDouble(txtSuministros2.getText());
        herramientas = Double.parseDouble(txtHerramientas2.getText());
        otrosMateriales = Double.parseDouble(txtOtrosMateriales2.getText());
        depreciacion = Double.parseDouble(txtDepreciacion2.getText());
        impuestos = Double.parseDouble(txtImpuestos2.getText());
        serviciosPublicos = Double.parseDouble(txtServiciosPublicos2.getText());
        serviciosPrivados = Double.parseDouble(txtServiciosPrivados2.getText());

        //Recojo los datos de los textbox de Departamentos
        analisisEspacio = Double.parseDouble(txtAnalisisEspacio.getText());
        analisisConsumo = Double.parseDouble(txtAnalisisConsumo.getText());
        analisisEmpleado = Double.parseDouble(txtAnalisisEmpleado.getText());
        analisisHora = Double.parseDouble(txtAnalisisHora.getText());
        disenoEspacio = Double.parseDouble(txtDisenoEspacio.getText());
        disenoConsumo = Double.parseDouble(txtDisenoConsumo.getText());
        disenoEmpleado = Double.parseDouble(txtDisenoEmpleado.getText());
        disenoHora = Double.parseDouble(txtDisenoHora.getText());
        desarrolloEspacio = Double.parseDouble(txtDesarrolloEspacio.getText());
        desarrolloConsumo = Double.parseDouble(txtDesarrolloConsumo.getText());
        desarrolloEmpleado = Double.parseDouble(txtDesarrolloEmpleado.getText());
        desarrolloHora = Double.parseDouble(txtDesarrolloHora.getText());
        pruebaEspacio = Double.parseDouble(txtPruebaEspacio.getText());
        pruebaConsumo = Double.parseDouble(txtPruebaConsumo.getText());
        pruebaEmpleado = Double.parseDouble(txtPruebaEmpleado.getText());
        pruebaHora = Double.parseDouble(txtPruebaHora.getText());
        mantenimientoEspacio = Double.parseDouble(txtMantenimientoEspacio.getText());
        mantenimientoConsumo = Double.parseDouble(txtMantenimientoConsumo.getText());
        mantenimientoEmpleado = Double.parseDouble(txtMantenimientoEmpleado.getText());
        mantenimientoHora = Double.parseDouble(txtMantenimientoHora.getText());
        calidadEspacio = Double.parseDouble(txtCalidadEspacio.getText());
        calidadConsumo = Double.parseDouble(txtCalidadConsumo.getText());
        calidadEmpleado = Double.parseDouble(txtCalidadEmpleado.getText());
        calidadHora = Double.parseDouble(txtCalidadHora.getText());
        insumoEspacio = Double.parseDouble(txtInsumoEspacio.getText());
        insumoConsumo = Double.parseDouble(txtInsumoConsumo.getText());
        insumoEmpleado = Double.parseDouble(txtInsumoEmpleado.getText());
        insumoHora = Double.parseDouble(txtInsumoHora.getText());
        mantenimientoSoftEspacio = Double.parseDouble(txtMantenimientoSoftEspacio.getText());
        mantenimientoSoftConsumo = Double.parseDouble(txtMantenimientoSoftConsumo.getText());
        mantenimientoSoftEmpleado = Double.parseDouble(txtMantenimientoSoftEmpleado.getText());
        mantenimientoSoftHora = Double.parseDouble(txtMantenimientoSoftHora.getText());
        mantenimientoDepaEspacio = Double.parseDouble(txtMantenimientoServicioEspacio.getText());
        mantenimientoDepaConsumo = Double.parseDouble(txtMantenimientoServicioConsumo.getText());
        mantenimientoDepaEmpleado = Double.parseDouble(txtMantenimientoServicioEmpleado.getText());
        mantenimientoDepaHora = Double.parseDouble(txtMantenimientoServicioHora.getText());
        //SUMA RESPECTIVAS
        sumaCatalogo = salarioSup + salarioOfi + manoObraIndirecta + materialesIndirectos + suministros + herramientas + otrosMateriales + depreciacion + impuestos + serviciosPrivados + serviciosPublicos;
        sumaConsumo =  analisisConsumo  + disenoConsumo + desarrolloConsumo + pruebaConsumo + mantenimientoConsumo + calidadConsumo + insumoConsumo + mantenimientoSoftConsumo + mantenimientoDepaConsumo;
        sumaEspacio = analisisEspacio + disenoEspacio + desarrolloEspacio + pruebaEspacio + mantenimientoEspacio + calidadEspacio + insumoEspacio + mantenimientoSoftEspacio + mantenimientoDepaEspacio;
        sumaEmpleado = analisisEmpleado + disenoEmpleado + desarrolloEmpleado + pruebaEmpleado + mantenimientoEmpleado + calidadEmpleado + insumoEmpleado + mantenimientoSoftEmpleado + mantenimientoDepaEmpleado;
        sumaHora = analisisHora + disenoHora + desarrolloHora + pruebaHora + mantenimientoHora + calidadHora + insumoHora + mantenimientoDepaHora + mantenimientoSoftHora;
        //Impresion de los totales en los textbox
        txtTotales2.setText("" + sumaCatalogo);
        txtTotalEspacio.setText("" + sumaEspacio);
        txtTotalConsumo.setText("" + sumaConsumo);
        txtTotalEmpleado.setText("" + sumaEmpleado);
        txtTotalHoraHombre.setText("" + sumaHora);

        //Procesos para el llenado de la tabla grande
        //Varibales de proceso a utilizar
        //Primera parte de la tabla
        double salarioSup_Analisis, salarioSup_Diseno, salarioSup_Desarrollo, salarioSup_Prueba, salarioSup_Mantenimiento, salarioSup_Calidad, salarioSup_Insumo, salarioSup_MantenimientoSoft, salarioSup_Infraestructura;
        double salarioOfi_Calidad, salarioOfi_Insumo, salarioOfi_MantenimientoSoft, salarioOfi_Infraestructura;
        double mano_Analisis, mano_Diseno, mano_Desarrollo, mano_Prueba, mano_Mantenimiento;
        double materiales_Analisis, materiales_Diseno, materiales_Desarrollo, materiales_Prueba, materiales_Mantenimiento;
        double suministros_Analisis, suministros_Diseno, suministros_Desarrollo, suministros_Prueba, suministros_Mantenimiento;
        double herramientas_Analisis, herramientas_Diseno, herramientas_Desarrollo, herramientas_Prueba, herramientas_Mantenimiento, herramientas_Calidad, herramientas_Insumo, herramientas_MantenimientoSoft, herramientas_Infraestructura;
        double otros_Analisis, otros_Diseno, otros_Desarrollo, otros_Prueba, otros_Mantenimiento, otros_Calidad, otros_Insumo, otros_MantenimientoSoft, otros_Infraestructura;
        double depreciacion_Analisis, depreciacion_Diseno, depreciacion_Desarrollo, depreciacion_Prueba, depreciacion_Mantenimiento, depreciacion_Calidad, depreciacion_Insumo, depreciacion_MantenimientoSoft, depreciacion_Infraestructura;
        double impuestos_Analisis, impuestos_Diseno, impuestos_Desarrollo, impuestos_Prueba, impuestos_Mantenimiento, impuestos_Calidad, impuestos_Insumo, impuestos_MantenimientoSoft, impuestos_Infraestructura;
        double servicioPublico_Analisis, servicioPublico_Diseno, servicioPublico_Desarrollo, servicioPublico_Prueba, servicioPublico_Mantenimiento, servicioPublico_Calidad, servicioPublico_Insumo, servicioPublico_MantenimientoSoft, servicioPublico_Infraestructura;
        double servicioPrivado_Analisis, servicioPrivado_Diseno, servicioPrivado_Desarrollo, servicioPrivado_Prueba, servicioPrivado_Mantenimiento, servicioPrivado_Calidad, servicioPrivado_Insumo, servicioPrivado_MantenimientoSoft, servicioPrivado_Infraestructura;
        double mantenimiento_Analisis, mantenimiento_Diseno, mantenimiento_Desarrollo, mantenimiento_Prueba, mantenimiento_Mantenimiento, mantenimiento_Calidad, mantenimiento_Insumo, mantenimiento_MantenimientoSoft, mantenimiento_Infraestructura;
        //Segunda parte
        double calidad_Analisis, calidad_Diseno, calidad_Desarrollo, calidad_Prueba, calidad_Mantenimiento;
        double insumos_Analisis, insumos_Diseno, insumos_Desarrollo, insumos_Prueba, insumos_Mantenimiento;
        double mantenimientoSoft_Analisis, mantenimientoSoft_Diseno, mantenimientoSoft_Desarrollo, mantenimientoSoft_Prueba, mantenimientoSoft_Mantenimiento;
        double infraestructura_Analisis, infraestructura_Diseno, infraestructura_Desarrollo, infraestructura_Prueba, infraestructura_Mantenimiento;
        //Suma de las columnas
        double sumaAnalisis, sumaDiseno, sumaDesarrollo, sumaPrueba, sumaMantenimiento, sumaCalidad, sumaInsumo, sumaMantenimientoSoft, sumaInfraestructura;
        //Suma de las filas

        //Procesos y calculos respectivos
        //Fila de salario supervision
        salarioSup_Analisis = (analisisEmpleado/sumaEmpleado)*salarioSup;
        salarioSup_Diseno = (disenoEmpleado/sumaEmpleado)*salarioSup;
        salarioSup_Desarrollo = (desarrolloEmpleado/sumaEmpleado)*salarioSup;
        salarioSup_Prueba = (pruebaEmpleado/sumaEmpleado)*salarioSup;
        salarioSup_Mantenimiento = (mantenimientoEmpleado/sumaEmpleado)*salarioSup;
        salarioSup_Calidad = (calidadEmpleado/sumaEmpleado)*salarioSup;
        salarioSup_Insumo = (insumoEmpleado/sumaEmpleado)*salarioSup;
        salarioSup_MantenimientoSoft = (mantenimientoSoftEmpleado/sumaEmpleado)*salarioSup;
        salarioSup_Infraestructura = (mantenimientoDepaEmpleado/sumaEmpleado)*salarioSup;
        //Fila de salario oficina
        salarioOfi_Calidad = (calidadEmpleado/(calidadEmpleado + insumoEmpleado + mantenimientoSoftEmpleado + mantenimientoDepaEmpleado))*salarioOfi;
        salarioOfi_Insumo = (insumoEmpleado/(calidadEmpleado + insumoEmpleado + mantenimientoSoftEmpleado + mantenimientoDepaEmpleado))*salarioOfi;
        salarioOfi_MantenimientoSoft = (mantenimientoSoftEmpleado/(calidadEmpleado + insumoEmpleado + mantenimientoSoftEmpleado + mantenimientoDepaEmpleado))*salarioOfi;
        salarioOfi_Infraestructura = (mantenimientoDepaEmpleado/(calidadEmpleado + insumoEmpleado + mantenimientoSoftEmpleado + mantenimientoDepaEmpleado))*salarioOfi;
        //Fila Mano de obra indirecta
        mano_Analisis = (analisisEspacio/(analisisEspacio + disenoEspacio + desarrolloEspacio + pruebaEspacio + mantenimientoEspacio))*manoObraIndirecta;
        mano_Diseno = (disenoEspacio/(analisisEspacio + disenoEspacio + desarrolloEspacio + pruebaEspacio + mantenimientoEspacio))*manoObraIndirecta;
        mano_Desarrollo = (desarrolloEspacio/(analisisEspacio + disenoEspacio + desarrolloEspacio + pruebaEspacio + mantenimientoEspacio))*manoObraIndirecta;
        mano_Prueba =  (pruebaEspacio/(analisisEspacio + disenoEspacio + desarrolloEspacio + pruebaEspacio + mantenimientoEspacio))*manoObraIndirecta;
        mano_Mantenimiento = (mantenimientoEspacio/(analisisEspacio + disenoEspacio + desarrolloEspacio + pruebaEspacio + mantenimientoEspacio))*manoObraIndirecta;
        //Fila Materiales Indirectos
        materiales_Analisis =  (analisisEspacio/(analisisEspacio + disenoEspacio + desarrolloEspacio + pruebaEspacio + mantenimientoEspacio))*materialesIndirectos;
        materiales_Diseno =  (disenoEspacio/(analisisEspacio + disenoEspacio + desarrolloEspacio + pruebaEspacio + mantenimientoEspacio))*materialesIndirectos;
        materiales_Desarrollo =  (desarrolloEspacio/(analisisEspacio + disenoEspacio + desarrolloEspacio + pruebaEspacio + mantenimientoEspacio))*materialesIndirectos;
        materiales_Prueba =  (pruebaEspacio/(analisisEspacio + disenoEspacio + desarrolloEspacio + pruebaEspacio + mantenimientoEspacio))*materialesIndirectos;
        materiales_Mantenimiento =  (mantenimientoEspacio/(analisisEspacio + disenoEspacio + desarrolloEspacio + pruebaEspacio + mantenimientoEspacio))*materialesIndirectos;
        //Fila suministros
        suministros_Analisis =  (analisisEspacio/(analisisEspacio + disenoEspacio + desarrolloEspacio + pruebaEspacio + mantenimientoEspacio))*suministros;
        suministros_Diseno =  (disenoEspacio/(analisisEspacio + disenoEspacio + desarrolloEspacio + pruebaEspacio + mantenimientoEspacio))*suministros;
        suministros_Desarrollo =  (desarrolloEspacio/(analisisEspacio + disenoEspacio + desarrolloEspacio + pruebaEspacio + mantenimientoEspacio))*suministros;
        suministros_Prueba =  (pruebaEspacio/(analisisEspacio + disenoEspacio + desarrolloEspacio + pruebaEspacio + mantenimientoEspacio))*suministros;
        suministros_Mantenimiento =  (mantenimientoEspacio/(analisisEspacio + disenoEspacio + desarrolloEspacio + pruebaEspacio + mantenimientoEspacio))*suministros;
        //Fila HERRAMIENTAS
        herramientas_Analisis = (analisisConsumo/sumaConsumo)*herramientas;
        herramientas_Diseno = (disenoConsumo/sumaConsumo)*herramientas;
        herramientas_Desarrollo = (desarrolloConsumo/sumaConsumo)*herramientas;
        herramientas_Prueba = (pruebaConsumo/sumaConsumo)*herramientas;
        herramientas_Mantenimiento = (mantenimientoConsumo/sumaConsumo)*herramientas;
        herramientas_Calidad = (calidadConsumo/sumaConsumo)*herramientas;
        herramientas_Insumo = (insumoConsumo/sumaConsumo)*herramientas;
        herramientas_MantenimientoSoft = (mantenimientoSoftConsumo/sumaConsumo)*herramientas;
        herramientas_Infraestructura = (mantenimientoDepaConsumo/sumaConsumo)*herramientas;
        //Fila Otros materiales
        otros_Analisis = (analisisConsumo/sumaConsumo)*otrosMateriales;
        otros_Diseno = (disenoConsumo/sumaConsumo)*otrosMateriales;
        otros_Desarrollo = (desarrolloConsumo/sumaConsumo)*otrosMateriales;
        otros_Prueba = (pruebaConsumo/sumaConsumo)*otrosMateriales;
        otros_Mantenimiento = (mantenimientoConsumo/sumaConsumo)*otrosMateriales;
        otros_Calidad = (calidadConsumo/sumaConsumo)*otrosMateriales;
        otros_Insumo = (insumoConsumo/sumaConsumo)*otrosMateriales;
        otros_MantenimientoSoft = (mantenimientoSoftConsumo/sumaConsumo)*otrosMateriales;
        otros_Infraestructura = (mantenimientoDepaConsumo/sumaConsumo)*otrosMateriales;
        //Fila depreciacion
        depreciacion_Analisis = (analisisEspacio/sumaEspacio)*depreciacion;
        depreciacion_Diseno = (disenoEspacio/sumaEspacio)*depreciacion;
        depreciacion_Desarrollo = (desarrolloEspacio/sumaEspacio)*depreciacion;
        depreciacion_Prueba = (pruebaEspacio/sumaEspacio)*depreciacion;
        depreciacion_Mantenimiento = (mantenimientoEspacio/sumaEspacio)*depreciacion;
        depreciacion_Calidad = (calidadEspacio/sumaEspacio)*depreciacion;
        depreciacion_Insumo = (insumoEspacio/sumaEspacio)*depreciacion;
        depreciacion_MantenimientoSoft = (mantenimientoSoftEspacio/sumaEspacio)*depreciacion;
        depreciacion_Infraestructura = (mantenimientoDepaEspacio/sumaEspacio)*depreciacion;
        //Fila impuestos
        impuestos_Analisis = (analisisEspacio/sumaEspacio)*impuestos;
        impuestos_Diseno = (disenoEspacio/sumaEspacio)*impuestos;
        impuestos_Desarrollo = (desarrolloEspacio/sumaEspacio)*impuestos;
        impuestos_Prueba = (pruebaEspacio/sumaEspacio)*impuestos;
        impuestos_Mantenimiento = (mantenimientoEspacio/sumaEspacio)*impuestos;
        impuestos_Calidad = (calidadEspacio/sumaEspacio)*impuestos;
        impuestos_Insumo = (insumoEspacio/sumaEspacio)*impuestos;
        impuestos_MantenimientoSoft = (mantenimientoSoftEspacio/sumaEspacio)*impuestos;
        impuestos_Infraestructura = (mantenimientoDepaEspacio/sumaEspacio)*impuestos;
        //Fila servicios publivos
        servicioPublico_Analisis = (analisisConsumo/sumaConsumo)*serviciosPublicos;
        servicioPublico_Diseno = (disenoConsumo/sumaConsumo)*serviciosPublicos;
        servicioPublico_Desarrollo = (desarrolloConsumo/sumaConsumo)*serviciosPublicos;
        servicioPublico_Prueba = (pruebaConsumo/sumaConsumo)*serviciosPublicos;
        servicioPublico_Mantenimiento = (mantenimientoConsumo/sumaConsumo)*serviciosPublicos;
        servicioPublico_Calidad = (calidadConsumo/sumaConsumo)*serviciosPublicos;
        servicioPublico_Insumo = (insumoConsumo/sumaConsumo)*serviciosPublicos;
        servicioPublico_MantenimientoSoft = (mantenimientoSoftConsumo/sumaConsumo)*serviciosPublicos;
        servicioPublico_Infraestructura = (mantenimientoDepaConsumo/sumaConsumo)*serviciosPublicos;
        //Fila servicio privados
        servicioPrivado_Analisis = (analisisConsumo/sumaConsumo)*serviciosPrivados;
        servicioPrivado_Diseno = (disenoConsumo/sumaConsumo)*serviciosPrivados;
        servicioPrivado_Desarrollo = (desarrolloConsumo/sumaConsumo)*serviciosPrivados;
        servicioPrivado_Prueba = (pruebaConsumo/sumaConsumo)*serviciosPrivados;
        servicioPrivado_Mantenimiento = (mantenimientoConsumo/sumaConsumo)*serviciosPrivados;
        servicioPrivado_Calidad = (calidadConsumo/sumaConsumo)*serviciosPrivados;
        servicioPrivado_Insumo = (insumoConsumo/sumaConsumo)*serviciosPrivados;
        servicioPrivado_MantenimientoSoft = (mantenimientoSoftConsumo/sumaConsumo)*serviciosPrivados;
        servicioPrivado_Infraestructura = (mantenimientoDepaConsumo/sumaConsumo)*serviciosPrivados;
        //Fila Mantenimiento y reparaciones (NO LLEVA PORQUE SE OMITE EN LA TABLA 01 DEL CIIF)
        //TOTALES DE LAS COLUMNAS DE CALIDAD, INSUMOS, MANTENIMIENTOSOFT Y INFRAESTRUCTURA PARA USAR EN LAS FILAS DE ABAJO
        sumaInfraestructura = servicioPrivado_Infraestructura  + servicioPublico_Infraestructura + impuestos_Infraestructura + depreciacion_Infraestructura + otros_Infraestructura +
        herramientas_Infraestructura + salarioOfi_Infraestructura + salarioSup_Infraestructura;
        sumaMantenimientoSoft = servicioPrivado_MantenimientoSoft  + servicioPublico_MantenimientoSoft + impuestos_MantenimientoSoft + depreciacion_MantenimientoSoft + otros_MantenimientoSoft +
        herramientas_MantenimientoSoft + salarioOfi_MantenimientoSoft + salarioSup_MantenimientoSoft;
        sumaInsumo = servicioPrivado_Insumo  + servicioPublico_Insumo + impuestos_Insumo + depreciacion_Insumo + otros_Insumo +
        herramientas_Insumo + salarioOfi_Insumo + salarioSup_Insumo;
        sumaCalidad = servicioPrivado_Calidad  + servicioPublico_Calidad + impuestos_Calidad + depreciacion_Calidad + otros_Calidad +
        herramientas_Calidad+ salarioOfi_Calidad + salarioSup_Calidad;

        //Fila de calidad
        calidad_Analisis =  (analisisConsumo/(analisisConsumo + disenoConsumo + desarrolloConsumo + pruebaConsumo + mantenimientoConsumo))*sumaInfraestructura;
        calidad_Diseno =  (disenoConsumo/(analisisConsumo + disenoConsumo + desarrolloConsumo + pruebaConsumo + mantenimientoConsumo))*sumaInfraestructura;
        calidad_Desarrollo =  (desarrolloConsumo/(analisisConsumo + disenoConsumo + desarrolloConsumo + pruebaConsumo + mantenimientoConsumo))*sumaInfraestructura;
        calidad_Prueba =  (pruebaConsumo/(analisisConsumo + disenoConsumo + desarrolloConsumo + pruebaConsumo + mantenimientoConsumo))*sumaInfraestructura;
        calidad_Mantenimiento =  (mantenimientoConsumo/(analisisConsumo + disenoConsumo + desarrolloConsumo + pruebaConsumo + mantenimientoConsumo))*sumaInfraestructura;
        //Fila de Insumos
        insumos_Analisis =  (analisisConsumo/(analisisConsumo + disenoConsumo + desarrolloConsumo + pruebaConsumo + mantenimientoConsumo))*sumaMantenimientoSoft;
        insumos_Diseno =  (disenoConsumo/(analisisConsumo + disenoConsumo + desarrolloConsumo + pruebaConsumo + mantenimientoConsumo))*sumaMantenimientoSoft;
        insumos_Desarrollo =  (desarrolloConsumo/(analisisConsumo + disenoConsumo + desarrolloConsumo + pruebaConsumo + mantenimientoConsumo))*sumaMantenimientoSoft;
        insumos_Prueba =  (pruebaConsumo/(analisisConsumo + disenoConsumo + desarrolloConsumo + pruebaConsumo + mantenimientoConsumo))*sumaMantenimientoSoft;
        insumos_Mantenimiento =  (mantenimientoConsumo/(analisisConsumo + disenoConsumo + desarrolloConsumo + pruebaConsumo + mantenimientoConsumo))*sumaMantenimientoSoft;
        //Fila de MantenimientoSoft
        mantenimientoSoft_Analisis =  (analisisConsumo/(analisisConsumo + disenoConsumo + desarrolloConsumo + pruebaConsumo + mantenimientoConsumo))*sumaInsumo;
        mantenimientoSoft_Diseno =  (disenoConsumo/(analisisConsumo + disenoConsumo + desarrolloConsumo + pruebaConsumo + mantenimientoConsumo))*sumaInsumo;
        mantenimientoSoft_Desarrollo =  (desarrolloConsumo/(analisisConsumo + disenoConsumo + desarrolloConsumo + pruebaConsumo + mantenimientoConsumo))*sumaInsumo;
        mantenimientoSoft_Prueba =  (pruebaConsumo/(analisisConsumo + disenoConsumo + desarrolloConsumo + pruebaConsumo + mantenimientoConsumo))*sumaInsumo;
        mantenimientoSoft_Mantenimiento =  (mantenimientoConsumo/(analisisConsumo + disenoConsumo + desarrolloConsumo + pruebaConsumo + mantenimientoConsumo))*sumaInsumo;
        //Fila de Infraestructura (MANTENIMIENTODEPA)
        infraestructura_Analisis =  (analisisEspacio/(analisisEspacio + disenoEspacio + desarrolloEspacio + pruebaEspacio + mantenimientoEspacio))*sumaCalidad;
        infraestructura_Diseno =  (disenoEspacio/(analisisEspacio + disenoEspacio + desarrolloEspacio + pruebaEspacio + mantenimientoEspacio))*sumaCalidad;
        infraestructura_Desarrollo =  (desarrolloEspacio/(analisisEspacio + disenoEspacio + desarrolloEspacio + pruebaEspacio + mantenimientoEspacio))*sumaCalidad;
        infraestructura_Prueba =  (pruebaEspacio/(analisisEspacio + disenoEspacio + desarrolloEspacio + pruebaEspacio + mantenimientoEspacio))*sumaCalidad;
        infraestructura_Mantenimiento =  (mantenimientoEspacio/(analisisEspacio + disenoEspacio + desarrolloEspacio + pruebaEspacio + mantenimientoEspacio))*sumaCalidad;

        //TOTALES DE LAS COLUMNAS DE PRODUCTIVO
        sumaMantenimiento = salarioSup_Mantenimiento + mano_Mantenimiento + materiales_Mantenimiento + suministros_Mantenimiento + herramientas_Mantenimiento + otros_Mantenimiento +
        depreciacion_Mantenimiento + impuestos_Mantenimiento + servicioPublico_Mantenimiento + servicioPrivado_Mantenimiento +
        calidad_Mantenimiento + insumos_Mantenimiento + mantenimientoSoft_Mantenimiento + infraestructura_Mantenimiento;
        sumaPrueba = salarioSup_Prueba + mano_Prueba + materiales_Prueba + suministros_Prueba + herramientas_Prueba + otros_Prueba +
        depreciacion_Prueba + impuestos_Prueba + servicioPublico_Prueba + servicioPrivado_Prueba +
        calidad_Prueba + insumos_Prueba + mantenimientoSoft_Prueba + infraestructura_Prueba;
        sumaDesarrollo = salarioSup_Desarrollo + mano_Desarrollo + materiales_Desarrollo + suministros_Desarrollo + herramientas_Desarrollo + otros_Desarrollo +
        depreciacion_Desarrollo + impuestos_Desarrollo + servicioPublico_Desarrollo + servicioPrivado_Desarrollo +
        calidad_Desarrollo + insumos_Desarrollo + mantenimientoSoft_Desarrollo + infraestructura_Desarrollo;
        sumaDiseno = salarioSup_Diseno + mano_Diseno + materiales_Diseno + suministros_Diseno + herramientas_Diseno + otros_Diseno +
        depreciacion_Diseno + impuestos_Diseno + servicioPublico_Diseno + servicioPrivado_Diseno +
        calidad_Diseno + insumos_Diseno + mantenimientoSoft_Diseno + infraestructura_Diseno;
        sumaAnalisis = salarioSup_Analisis + mano_Analisis + materiales_Analisis + suministros_Analisis + herramientas_Analisis + otros_Analisis +
        depreciacion_Analisis + impuestos_Analisis + servicioPublico_Analisis + servicioPrivado_Analisis +
        calidad_Analisis + insumos_Analisis + mantenimientoSoft_Analisis + infraestructura_Analisis;

        //FORMATO DECIMAL A LA SALIDA
        DecimalFormat df = new DecimalFormat("#.00");
        //Colocando todos los valores calculados en los textbox
        txtSalarioSupAnalisisProductivo.setText(""+ df.format(salarioSup_Analisis));
        txtSalarioSupDisenoProductivo.setText(""+ df.format(salarioSup_Diseno));
        txtSalarioSupDesarrolloProductivo.setText(""+ df.format(salarioSup_Desarrollo));
        txtSalarioSupPruebaProductivo.setText(""+ df.format(salarioSup_Prueba));
        txtSalarioSupMantenimientoProductivo.setText(""+ df.format(salarioSup_Mantenimiento));
        txtSalarioSupCalidadServicio.setText(""+ df.format(salarioSup_Calidad));
        txtSalarioSupInsumoServicio.setText(""+ df.format(salarioSup_Insumo));
        txtSalarioSupMantenimientoSoftServicio.setText(""+ df.format(salarioSup_MantenimientoSoft));
        txtSalarioSupInfraestructuraServicio.setText(""+ df.format(salarioSup_Infraestructura));
        //
        txtSalarioOfiCalidadServicio.setText(""+ df.format(salarioOfi_Calidad));
        txtSalarioOfiInsumosServicio.setText(""+ df.format(salarioOfi_Insumo));
        txtSalarioOfiMantenimientoSoftServicio.setText(""+ df.format(salarioOfi_MantenimientoSoft));
        txtSalarioOfiInfraestructuraServicio.setText(""+ df.format(salarioOfi_Infraestructura));
        //
        txtManoAnalisisProductivo.setText(""+ df.format(mano_Analisis));
        txtManoDisenoProductivo.setText(""+ df.format(mano_Diseno));
        txtManoDesarrolloProductivo.setText(""+ df.format(mano_Desarrollo));
        txtManoPruebaProductivo.setText(""+ df.format(mano_Prueba));
        txtManoMantenimientoProductivo.setText(""+ df.format(mano_Mantenimiento));
        //
        txtMaterialAnalisisProductivo.setText(""+ df.format(materiales_Analisis));
        txtMaterialDisenoProductivo.setText(""+ df.format(materiales_Diseno));
        txtMaterialDesarrolloProductivo.setText(""+ df.format(materiales_Desarrollo));
        txtMaterialPruebaProductivo.setText(""+ df.format(materiales_Prueba));
        txtMaterialMantenimientoProductivo.setText(""+ df.format(materiales_Mantenimiento));
        //
        txtSumiAnalisisProductivo.setText(""+ df.format(suministros_Analisis));
        txtSumiDisenoProductivo.setText(""+ df.format(suministros_Diseno));
        txtSumiDesarrolloProductivo.setText(""+ df.format(suministros_Desarrollo));
        txtSumiPruebaProductivo.setText(""+ df.format(suministros_Prueba));
        txtSumiMantenimientoProductivo.setText(""+ df.format(suministros_Mantenimiento));
        //
        txtHerraAnalisisProductivo.setText(""+ df.format(herramientas_Analisis));
        txtHerraDisenoProductivo.setText(""+ df.format(herramientas_Diseno));
        txtHerraDesarrolloProductivo.setText(""+ df.format(herramientas_Desarrollo));
        txtHerraPruebaProductivo.setText(""+ df.format(herramientas_Prueba));
        txtHerraMantenimientoProductivo.setText(""+ df.format(herramientas_Mantenimiento));
        txtHerraCalidadServicio.setText(""+ df.format(herramientas_Calidad));
        txtHerraInsumoServicio.setText(""+ df.format(herramientas_Insumo));
        txtHerraMantenimientoSoftServicio.setText(""+ df.format(herramientas_MantenimientoSoft));
        txtHerraInfraestructuraServicio.setText(""+ df.format(herramientas_Infraestructura));
        //
        txtOtrosAnalisisProductivo.setText(""+ df.format(otros_Analisis));
        txtOtrosDisenoProductivo.setText(""+ df.format(otros_Diseno));
        txtOtrosDesarrolloProductivo.setText(""+ df.format(otros_Desarrollo));
        txtOtrosPruebaProductivo.setText(""+ df.format(otros_Prueba));
        txtOtrosMantenimientoProductivo.setText(""+ df.format(otros_Mantenimiento));
        txtOtrosCalidadServicio.setText(""+ df.format(otros_Calidad));
        txtOtrosInsumoServicio.setText(""+ df.format(otros_Insumo));
        txtOtrosMantenimientoSoftServicio.setText(""+ df.format(otros_MantenimientoSoft));
        txtOtrosInfraestructuraServicio.setText(""+ df.format(otros_Infraestructura));
        //
        txtDepreciacionAnalisisProductivo.setText(""+ df.format(depreciacion_Analisis));
        txtDepreciacionDisenoProductivo.setText(""+ df.format(depreciacion_Diseno));
        txtDepreciacionDesarrolloProductivo.setText(""+ df.format(depreciacion_Desarrollo));
        txtDepreciacionPruebaProductivo.setText(""+ df.format(depreciacion_Prueba));
        txtDepreciacionMantenimientoProductivo.setText(""+ df.format(depreciacion_Mantenimiento));
        txtDepreciacionCalidadServicio.setText(""+ df.format(depreciacion_Calidad));
        txtDepreciacionInsumoServicio.setText(""+ df.format(depreciacion_Insumo));
        txtDepreciacionMantenimientoSoftServicio.setText(""+ df.format(depreciacion_MantenimientoSoft));
        txtDepreciacionInfraestructuraServicio.setText(""+ df.format(depreciacion_Infraestructura));
        //
        txtImpuestosAnalisisProductivo.setText(""+ df.format(impuestos_Analisis));
        txtImpuestosDisenoProductivo.setText(""+ df.format(impuestos_Diseno));
        txtImpuestosDesarrolloProductivo.setText(""+ df.format(impuestos_Desarrollo));
        txtImpuestosPruebaProductivo.setText(""+ df.format(impuestos_Prueba));
        txtImpuestosMantenimientoProductivo.setText(""+ df.format(impuestos_Mantenimiento));
        txtImpuestosCalidadServicio.setText(""+ df.format(impuestos_Calidad));
        txtImpuestosInsumoServicio.setText(""+ df.format(impuestos_Insumo));
        txtImpuestosMantenimientoSoftServicio.setText(""+ df.format(impuestos_MantenimientoSoft));
        txtImpuestosInfraestructuraServicio.setText(""+ df.format(impuestos_Infraestructura));
        //
        txtServicioPublicoAnalisisProductivo.setText(""+ df.format(servicioPublico_Analisis));
        txtServicioPublicoDisenoProductivo.setText(""+ df.format(servicioPublico_Diseno));
        txtServicioPublicoDesarrolloProductivo.setText(""+ df.format(servicioPublico_Desarrollo));
        txtServicioPublicoPruebaProductivo.setText(""+ df.format(servicioPublico_Prueba));
        txtServicioPublicoMantenimientoProductivo.setText(""+ df.format(servicioPublico_Mantenimiento));
        txtServicioPublicoCalidadServicio.setText(""+ df.format(servicioPublico_Calidad));
        txtServicioPublicoInsumoServicio.setText(""+ df.format(servicioPublico_Insumo));
        txtServicioPublicoMantenimientoSoftServicio.setText(""+ df.format(servicioPublico_MantenimientoSoft));
        txtServicioPublicoInfraestructuraServicio.setText(""+ df.format(servicioPublico_Infraestructura));
        //
        txtServicioPrivadoAnalisisProductivo.setText(""+ df.format(servicioPrivado_Analisis));
        txtServicioPrivadoDisenoProductivo.setText(""+ df.format(servicioPrivado_Diseno));
        txtServicioPrivadoDesarrolloProductivo.setText(""+ df.format(servicioPrivado_Desarrollo));
        txtServicioPrivadoPruebaProductivo.setText(""+ df.format(servicioPrivado_Prueba));
        txtServicioPrivadoMantenimientoProductivo.setText(""+ df.format(servicioPrivado_Mantenimiento));
        txtServicioPrivadoCalidadServicio.setText(""+ df.format(servicioPrivado_Calidad));
        txtServicioPrivadoInsumoServicio.setText(""+ df.format(servicioPrivado_Insumo));
        txtServicioPrivadoMantenimientoSoftServicio.setText(""+ df.format(servicioPrivado_MantenimientoSoft));
        txtServicioPrivadoInfraestructuraServicio.setText(""+ df.format(servicioPrivado_Infraestructura));
        //
        txtCalidadAnalisisProductivo.setText(""+ df.format(calidad_Analisis));
        txtCalidadDisenoProductivo.setText(""+ df.format(calidad_Diseno));
        txtCalidadDesarrolloProductivo.setText(""+ df.format(calidad_Desarrollo));
        txtCalidadPruebaProductivo.setText(""+ df.format(calidad_Prueba));
        txtCalidadMantenimientoProductivo.setText(""+ df.format(calidad_Mantenimiento));
        //
        txtInsumoAnalisisProductivo.setText(""+ df.format(insumos_Analisis));
        txtInsumoDisenoProductivo.setText(""+ df.format(insumos_Diseno));
        txtInsumoDesarrolloProductivo.setText(""+ df.format(insumos_Desarrollo));
        txtInsumoPruebaProductivo.setText(""+ df.format(insumos_Prueba));
        txtInsumoMantenimientoProductivo.setText(""+ df.format(insumos_Mantenimiento));
        //
        txtMantenimientoSoftAnalisisProductivo.setText(""+ df.format(mantenimientoSoft_Analisis));
        txtMantenimientoSoftDisenoProductivo.setText(""+ df.format(mantenimientoSoft_Diseno));
        txtMantenimientoSoftDesarrolloProductivo.setText(""+ df.format(mantenimientoSoft_Desarrollo));
        txtMantenimientoSoftPruebaProductivo.setText(""+ df.format(mantenimientoSoft_Prueba));
        txtMantenimientoSoftMantenimientoProductivo.setText(""+ df.format(mantenimientoSoft_Mantenimiento));
        //
        txtInfraestructuraAnalisisProductivo.setText(""+ df.format(infraestructura_Analisis));
        txtInfraestructuraDisenoProductivo.setText(""+ df.format(infraestructura_Diseno));
        txtInfraestructuraDesarrolloProductivo.setText(""+ df.format(infraestructura_Desarrollo));
        txtInfraestructuraPruebaProductivo.setText(""+ df.format(infraestructura_Prueba));
        txtInfraestructuraMantenimientoProductivo.setText(""+ df.format(infraestructura_Mantenimiento));
        //SETEANDO LOS VALORES DE LOS TOTALES EN LOS TXT FINALES
        txtTotalAnalisis.setText("" + df.format(sumaAnalisis));
        txtTotalDesarrollo.setText("" + df.format(sumaDesarrollo));
        txtTotalDiseno.setText("" + df.format(sumaDiseno));
        txtTotalPrueba.setText("" + df.format(sumaPrueba));
        txtTotalMantenimiento.setText("" + df.format(sumaMantenimiento));
        txtTotalCalidad.setText("" + df.format(sumaCalidad));
        txtTotalInsumo.setText("" + df.format(sumaInsumo));
        txtTotalMantenimientoSoft.setText("" + df.format(sumaMantenimientoSoft));
        txtTotalInfraestructura.setText("" + df.format(sumaInfraestructura));
        //totales segun cada cuenta, parte derecha
        txtTotalSalarioSup.setText("" + df.format(salarioSup));
        txtTotalSalarioOfi.setText("" + df.format(salarioOfi));
        txtTotalMano.setText("" + df.format(manoObraIndirecta));
        txtTotalMaterial.setText("" + df.format(materialesIndirectos));
        txtTotalOtros.setText("" + df.format(otrosMateriales));
        txtTotalDepreciacion.setText("" + df.format(depreciacion));
        txtTotalImpuesto.setText("" + df.format(impuestos));
        txtTotalServicioPublico.setText("" + df.format(serviciosPublicos));
        txtTotalServicioPrivado.setText("" + df.format(serviciosPrivados));
        txtTotalSumi.setText("" + df.format(suministros));
        txtTotalHerra.setText("" + df.format(herramientas));
        txtTotalPresupuesto.setText("" + df.format(sumaCatalogo));
        //
        double sumaTotalTotal;
        sumaTotalTotal = sumaAnalisis + sumaDiseno + sumaDesarrollo + sumaPrueba + sumaMantenimiento + sumaCalidad + sumaInsumo + sumaMantenimientoSoft + sumaInfraestructura;
        totalCif = sumaTotalTotal;
        txtTotalTotal.setText( df.format(sumaTotalTotal));
        txtTotalVCalidad.setText("" + df.format(sumaCalidad));
        txtTotalVInsumo.setText("" + df.format(sumaInsumo));
        txtTotalVMantenimientoSoft.setText("" + df.format(sumaMantenimientoSoft));
        txtTotalVInfraestructura.setText("" + df.format(sumaInfraestructura));
    }//GEN-LAST:event_btnGuardarActionPerformed

    private void btnCalculoUtilidadesPerdidasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCalculoUtilidadesPerdidasActionPerformed
        double totalIngresos = 0, totalGastos = 0;
        
        for(var cuenta: listadoCuentasIngresoGastos){
            if(cuenta.getCategoria() == Categoria.INGRESOS) totalIngresos += cuenta.getSaldo();
            if(cuenta.getCategoria() == Categoria.COSTOS_y_GASTOS) totalGastos += cuenta.getSaldo();
        }
        
        double GastosVsIngresos = totalGastos - totalIngresos;
        
        Color color = (GastosVsIngresos > 0)? Color.GREEN: Color.RED;
        String utilidadVsPerdida = (GastosVsIngresos > 0)? "Utilidades":"Perdidas";
        
        txtUtilidadesPerdidas.setText(String.valueOf(GastosVsIngresos));
        lblResultadoGastosVsIngresos.setText(utilidadVsPerdida);
    }//GEN-LAST:event_btnCalculoUtilidadesPerdidasActionPerformed

    private void btnLimpiaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLimpiaActionPerformed
      
         //
        txtSalariosSup2.setText("");
        txtSalariosOfi2.setText("");
        txtManoDeObraIndirecta2.setText("");
        txtMaterialesIndirectos2.setText("");
        txtSuministros2.setText("");
        txtHerramientas2.setText("");
        txtOtrosMateriales2.setText("");
        txtDepreciacion2.setText("");
        txtImpuestos2.setText("");
        txtServiciosPublicos2.setText("");
        txtServiciosPrivados2.setText("");

        txtTotales2.setText("" );
        txtTotalEspacio.setText("" );
        txtTotalConsumo.setText("" );
        txtTotalEmpleado.setText("" );
        txtTotalHoraHombre.setText("" );
        //Recojo los datos de los textbox de Departamentos
        txtAnalisisEspacio.setText("");
        txtAnalisisConsumo.setText("");
        txtAnalisisEmpleado.setText("");
        txtAnalisisHora.setText("");
        txtDisenoEspacio.setText("");
        txtDisenoConsumo.setText("");
        txtDisenoEmpleado.setText("");
        txtDisenoHora.setText("");
        txtDesarrolloEspacio.setText("");
        txtDesarrolloConsumo.setText("");
        txtDesarrolloEmpleado.setText("");
        txtDesarrolloHora.setText("");
        txtPruebaEspacio.setText("");
        txtPruebaConsumo.setText("");
        txtPruebaEmpleado.setText("");
        txtPruebaHora.setText("");
        txtMantenimientoEspacio.setText("");
        txtMantenimientoConsumo.setText("");
        txtMantenimientoEmpleado.setText("");
        txtMantenimientoHora.setText("");
        txtCalidadEspacio.setText("");
        txtCalidadConsumo.setText("");
        txtCalidadEmpleado.setText("");
        txtCalidadHora.setText("");
        txtInsumoEspacio.setText("");
        txtInsumoConsumo.setText("");
        txtInsumoEmpleado.setText("");
        txtInsumoHora.setText("");
        txtMantenimientoSoftEspacio.setText("");
        txtMantenimientoSoftConsumo.setText("");
        txtMantenimientoSoftEmpleado.setText("");
        txtMantenimientoSoftHora.setText("");
        txtMantenimientoServicioConsumo.setText("");
        txtMantenimientoServicioEmpleado.setText("");
        txtMantenimientoServicioHora.setText("");
        
        
         //
        txtSalarioSupAnalisisProductivo.setText("");
        txtSalarioSupDisenoProductivo.setText("");
        txtSalarioSupDesarrolloProductivo.setText("");
        txtSalarioSupPruebaProductivo.setText("");
        txtSalarioSupMantenimientoProductivo.setText("");
        txtSalarioSupCalidadServicio.setText("");
        txtSalarioSupInsumoServicio.setText("");
        txtSalarioSupMantenimientoSoftServicio.setText("");
        txtSalarioSupInfraestructuraServicio.setText("");
        //
        txtSalarioOfiCalidadServicio.setText("");
        txtSalarioOfiInsumosServicio.setText("");
        txtSalarioOfiMantenimientoSoftServicio.setText("");
        txtSalarioOfiInfraestructuraServicio.setText("");
        //
        txtManoAnalisisProductivo.setText("");
        txtManoDisenoProductivo.setText("");
        txtManoDesarrolloProductivo.setText("");
        txtManoPruebaProductivo.setText("");
        txtManoMantenimientoProductivo.setText("");
        //
        txtMaterialAnalisisProductivo.setText("");
        txtMaterialDisenoProductivo.setText("");
        txtMaterialDesarrolloProductivo.setText("");
        txtMaterialPruebaProductivo.setText("");
        txtMaterialMantenimientoProductivo.setText("");
        //
        txtSumiAnalisisProductivo.setText("");
        txtSumiDisenoProductivo.setText("");
        txtSumiDesarrolloProductivo.setText("");
        txtSumiPruebaProductivo.setText("");
        txtSumiMantenimientoProductivo.setText("");
        //
        txtHerraAnalisisProductivo.setText("");
        txtHerraDisenoProductivo.setText("");
        txtHerraDesarrolloProductivo.setText("");
        txtHerraPruebaProductivo.setText("");
        txtHerraMantenimientoProductivo.setText("");
        txtHerraCalidadServicio.setText("");
        txtHerraInsumoServicio.setText("");
        txtHerraMantenimientoSoftServicio.setText("");
        txtHerraInfraestructuraServicio.setText("");
        //
        txtOtrosAnalisisProductivo.setText("");
        txtOtrosDisenoProductivo.setText("");
        txtOtrosDesarrolloProductivo.setText("");
        txtOtrosPruebaProductivo.setText("");
        txtOtrosMantenimientoProductivo.setText("");
        txtOtrosCalidadServicio.setText("");
        txtOtrosInsumoServicio.setText("");
        txtOtrosMantenimientoSoftServicio.setText("");
        txtOtrosInfraestructuraServicio.setText("");
        //
        txtDepreciacionAnalisisProductivo.setText("");
        txtDepreciacionDisenoProductivo.setText("");
        txtDepreciacionDesarrolloProductivo.setText("");
        txtDepreciacionPruebaProductivo.setText("");
        txtDepreciacionMantenimientoProductivo.setText("");
        txtDepreciacionCalidadServicio.setText("");
        txtDepreciacionInsumoServicio.setText("");
        txtDepreciacionMantenimientoSoftServicio.setText("");
        txtDepreciacionInfraestructuraServicio.setText("");
        //
        txtImpuestosAnalisisProductivo.setText("");
        txtImpuestosDisenoProductivo.setText("");
        txtImpuestosDesarrolloProductivo.setText("");
        txtImpuestosPruebaProductivo.setText("");
        txtImpuestosMantenimientoProductivo.setText("");
        txtImpuestosCalidadServicio.setText("");
        txtImpuestosInsumoServicio.setText("");
        txtImpuestosMantenimientoSoftServicio.setText("");
        txtImpuestosInfraestructuraServicio.setText("");
        //
        txtServicioPublicoAnalisisProductivo.setText("");
        txtServicioPublicoDisenoProductivo.setText("");
        txtServicioPublicoDesarrolloProductivo.setText("");
        txtServicioPublicoPruebaProductivo.setText("");
        txtServicioPublicoMantenimientoProductivo.setText("");
        txtServicioPublicoCalidadServicio.setText("");
        txtServicioPublicoInsumoServicio.setText("");
        txtServicioPublicoMantenimientoSoftServicio.setText("");
        txtServicioPublicoInfraestructuraServicio.setText("");
        //
        txtServicioPrivadoAnalisisProductivo.setText("");
        txtServicioPrivadoDisenoProductivo.setText("");
        txtServicioPrivadoDesarrolloProductivo.setText("");
        txtServicioPrivadoPruebaProductivo.setText("");
        txtServicioPrivadoMantenimientoProductivo.setText("");
        txtServicioPrivadoCalidadServicio.setText("");
        txtServicioPrivadoInsumoServicio.setText("");
        txtServicioPrivadoMantenimientoSoftServicio.setText("");
        txtServicioPrivadoInfraestructuraServicio.setText("");
        //
        txtCalidadAnalisisProductivo.setText("");
        txtCalidadDisenoProductivo.setText("");
        txtCalidadDesarrolloProductivo.setText("");
        txtCalidadPruebaProductivo.setText("");
        txtCalidadMantenimientoProductivo.setText("");
        //
        txtInsumoAnalisisProductivo.setText("");
        txtInsumoDisenoProductivo.setText("");
        txtInsumoDesarrolloProductivo.setText("");
        txtInsumoPruebaProductivo.setText("");
        txtInsumoMantenimientoProductivo.setText("");
        //
        txtMantenimientoSoftAnalisisProductivo.setText("");
        txtMantenimientoSoftDisenoProductivo.setText("");
        txtMantenimientoSoftDesarrolloProductivo.setText("");
        txtMantenimientoSoftPruebaProductivo.setText("");
        txtMantenimientoSoftMantenimientoProductivo.setText("");
        txtMantenimientoServicioEspacio.setText("");
        //
        txtInfraestructuraAnalisisProductivo.setText("");
        txtInfraestructuraDisenoProductivo.setText("");
        txtInfraestructuraDesarrolloProductivo.setText("");
        txtInfraestructuraPruebaProductivo.setText("");
        txtInfraestructuraMantenimientoProductivo.setText("");
        //SETEANDO LOS VALORES DE LOS TOTALES EN LOS TXT FINALES
        txtTotalAnalisis.setText("");
        txtTotalDesarrollo.setText("");
        txtTotalDiseno.setText("");
        txtTotalPrueba.setText("");
        txtTotalMantenimiento.setText("");
        txtTotalCalidad.setText("");
        txtTotalInsumo.setText("");
        txtTotalMantenimientoSoft.setText("");
        txtTotalInfraestructura.setText("");
        //totales segun cada cuenta, parte derecha
        txtTotalSalarioSup.setText("" );
        txtTotalSalarioOfi.setText("" );
        txtTotalMano.setText("" );
        txtTotalMaterial.setText("" );
        txtTotalOtros.setText("" );
        txtTotalDepreciacion.setText("" );
        txtTotalImpuesto.setText("" );
        txtTotalServicioPublico.setText("" );
        txtTotalServicioPrivado.setText("" );
        txtTotalSumi.setText("" );
        txtTotalHerra.setText("" );
        txtTotalPresupuesto.setText("" );
        txtTotalTotal.setText("");
        txtTotalVCalidad.setText("" );
        txtTotalVInsumo.setText("" );
        txtTotalVMantenimientoSoft.setText("" );
        txtTotalVInfraestructura.setText("");

    }//GEN-LAST:event_btnLimpiaActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton5ActionPerformed

    private void btnLimpiarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLimpiarActionPerformed

        txtSalario.setText("");
        txtHoras.setText("");
        txtDiasAguinaldo.setText("");
        txtDiasVacacion.setText("");
        txtDiasTrabajado.setText("");
        txtPorcentajeVacacion.setText("");
        txtPorcentajeSeguro.setText("");
        txtPorcentajeAFP.setText("");
        txtPorcentajeINSAFORP.setText("");
        txtPorcentajeEficiencia.setText("");
        
        //
        txtSeptimo2.setText("");
        txtAguinaldo2.setText("");
        txtVacacion2.setText("");
        txtSalud.setText("");
        txtAFP.setText("");
        txtINSAFORP.setText("");
        txtTotal.setText("");
        txtTotalDia.setText("");
        txtTotalHora.setText("");
        txtDia.setText("");
        txtHora.setText("");
        txtSemana.setText("");
        txtSalarioDia.setText("");
        txtSalarioHora.setText("");
        txtSalarioSemana.setText("");
        txtFactorDia.setText("");
        txtFactorHora.setText("");
        txtFactorSemana.setText("");
        txtDiaEficiencia.setText("");
        txtHoraEficiencia.setText("");
        txtSalarioDiaEficiencia.setText("");
        txtSalarioHoraEficiencia.setText("");
        txtSalarioSemanaEficiencia.setText("");
        txtFactorDiaEficiencia.setText("");
        txtFactorHoraEficiencia.setText("");
        txtFactorSemanaEficiencia.setText("");
    }//GEN-LAST:event_btnLimpiarActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        // TODO add your handling code here:
        DecimalFormat df = new DecimalFormat("#.00");
        double manoObra, ciif, suma;
        System.out.println("El valor del total es:" + valorTotalManoObra);
        manoObra = valorTotalManoObra;
        txtManoDeObra.setText("" + df.format(manoObra));
        ciif = totalCif;
        txtCIF.setText("" + df.format(ciif));
        suma = manoObra + ciif;
        txtCostoTotal.setText("" + df.format(suma));
    }//GEN-LAST:event_jButton4ActionPerformed

    //metodos utilitarios
    public void limpiarTxtPestañaCuentas(){
        txtCodigoCuenta.setText("");
        txtNombreCuenta.setText("");
        cmbSeleccionarCuenta.setSelectedIndex(-1);
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
    private javax.swing.JButton btnCalcularManoDeObra;
    private javax.swing.JButton btnCalculoUtilidadesPerdidas;
    private javax.swing.JButton btnCrearNuevoArchivoInfc;
    private javax.swing.JButton btnGenerarBalanzaComprobacion;
    private javax.swing.JButton btnGenerarEstadoResultado;
    private javax.swing.JButton btnGuardar;
    private javax.swing.JButton btnLimpia;
    private javax.swing.JButton btnLimpiar;
    private javax.swing.JButton btnModificarCuenta;
    private javax.swing.JButton btnOlvidarSeleccionCuenta;
    private javax.swing.JComboBox<String> cmbSeleccionarCuenta;
    private javax.swing.JTabbedPane contenedorPestañas;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel100;
    private javax.swing.JLabel jLabel101;
    private javax.swing.JLabel jLabel102;
    private javax.swing.JLabel jLabel103;
    private javax.swing.JLabel jLabel104;
    private javax.swing.JLabel jLabel105;
    private javax.swing.JLabel jLabel106;
    private javax.swing.JLabel jLabel107;
    private javax.swing.JLabel jLabel108;
    private javax.swing.JLabel jLabel109;
    private javax.swing.JLabel jLabel110;
    private javax.swing.JLabel jLabel111;
    private javax.swing.JLabel jLabel112;
    private javax.swing.JLabel jLabel113;
    private javax.swing.JLabel jLabel114;
    private javax.swing.JLabel jLabel115;
    private javax.swing.JLabel jLabel116;
    private javax.swing.JLabel jLabel117;
    private javax.swing.JLabel jLabel118;
    private javax.swing.JLabel jLabel119;
    private javax.swing.JLabel jLabel120;
    private javax.swing.JLabel jLabel121;
    private javax.swing.JLabel jLabel122;
    private javax.swing.JLabel jLabel123;
    private javax.swing.JLabel jLabel124;
    private javax.swing.JLabel jLabel125;
    private javax.swing.JLabel jLabel126;
    private javax.swing.JLabel jLabel127;
    private javax.swing.JLabel jLabel128;
    private javax.swing.JLabel jLabel129;
    private javax.swing.JLabel jLabel130;
    private javax.swing.JLabel jLabel131;
    private javax.swing.JLabel jLabel132;
    private javax.swing.JLabel jLabel133;
    private javax.swing.JLabel jLabel134;
    private javax.swing.JLabel jLabel135;
    private javax.swing.JLabel jLabel136;
    private javax.swing.JLabel jLabel137;
    private javax.swing.JLabel jLabel138;
    private javax.swing.JLabel jLabel139;
    private javax.swing.JLabel jLabel140;
    private javax.swing.JLabel jLabel141;
    private javax.swing.JLabel jLabel142;
    private javax.swing.JLabel jLabel143;
    private javax.swing.JLabel jLabel144;
    private javax.swing.JLabel jLabel145;
    private javax.swing.JLabel jLabel146;
    private javax.swing.JLabel jLabel147;
    private javax.swing.JLabel jLabel148;
    private javax.swing.JLabel jLabel149;
    private javax.swing.JLabel jLabel173;
    private javax.swing.JLabel jLabel174;
    private javax.swing.JLabel jLabel175;
    private javax.swing.JLabel jLabel176;
    private javax.swing.JLabel jLabel177;
    private javax.swing.JLabel jLabel178;
    private javax.swing.JLabel jLabel179;
    private javax.swing.JLabel jLabel180;
    private javax.swing.JLabel jLabel181;
    private javax.swing.JLabel jLabel182;
    private javax.swing.JLabel jLabel183;
    private javax.swing.JLabel jLabel184;
    private javax.swing.JLabel jLabel185;
    private javax.swing.JLabel jLabel186;
    private javax.swing.JLabel jLabel187;
    private javax.swing.JLabel jLabel188;
    private javax.swing.JLabel jLabel189;
    private javax.swing.JLabel jLabel190;
    private javax.swing.JLabel jLabel191;
    private javax.swing.JLabel jLabel192;
    private javax.swing.JLabel jLabel193;
    private javax.swing.JLabel jLabel194;
    private javax.swing.JLabel jLabel195;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel37;
    private javax.swing.JLabel jLabel38;
    private javax.swing.JLabel jLabel39;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel40;
    private javax.swing.JLabel jLabel41;
    private javax.swing.JLabel jLabel42;
    private javax.swing.JLabel jLabel43;
    private javax.swing.JLabel jLabel44;
    private javax.swing.JLabel jLabel45;
    private javax.swing.JLabel jLabel46;
    private javax.swing.JLabel jLabel47;
    private javax.swing.JLabel jLabel48;
    private javax.swing.JLabel jLabel49;
    private javax.swing.JLabel jLabel50;
    private javax.swing.JLabel jLabel51;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel74;
    private javax.swing.JLabel jLabel75;
    private javax.swing.JLabel jLabel76;
    private javax.swing.JLabel jLabel77;
    private javax.swing.JLabel jLabel78;
    private javax.swing.JLabel jLabel79;
    private javax.swing.JLabel jLabel80;
    private javax.swing.JLabel jLabel81;
    private javax.swing.JLabel jLabel82;
    private javax.swing.JLabel jLabel83;
    private javax.swing.JLabel jLabel84;
    private javax.swing.JLabel jLabel85;
    private javax.swing.JLabel jLabel86;
    private javax.swing.JLabel jLabel87;
    private javax.swing.JLabel jLabel88;
    private javax.swing.JLabel jLabel89;
    private javax.swing.JLabel jLabel90;
    private javax.swing.JLabel jLabel92;
    private javax.swing.JLabel jLabel93;
    private javax.swing.JLabel jLabel94;
    private javax.swing.JLabel jLabel95;
    private javax.swing.JLabel jLabel96;
    private javax.swing.JLabel jLabel97;
    private javax.swing.JLabel jLabel98;
    private javax.swing.JLabel jLabel99;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel15;
    private javax.swing.JPanel jPanel16;
    private javax.swing.JPanel jPanel18;
    private javax.swing.JPanel jPanel19;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel20;
    private javax.swing.JPanel jPanel21;
    private javax.swing.JPanel jPanel22;
    private javax.swing.JPanel jPanel23;
    private javax.swing.JPanel jPanel24;
    private javax.swing.JPanel jPanel25;
    private javax.swing.JPanel jPanel26;
    private javax.swing.JPanel jPanel27;
    private javax.swing.JPanel jPanel28;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel30;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JScrollPane jScrollPane8;
    private javax.swing.JLabel lblInfoEstadoResultado;
    private javax.swing.JLabel lblInforBalanzaComprobacion;
    private javax.swing.JLabel lblResultadoGastosVsIngresos;
    private javax.swing.JList<String> lstCuentasDisponibles;
    private javax.swing.JTable tablaBalanzaComprobacion;
    private javax.swing.JTable tablaEstadoResultado;
    private javax.swing.JTable tablaLibroDiario;
    private javax.swing.JTable tablaLibroMayor;
    private javax.swing.JTextField txtAFP;
    private javax.swing.JTextField txtAguinaldo2;
    private javax.swing.JTextField txtAnalisisConsumo;
    private javax.swing.JTextField txtAnalisisEmpleado;
    private javax.swing.JTextField txtAnalisisEspacio;
    private javax.swing.JTextField txtAnalisisHora;
    private javax.swing.JTextField txtCIF;
    private javax.swing.JTextField txtCalidadAnalisisProductivo;
    private javax.swing.JTextField txtCalidadCalidadServicio;
    private javax.swing.JTextField txtCalidadConsumo;
    private javax.swing.JTextField txtCalidadDesarrolloProductivo;
    private javax.swing.JTextField txtCalidadDisenoProductivo;
    private javax.swing.JTextField txtCalidadEmpleado;
    private javax.swing.JTextField txtCalidadEspacio;
    private javax.swing.JTextField txtCalidadHora;
    private javax.swing.JTextField txtCalidadInfraestructuraServicio;
    private javax.swing.JTextField txtCalidadInsumoServicio;
    private javax.swing.JTextField txtCalidadMantenimientoProductivo;
    private javax.swing.JTextField txtCalidadMantenimientoSoftServicio;
    private javax.swing.JTextField txtCalidadPruebaProductivo;
    private javax.swing.JTextField txtCodigoCuenta;
    private javax.swing.JTextField txtCostoTotal;
    private javax.swing.JTextField txtDebe;
    private javax.swing.JTextField txtDepreciacion2;
    private javax.swing.JTextField txtDepreciacionAnalisisProductivo;
    private javax.swing.JTextField txtDepreciacionCalidadServicio;
    private javax.swing.JTextField txtDepreciacionDesarrolloProductivo;
    private javax.swing.JTextField txtDepreciacionDisenoProductivo;
    private javax.swing.JTextField txtDepreciacionInfraestructuraServicio;
    private javax.swing.JTextField txtDepreciacionInsumoServicio;
    private javax.swing.JTextField txtDepreciacionMantenimientoProductivo;
    private javax.swing.JTextField txtDepreciacionMantenimientoSoftServicio;
    private javax.swing.JTextField txtDepreciacionPruebaProductivo;
    private javax.swing.JTextField txtDesarrolloConsumo;
    private javax.swing.JTextField txtDesarrolloEmpleado;
    private javax.swing.JTextField txtDesarrolloEspacio;
    private javax.swing.JTextField txtDesarrolloHora;
    private javax.swing.JTextField txtDia;
    private javax.swing.JTextField txtDiaEficiencia;
    private javax.swing.JTextField txtDiasAguinaldo;
    private javax.swing.JTextField txtDiasTrabajado;
    private javax.swing.JTextField txtDiasVacacion;
    private javax.swing.JTextField txtDisenoConsumo;
    private javax.swing.JTextField txtDisenoEmpleado;
    private javax.swing.JTextField txtDisenoEspacio;
    private javax.swing.JTextField txtDisenoHora;
    private javax.swing.JTextField txtFactorDia;
    private javax.swing.JTextField txtFactorDiaEficiencia;
    private javax.swing.JTextField txtFactorHora;
    private javax.swing.JTextField txtFactorHoraEficiencia;
    private javax.swing.JTextField txtFactorSemana;
    private javax.swing.JTextField txtFactorSemanaEficiencia;
    private javax.swing.JTextField txtHaber;
    private javax.swing.JTextField txtHerraAnalisisProductivo;
    private javax.swing.JTextField txtHerraCalidadServicio;
    private javax.swing.JTextField txtHerraDesarrolloProductivo;
    private javax.swing.JTextField txtHerraDisenoProductivo;
    private javax.swing.JTextField txtHerraInfraestructuraServicio;
    private javax.swing.JTextField txtHerraInsumoServicio;
    private javax.swing.JTextField txtHerraMantenimientoProductivo;
    private javax.swing.JTextField txtHerraMantenimientoSoftServicio;
    private javax.swing.JTextField txtHerraPruebaProductivo;
    private javax.swing.JTextField txtHerramientas2;
    private javax.swing.JTextField txtHora;
    private javax.swing.JTextField txtHoraEficiencia;
    private javax.swing.JTextField txtHoras;
    private javax.swing.JTextField txtINSAFORP;
    private javax.swing.JTextField txtImpuestos2;
    private javax.swing.JTextField txtImpuestosAnalisisProductivo;
    private javax.swing.JTextField txtImpuestosCalidadServicio;
    private javax.swing.JTextField txtImpuestosDesarrolloProductivo;
    private javax.swing.JTextField txtImpuestosDisenoProductivo;
    private javax.swing.JTextField txtImpuestosInfraestructuraServicio;
    private javax.swing.JTextField txtImpuestosInsumoServicio;
    private javax.swing.JTextField txtImpuestosMantenimientoProductivo;
    private javax.swing.JTextField txtImpuestosMantenimientoSoftServicio;
    private javax.swing.JTextField txtImpuestosPruebaProductivo;
    private javax.swing.JTextField txtInfraestructuraAnalisisProductivo;
    private javax.swing.JTextField txtInfraestructuraCalidadServicio;
    private javax.swing.JTextField txtInfraestructuraDesarrolloProductivo;
    private javax.swing.JTextField txtInfraestructuraDisenoProductivo;
    private javax.swing.JTextField txtInfraestructuraInfraestructuraServicio;
    private javax.swing.JTextField txtInfraestructuraInsumoServicio;
    private javax.swing.JTextField txtInfraestructuraMantenimientoProductivo;
    private javax.swing.JTextField txtInfraestructuraMantenimientoSoftServicio;
    private javax.swing.JTextField txtInfraestructuraPruebaProductivo;
    private javax.swing.JTextField txtInsumoAnalisisProductivo;
    private javax.swing.JTextField txtInsumoCalidadServicio;
    private javax.swing.JTextField txtInsumoConsumo;
    private javax.swing.JTextField txtInsumoDesarrolloProductivo;
    private javax.swing.JTextField txtInsumoDisenoProductivo;
    private javax.swing.JTextField txtInsumoEmpleado;
    private javax.swing.JTextField txtInsumoEspacio;
    private javax.swing.JTextField txtInsumoHora;
    private javax.swing.JTextField txtInsumoInfraestructuraServicio;
    private javax.swing.JTextField txtInsumoInsumoServicio;
    private javax.swing.JTextField txtInsumoMantenimientoProductivo;
    private javax.swing.JTextField txtInsumoMantenimientoSoftServicio;
    private javax.swing.JTextField txtInsumoPruebaProductivo;
    private javax.swing.JTextField txtManoAnalisisProductivo;
    private javax.swing.JTextField txtManoCalidadServicio;
    private javax.swing.JTextField txtManoDeObra;
    private javax.swing.JTextField txtManoDeObraIndirecta2;
    private javax.swing.JTextField txtManoDesarrolloProductivo;
    private javax.swing.JTextField txtManoDisenoProductivo;
    private javax.swing.JTextField txtManoInfraestructuraServicio;
    private javax.swing.JTextField txtManoInsumoServicio;
    private javax.swing.JTextField txtManoMantenimientoProductivo;
    private javax.swing.JTextField txtManoMantenimientoSoftServicio;
    private javax.swing.JTextField txtManoPruebaProductivo;
    private javax.swing.JTextField txtMantenimientoConsumo;
    private javax.swing.JTextField txtMantenimientoEmpleado;
    private javax.swing.JTextField txtMantenimientoEspacio;
    private javax.swing.JTextField txtMantenimientoHora;
    private javax.swing.JTextField txtMantenimientoServicioConsumo;
    private javax.swing.JTextField txtMantenimientoServicioEmpleado;
    private javax.swing.JTextField txtMantenimientoServicioEspacio;
    private javax.swing.JTextField txtMantenimientoServicioHora;
    private javax.swing.JTextField txtMantenimientoSoftAnalisisProductivo;
    private javax.swing.JTextField txtMantenimientoSoftCalidadServicio;
    private javax.swing.JTextField txtMantenimientoSoftConsumo;
    private javax.swing.JTextField txtMantenimientoSoftDesarrolloProductivo;
    private javax.swing.JTextField txtMantenimientoSoftDisenoProductivo;
    private javax.swing.JTextField txtMantenimientoSoftEmpleado;
    private javax.swing.JTextField txtMantenimientoSoftEspacio;
    private javax.swing.JTextField txtMantenimientoSoftHora;
    private javax.swing.JTextField txtMantenimientoSoftInfraestructuraServicio;
    private javax.swing.JTextField txtMantenimientoSoftInsumoServicio;
    private javax.swing.JTextField txtMantenimientoSoftMantenimientoProductivo;
    private javax.swing.JTextField txtMantenimientoSoftMantenimientoSoftServicio;
    private javax.swing.JTextField txtMantenimientoSoftPruebaProductivo;
    private javax.swing.JTextField txtMaterialAnalisisProductivo;
    private javax.swing.JTextField txtMaterialCalidadServicio;
    private javax.swing.JTextField txtMaterialDesarrolloProductivo;
    private javax.swing.JTextField txtMaterialDisenoProductivo;
    private javax.swing.JTextField txtMaterialInfraestructuraServicio;
    private javax.swing.JTextField txtMaterialInsumoServicio;
    private javax.swing.JTextField txtMaterialMantenimientoProductivo;
    private javax.swing.JTextField txtMaterialMantenimientoSoftServicio;
    private javax.swing.JTextField txtMaterialPruebaProductivo;
    private javax.swing.JTextField txtMaterialesIndirectos2;
    private javax.swing.JTextField txtNombreCuenta;
    private javax.swing.JTextField txtOtrosAnalisisProductivo;
    private javax.swing.JTextField txtOtrosCalidadServicio;
    private javax.swing.JTextField txtOtrosDesarrolloProductivo;
    private javax.swing.JTextField txtOtrosDisenoProductivo;
    private javax.swing.JTextField txtOtrosInfraestructuraServicio;
    private javax.swing.JTextField txtOtrosInsumoServicio;
    private javax.swing.JTextField txtOtrosMantenimientoProductivo;
    private javax.swing.JTextField txtOtrosMantenimientoSoftServicio;
    private javax.swing.JTextField txtOtrosMateriales2;
    private javax.swing.JTextField txtOtrosPruebaProductivo;
    private javax.swing.JTextField txtPorcentajeAFP;
    private javax.swing.JTextField txtPorcentajeEficiencia;
    private javax.swing.JTextField txtPorcentajeINSAFORP;
    private javax.swing.JTextField txtPorcentajeSeguro;
    private javax.swing.JTextField txtPorcentajeVacacion;
    private javax.swing.JTextField txtPruebaConsumo;
    private javax.swing.JTextField txtPruebaEmpleado;
    private javax.swing.JTextField txtPruebaEspacio;
    private javax.swing.JTextField txtPruebaHora;
    private javax.swing.JTextField txtSalario;
    private javax.swing.JTextField txtSalarioDia;
    private javax.swing.JTextField txtSalarioDiaEficiencia;
    private javax.swing.JTextField txtSalarioHora;
    private javax.swing.JTextField txtSalarioHoraEficiencia;
    private javax.swing.JTextField txtSalarioOfiAnalisisProductivo;
    private javax.swing.JTextField txtSalarioOfiCalidadServicio;
    private javax.swing.JTextField txtSalarioOfiDesarrolloProductivo;
    private javax.swing.JTextField txtSalarioOfiDisenoProductivo;
    private javax.swing.JTextField txtSalarioOfiInfraestructuraServicio;
    private javax.swing.JTextField txtSalarioOfiInsumosServicio;
    private javax.swing.JTextField txtSalarioOfiMantenimientoProductivo;
    private javax.swing.JTextField txtSalarioOfiMantenimientoSoftServicio;
    private javax.swing.JTextField txtSalarioOfiPruebaProductivo;
    private javax.swing.JTextField txtSalarioSemana;
    private javax.swing.JTextField txtSalarioSemanaEficiencia;
    private javax.swing.JTextField txtSalarioSupAnalisisProductivo;
    private javax.swing.JTextField txtSalarioSupCalidadServicio;
    private javax.swing.JTextField txtSalarioSupDesarrolloProductivo;
    private javax.swing.JTextField txtSalarioSupDisenoProductivo;
    private javax.swing.JTextField txtSalarioSupInfraestructuraServicio;
    private javax.swing.JTextField txtSalarioSupInsumoServicio;
    private javax.swing.JTextField txtSalarioSupMantenimientoProductivo;
    private javax.swing.JTextField txtSalarioSupMantenimientoSoftServicio;
    private javax.swing.JTextField txtSalarioSupPruebaProductivo;
    private javax.swing.JTextField txtSalariosOfi2;
    private javax.swing.JTextField txtSalariosSup2;
    private javax.swing.JTextField txtSalud;
    private javax.swing.JTextField txtSemana;
    private javax.swing.JTextField txtSemanaEficiencia;
    private javax.swing.JTextField txtSeptimo2;
    private javax.swing.JTextField txtServicioPrivadoAnalisisProductivo;
    private javax.swing.JTextField txtServicioPrivadoCalidadServicio;
    private javax.swing.JTextField txtServicioPrivadoDesarrolloProductivo;
    private javax.swing.JTextField txtServicioPrivadoDisenoProductivo;
    private javax.swing.JTextField txtServicioPrivadoInfraestructuraServicio;
    private javax.swing.JTextField txtServicioPrivadoInsumoServicio;
    private javax.swing.JTextField txtServicioPrivadoMantenimientoProductivo;
    private javax.swing.JTextField txtServicioPrivadoMantenimientoSoftServicio;
    private javax.swing.JTextField txtServicioPrivadoPruebaProductivo;
    private javax.swing.JTextField txtServicioPublicoAnalisisProductivo;
    private javax.swing.JTextField txtServicioPublicoCalidadServicio;
    private javax.swing.JTextField txtServicioPublicoDesarrolloProductivo;
    private javax.swing.JTextField txtServicioPublicoDisenoProductivo;
    private javax.swing.JTextField txtServicioPublicoInfraestructuraServicio;
    private javax.swing.JTextField txtServicioPublicoInsumoServicio;
    private javax.swing.JTextField txtServicioPublicoMantenimientoProductivo;
    private javax.swing.JTextField txtServicioPublicoMantenimientoSoftServicio;
    private javax.swing.JTextField txtServicioPublicoPruebaProductivo;
    private javax.swing.JTextField txtServiciosPrivados2;
    private javax.swing.JTextField txtServiciosPublicos2;
    private javax.swing.JTextField txtSumiAnalisisProductivo;
    private javax.swing.JTextField txtSumiCalidadServicio;
    private javax.swing.JTextField txtSumiDesarrolloProductivo;
    private javax.swing.JTextField txtSumiDisenoProductivo;
    private javax.swing.JTextField txtSumiInfraestructuraServicio;
    private javax.swing.JTextField txtSumiInsumoServicio;
    private javax.swing.JTextField txtSumiMantenimientoProductivo;
    private javax.swing.JTextField txtSumiMantenimientoSoftServicio;
    private javax.swing.JTextField txtSumiPruebaProductivo;
    private javax.swing.JTextField txtSuministros2;
    private javax.swing.JTextField txtTotal;
    private javax.swing.JTextField txtTotalAnalisis;
    private javax.swing.JTextField txtTotalCalidad;
    private javax.swing.JTextField txtTotalConsumo;
    private javax.swing.JTextField txtTotalDepreciacion;
    private javax.swing.JTextField txtTotalDesarrollo;
    private javax.swing.JTextField txtTotalDia;
    private javax.swing.JTextField txtTotalDiseno;
    private javax.swing.JTextField txtTotalEmpleado;
    private javax.swing.JTextField txtTotalEspacio;
    private javax.swing.JTextField txtTotalHerra;
    private javax.swing.JTextField txtTotalHora;
    private javax.swing.JTextField txtTotalHoraHombre;
    private javax.swing.JTextField txtTotalImpuesto;
    private javax.swing.JTextField txtTotalInfraestructura;
    private javax.swing.JTextField txtTotalInsumo;
    private javax.swing.JTextField txtTotalMano;
    private javax.swing.JTextField txtTotalMantenimiento;
    private javax.swing.JTextField txtTotalMantenimientoSoft;
    private javax.swing.JTextField txtTotalMaterial;
    private javax.swing.JTextField txtTotalOtros;
    private javax.swing.JTextField txtTotalPresupuesto;
    private javax.swing.JTextField txtTotalPrueba;
    private javax.swing.JTextField txtTotalSalarioOfi;
    private javax.swing.JTextField txtTotalSalarioSup;
    private javax.swing.JTextField txtTotalServicioPrivado;
    private javax.swing.JTextField txtTotalServicioPublico;
    private javax.swing.JTextField txtTotalSumi;
    private javax.swing.JTextField txtTotalTotal;
    private javax.swing.JTextField txtTotalVCalidad;
    private javax.swing.JTextField txtTotalVInfraestructura;
    private javax.swing.JTextField txtTotalVInsumo;
    private javax.swing.JTextField txtTotalVMantenimientoSoft;
    private javax.swing.JTextField txtTotales2;
    private javax.swing.JTextField txtUtilidadesPerdidas;
    private javax.swing.JTextField txtVacacion2;
    // End of variables declaration//GEN-END:variables

    @Override
    public void valueChanged(ListSelectionEvent e) {
        
        
        JList<String> listadoCuentas = (JList) e.getSource();
        int indiceCuentaSeleccionada = listadoCuentas.getSelectedIndex();
        if(indiceCuentaSeleccionada == -1) return;
        
        int codigoCuentaSeleccionada = controladorCuentasDisp.getListadoCuentas().get(indiceCuentaSeleccionada).getCodCuenta();
        
        btnModificarCuenta.setEnabled(true);
        txtCodigoCuenta.setText(String.valueOf(codigoCuentaSeleccionada));
        cmbSeleccionarCuenta.setSelectedItem(cuentas.get(indiceCuentaSeleccionada).getCategoria().toString());
        txtNombreCuenta.setText(listadoCuentas.getSelectedValue());
        btnOlvidarSeleccionCuenta.setEnabled(true);
    }
}