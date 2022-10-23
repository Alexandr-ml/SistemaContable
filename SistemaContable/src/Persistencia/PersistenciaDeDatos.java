/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Persistencia;

import ModeloContable.InformacionContable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
/**
 *
 * @author pc
 * 
 */
public class PersistenciaDeDatos {
    private static PersistenciaDeDatos persistenciaDeDatos = new PersistenciaDeDatos();
    
    //Almacenamiento de la informacion contable
    private ObjectOutputStream objectOutputStream;
    private FileOutputStream fileOutputStream;
    
    //Recuperacion de la informacion contable
    private ObjectInputStream objectInputStream;
    private FileInputStream fileInputStream;
    
    private File archivo;

   
    private PersistenciaDeDatos(){
    }
    
    public static PersistenciaDeDatos getPersistenciaDeDatos(){
        return persistenciaDeDatos;
    }
    
   
    
    public void guardarDatos(InformacionContable informacionContable) throws FileNotFoundException, IOException{
        
        fileOutputStream = new FileOutputStream(archivo);
        objectOutputStream = new ObjectOutputStream(fileOutputStream);
        objectOutputStream.writeObject(informacionContable);
        objectOutputStream.close();
        objectOutputStream.flush();
        
    }
    
    public InformacionContable recuperarDatos() throws FileNotFoundException, IOException, ClassNotFoundException{
        fileInputStream = new FileInputStream(archivo);
        objectInputStream = new ObjectInputStream(fileInputStream);
        
        InformacionContable info = (InformacionContable) objectInputStream.readObject();
        
        return info;
    }
    

    
    public void configurarArchivo(String ruta){
        archivo = new File(ruta);
    }
    
    public void configurarArchivoNuevoParaAlmacenar(String ruta, String nombre){
        String directorio = ruta+"\\"+nombre+".ser";
        archivo = new File(directorio);
    }
    
}
