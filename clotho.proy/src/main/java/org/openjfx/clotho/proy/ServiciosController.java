package org.openjfx.clotho.proy;

import org.openjfx.clotho.proy.dao.hbnt.ServicioDaoHBNT;
import org.openjfx.clotho.proy.exception.ProyectoClothoException;
import org.openjfx.clotho.proy.vo.Servicio;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class ServiciosController {

	@FXML private TextField txtNombreServicio;
	@FXML private TextField txtPrecioServicio;
	@FXML private TextField txtDescripcionServicio;
	@FXML private TextField txtDescuento;
	@FXML private Label lblAnuncio;
	
	private PrimaryController controladorPrincipal;

	public void setControladorPrincipal(PrimaryController principal) {
		this.controladorPrincipal = principal;
	}

	@FXML
	private void guardarServicio() {
		ServicioDaoHBNT servicioHBNT = new ServicioDaoHBNT();
		
		String precioStr = txtPrecioServicio.getText().trim();
	    String descStr = txtDescuento.getText().trim();

	    if (precioStr.isEmpty() || !precioStr.matches("\\d+(\\.\\d+)?")) {
	        lblAnuncio.setText("Error: Formato de precio inválido.");
	        return;
	    } else if (!descStr.matches("\\d?(\\.\\d+)?")) {
	    	lblAnuncio.setText("Error: Formato de descuento inválido.");
	        return;
	    }

	    try {
	        Float precioBase = Float.parseFloat(precioStr);
	        Float descuento = descStr.isEmpty() ? 0f : Float.parseFloat(descStr);
	        Float precioFinal = precioBase - (precioBase * (descuento / 100));
		
			Servicio servicio = new Servicio();
			servicio.setNombre(txtNombreServicio.getText().trim());
			Servicio servicioExistente = servicioHBNT.obtenerEntidadPorNombre(servicio);
			
			if (servicioExistente == null) {
				servicio.setIdentificador(servicioHBNT.obtenerUltimoIdentificador() + 1);
				servicio.setPrecio(precioFinal);
				servicio.setDescripcion(txtDescripcionServicio.getText());
				servicioHBNT.crearEntidad(servicio);
				servicioExistente = servicio;
			}
			
			controladorPrincipal.recibirDatosServicio(servicioExistente);
			
		} catch (ProyectoClothoException | NumberFormatException e) {
			e.printStackTrace();
		}
	}
	
	@FXML
	private void finalizarServicio() {
		Stage stage = (Stage) txtNombreServicio.getScene().getWindow();
		stage.close();
	}
}