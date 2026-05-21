package org.openjfx.clotho.proy;

import java.util.function.UnaryOperator;

import org.openjfx.clotho.proy.dao.ClienteDAO;
import org.openjfx.clotho.proy.dao.hbnt.ClienteDaoHBNT;
import org.openjfx.clotho.proy.exception.ProyectoClothoException;
import org.openjfx.clotho.proy.vo.Cliente;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.stage.Stage;
import javafx.util.StringConverter;

public class ClientesController {

	private ClienteDAO clienteDao = new ClienteDaoHBNT();
	private ObservableList<Cliente> clientesObs;
	private boolean sincronizandoCliente = false;
	private ObjectProperty<Cliente> clienteActual = new SimpleObjectProperty<>(null);
	private PrincipalController controladorPrincipal;

	@FXML
	private Label lblAviso;
	@FXML
	private TextField txtNombre;
	@FXML
	private TextField txtApellidos;
	@FXML
	private TextField txtTelefono;
	@FXML
	private TextField txtCIF;
	@FXML
	private TextField txtEmail;
	@FXML
	private TextField txtDireccion;
	@FXML
	private TextField txtCodigoPostal;
	@FXML
	private TextField txtNotasAdicionales;
	@FXML
	private ComboBox<Cliente> cmbBusquedaNombreCliente;
	@FXML
	private ComboBox<Cliente> cmbBusquedaTelefonoCliente;
	@FXML
	private Button btnLimpiar;
	@FXML
	private Button btnGuardar;
	@FXML
	private Button btnEliminar;
	@FXML
	private Button btnConfirmar;
	@FXML
	private Button btnCancelar;

	// Este es nuestro "interruptor" reactivo. Empieza en false (apagado).
	private BooleanProperty procesoElimanacion = new SimpleBooleanProperty(false);

	@FXML
	public void initialize() {
		UnaryOperator<TextFormatter.Change> filtroMayusculas = cambio -> {
			cambio.setText(cambio.getText().toUpperCase());
			return cambio;
		};

		txtNombre.setTextFormatter(new TextFormatter<>(filtroMayusculas));
		txtApellidos.setTextFormatter(new TextFormatter<>(filtroMayusculas));
		txtCIF.setTextFormatter(new TextFormatter<>(filtroMayusculas));
		txtDireccion.setTextFormatter(new TextFormatter<>(filtroMayusculas));
		txtNotasAdicionales.setTextFormatter(new TextFormatter<>(filtroMayusculas));

		try {
			clientesObs = FXCollections.observableArrayList(clienteDao.obtenerListaTodasEntidades());
		} catch (ProyectoClothoException e) {
			e.printStackTrace();
		}

		cmbBusquedaNombreCliente.setItems(clientesObs);
		cmbBusquedaTelefonoCliente.setItems(clientesObs);

		cmbBusquedaNombreCliente.setConverter(new StringConverter<Cliente>() {
			@Override
			public String toString(Cliente cliente) {
				return cliente == null ? "" : cliente.getNombreCompleto();
			}

			@Override
			public Cliente fromString(String filtro) {
				if (filtro == null || filtro.isEmpty()) {
					return null;
				}
				return clientesObs.stream()
						.filter(c -> c.getNombreCompleto().toUpperCase().contains(filtro.toUpperCase())).findFirst()
						.orElse(null);
			}
		});

		cmbBusquedaTelefonoCliente.setConverter(new StringConverter<Cliente>() {
			@Override
			public String toString(Cliente cliente) {
				return cliente == null ? "" : cliente.getTelefono();
			}

			@Override
			public Cliente fromString(String filtro) {
				if (filtro == null || filtro.isEmpty()) {
					return null;
				}
				return clientesObs.stream().filter(c -> c.getTelefono().contains(filtro)).findFirst().orElse(null);
			}
		});

		cmbBusquedaNombreCliente.valueProperty().addListener((obs, oldVal, newVal) -> cargarDatosCliente(newVal));
		cmbBusquedaTelefonoCliente.valueProperty().addListener((obs, oldVal, newVal) -> cargarDatosCliente(newVal));

		btnLimpiar.visibleProperty().bind(procesoElimanacion.not());
		btnLimpiar.managedProperty().bind(btnLimpiar.visibleProperty());

		btnGuardar.visibleProperty().bind(procesoElimanacion.not());
		btnGuardar.managedProperty().bind(btnGuardar.visibleProperty());

		// CAMBIO AQUÍ: El botón de eliminar requiere que NO esté en proceso de eliminación Y que haya un cliente cargado
		btnEliminar.visibleProperty().bind(procesoElimanacion.not().and(clienteActual.isNotNull()));
		btnEliminar.managedProperty().bind(btnEliminar.visibleProperty());

		btnConfirmar.visibleProperty().bind(procesoElimanacion);
		btnConfirmar.managedProperty().bind(btnConfirmar.visibleProperty());

		btnCancelar.visibleProperty().bind(procesoElimanacion);
		btnCancelar.managedProperty().bind(btnCancelar.visibleProperty());
	}

	public void setControladorPrincipal(PrincipalController principal) {
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
				if (!txtCodigoPostal.getText().equals("")) {
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
				lblAviso.setText("Cliente añadido correctamente");
			} else {
				clienteDao.setApellidos(txtApellidos.getText());
				clienteDao.setTelefono(txtTelefono.getText());
				clienteDao.setCif(txtCIF.getText());
				clienteDao.setDireccion(txtDireccion.getText());
				clienteDao.setEmail(txtEmail.getText());
				clienteDao.setNotasAdicionales(txtNotasAdicionales.getText());

				clienteHBNT.actualizarEntidad(clienteDao);
				lblAviso.setText("Cliente actualizado correctamente");
			}

			controladorPrincipal.recibirDatosCliente(clienteDao);
		} catch (ProyectoClothoException e) {
			lblAviso.setText("Error al guardar el cliente nuevo");
			e.printStackTrace();
		}

		Stage stage = (Stage) txtNombre.getScene().getWindow();
		stage.close();
	}

	@FXML
	private void limpiarFormulario() {
		lblAviso.setText("");
		sincronizandoCliente = true;

		this.txtNombre.clear();
		this.txtApellidos.clear();
		this.txtCIF.clear();
		this.txtTelefono.clear();
		this.txtCodigoPostal.clear();
		this.txtDireccion.clear();
		this.txtEmail.clear();
		this.txtNotasAdicionales.clear();

		if (cmbBusquedaNombreCliente != null) {
			cmbBusquedaNombreCliente.setValue(null);
			cmbBusquedaNombreCliente.getEditor().clear();
		}

		if (cmbBusquedaTelefonoCliente != null) {
			cmbBusquedaTelefonoCliente.setValue(null);
			cmbBusquedaTelefonoCliente.getEditor().clear();
		}

		this.clienteActual.set(null);

		sincronizandoCliente = false;
	}

	@FXML
	private void eliminarCliente() {
		if (this.clienteActual.get() == null) {
			lblAviso.setText("Debes tener un cliente selecionado para eliminarle");
			return;
		}
		procesoElimanacion.set(true);
	}

	@FXML
	private void cancelarEliminacion() {
		lblAviso.setText("");
		procesoElimanacion.set(false);
	}

	@FXML
	private void confirmarEliminacion() {
		if (this.clienteActual.get() != null) {
			String nombreBorrado = this.clienteActual.get().getNombreCompleto();
			try {
				if ((clienteDao.contarTicketsPorCliente(clienteActual.get().getIdentificador()) > 0)) {
					lblAviso.setText("Rechazado: No se puede eliminar a " + nombreBorrado
							+ " porque tiene tickets asociados o hubo un error.");
				} else {
					clienteDao.borrarEntidadPorClave(this.clienteActual.get().getIdentificador());

					clientesObs.remove(this.clienteActual.get());
					limpiarFormulario();
					lblAviso.setText("Cliente " + nombreBorrado + " eliminado correctamente.");
				}
			} catch (Exception e) {
				lblAviso.setText("Rechazado: No se puede eliminar a " + nombreBorrado
						+ " porque tiene tickets asociados o hubo un error.");
			}
		}
		procesoElimanacion.set(false);
	}

	private void cargarDatosCliente(Cliente clienteSeleccionado) {
		if (sincronizandoCliente) {
			return;
		}

		sincronizandoCliente = true;
		
		// CAMBIO: Usamos .set() para actualizar el cliente actual
		this.clienteActual.set(clienteSeleccionado);

		if (clienteSeleccionado != null) {
			cmbBusquedaNombreCliente.setValue(clienteSeleccionado);
			cmbBusquedaTelefonoCliente.setValue(clienteSeleccionado);

			txtNombre.setText(clienteSeleccionado.getNombre() != null ? clienteSeleccionado.getNombre() : "");
			txtApellidos.setText(clienteSeleccionado.getApellidos() != null ? clienteSeleccionado.getApellidos() : "");
			txtTelefono.setText(clienteSeleccionado.getTelefono() != null ? clienteSeleccionado.getTelefono() : "");
			txtCIF.setText(clienteSeleccionado.getCif() != null ? clienteSeleccionado.getCif() : "");
			txtEmail.setText(clienteSeleccionado.getEmail() != null ? clienteSeleccionado.getEmail() : "");
			txtDireccion.setText(clienteSeleccionado.getDireccion() != null ? clienteSeleccionado.getDireccion() : "");
			txtNotasAdicionales.setText(
					clienteSeleccionado.getNotasAdicionales() != null ? clienteSeleccionado.getNotasAdicionales() : "");

			if (clienteSeleccionado.getCodigoPostal() > 0) {
				txtCodigoPostal.setText(String.valueOf(clienteSeleccionado.getCodigoPostal()));
			} else {
				txtCodigoPostal.clear();
			}
		} else {
			limpiarFormulario();
		}

		sincronizandoCliente = false;
	}
}