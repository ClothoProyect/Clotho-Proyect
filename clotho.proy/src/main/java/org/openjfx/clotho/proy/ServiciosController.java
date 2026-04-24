package org.openjfx.clotho.proy;

import org.openjfx.clotho.proy.dao.hbnt.ServicioDaoHBNT;
import org.openjfx.clotho.proy.exception.ProyectoClothoException;
import org.openjfx.clotho.proy.vo.Servicio;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class ServiciosController {

    @FXML private TextField txtNombreServicio;
    @FXML private TextField txtPrecioServicio;
    @FXML private TextArea txtDescripcionServicio;
    
    private PrimaryController controladorPrincipal;

    public void setControladorPrincipal(PrimaryController principal) {
        this.controladorPrincipal = principal;
    }

    @FXML
    private void guardarServicio() {
    	Servicio servicio = new Servicio();
        ServicioDaoHBNT servicioHBNT = new ServicioDaoHBNT();
        
        try {
            servicio.setNombre(txtNombreServicio.getText());
            // Buscamos si ya existe
            Servicio servicioDao = servicioHBNT.obtenerEntidadPorNombre(servicio);
            
            if (servicioDao == null) {
                // 1. Configurar datos básicos
                servicio.setPrecio(Float.parseFloat(txtPrecioServicio.getText()));
                servicio.setDescripcion(txtDescripcionServicio.getText());
                
                // 2. CALCULAR EL ID ANTES DE CREAR (Crucial)
                int proximoId = servicioHBNT.obtenerUltimoIdentificador() + 1;
                servicio.setIdentificador(proximoId);
                
                // 3. AHORA SÍ, PERSISTIR EN LA BASE DE DATOS
                servicioHBNT.crearEntidad(servicio);
                
                servicioDao = servicio;
                System.out.println("Servicio nuevo creado con ID: " + proximoId);
            }
            
            // Enviamos el servicio (ya sea el encontrado o el nuevo) al controlador principal
            controladorPrincipal.recibirDatosServicio(servicioDao);
            
        } catch (ProyectoClothoException | NumberFormatException e) {
            System.err.println("Error al guardar servicio: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Cerrar ventana
        Stage stage = (Stage) txtNombreServicio.getScene().getWindow();
        stage.close();
    }
}