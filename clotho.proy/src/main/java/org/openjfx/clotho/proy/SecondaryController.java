package org.openjfx.clotho.proy;

import org.openjfx.clotho.proy.dao.hbnt.ClienteDaoHBNT;
import org.openjfx.clotho.proy.exception.ProyectoClothoException;
import org.openjfx.clotho.proy.vo.Cliente;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class SecondaryController {

	@FXML private TextField txtNombre;
	@FXML private TextField txtApellidos;
	@FXML private TextField txtTelefono;
	@FXML private TextField txtCIF;
	@FXML private TextField txtEmail;
	@FXML private TextField txtDireccion;
	@FXML private TextField txtCodigoPostal;
	@FXML private TextField txtNotasAdicionales;

	private PrimaryController controladorPrincipal;

	public void setControladorPrincipal(PrimaryController principal) {
		this.controladorPrincipal = principal;
	}

	@FXML
	private void guardarCliente() {
		ClienteDaoHBNT clienteHBNT = new ClienteDaoHBNT();
		try {
			Cliente cliente = new Cliente();
			cliente.setNombre(txtNombre.getText());
			
			Cliente clienteDao = clienteHBNT.obtenerEntidadPorNombre(cliente);
			
			if (clienteDao == null) {
				if(!txtCodigoPostal.getText().equals("")) {
					int codigoPostal = Integer.parseInt(txtCodigoPostal.getText());
					cliente.setCodigoPostal(codigoPostal);
				}
				cliente.setIdentificador(clienteHBNT.obtenerUltimoIdentificador() + 1);
				cliente.setApellidos(txtApellidos.getText());
				cliente.setTelefono(txtTelefono.getText());
				cliente.setCif(txtCIF.getText());
				cliente.setDireccion(txtDireccion.getText());
				cliente.setEmail(txtEmail.getText());
				cliente.setNotasAdicionales(txtNotasAdicionales.getText());
				
				clienteHBNT.crearEntidad(cliente);
				clienteDao = cliente;
			}
			
			controladorPrincipal.recibirDatosCliente(clienteDao);
		} catch (ProyectoClothoException e) {
			e.printStackTrace();
		}

		Stage stage = (Stage) txtNombre.getScene().getWindow();
		stage.close();
	}
}