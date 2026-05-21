package org.openjfx.clotho.proy;

import java.util.function.UnaryOperator;

import org.openjfx.clotho.proy.dao.hbnt.ServicioDaoHBNT;
import org.openjfx.clotho.proy.exception.ProyectoClothoException;
import org.openjfx.clotho.proy.vo.Servicio;

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
import javafx.util.StringConverter;

public class ServiciosController {

	private ServicioDaoHBNT servicioDao = new ServicioDaoHBNT();
	private ObservableList<Servicio> serviciosObs;
	// Propiedad observable para pasarle la informacion a los TextField
	private ObjectProperty<Servicio> servicioActual = new SimpleObjectProperty<>(null);
	private boolean sincronizandoServicio = false;
	private PrincipalController controladorPrincipal;

	@FXML
	private Label lblAviso;
	@FXML
	private TextField txtNombre;
	@FXML
	private TextField txtPrecioEstandar;
	// Filtro de busque de servicios actuales
	@FXML
	private ComboBox<Servicio> cmbBusquedaNombreServicio;

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

	private BooleanProperty procesoElimanacion = new SimpleBooleanProperty(false);

	@FXML
	public void initialize() {
		// Fijador de mayusculas en los TextField
		UnaryOperator<TextFormatter.Change> filtroMayusculas = cambio -> {
			cambio.setText(cambio.getText().toUpperCase());
			return cambio;
		};
		txtNombre.setTextFormatter(new TextFormatter<>(filtroMayusculas));

		try {
			serviciosObs = FXCollections.observableArrayList(servicioDao.obtenerListaTodasEntidades());
		} catch (ProyectoClothoException e) {
			e.printStackTrace();
		}

		cmbBusquedaNombreServicio.setItems(serviciosObs);

		// Convertidor para gestionar las busquedas de autocompletado en el ComboBox
		cmbBusquedaNombreServicio.setConverter(new StringConverter<Servicio>() {
			@Override
			public String toString(Servicio servicio) {
				return servicio == null ? "" : servicio.getNombre();
			}

			@Override
			public Servicio fromString(String filtro) {
				if (filtro == null || filtro.isEmpty()) {
					return null;
				}
				return serviciosObs.stream().filter(s -> s.getNombre().toUpperCase().contains(filtro.toUpperCase()))
						.findFirst().orElse(null);
			}
		});

		// Listener para rellenar el formulario al seleccionar un servicio
		cmbBusquedaNombreServicio.valueProperty().addListener((obs, oldVal, newVal) -> cargarDatosServicio(newVal));

		// Vinculacion para botones dependiendo de la seleccion de un servicio
		btnLimpiar.visibleProperty().bind(procesoElimanacion.not());
		btnLimpiar.managedProperty().bind(btnLimpiar.visibleProperty());

		btnGuardar.visibleProperty().bind(procesoElimanacion.not());
		btnGuardar.managedProperty().bind(btnGuardar.visibleProperty());

		// El botón eliminar solo se muestra si NO se está borrando Y hay un servicio
		// seleccionado
		btnEliminar.visibleProperty().bind(procesoElimanacion.not().and(servicioActual.isNotNull()));
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
	private void guardarServicio() {
		if (txtNombre.getText().trim().isEmpty()) {
			lblAviso.setText("El nombre del servicio no puede estar vacío.");
			return;
		}

		float precioParsed = 0f;
		try {
			precioParsed = Float.parseFloat(txtPrecioEstandar.getText().trim());
		} catch (NumberFormatException e) {
			lblAviso.setText("Introduce un número válido para el precio (formato con un \".\").");
			return;
		}

		try {
			if (this.servicioActual.get() == null) {
				Servicio nuevoServicio = new Servicio();
				nuevoServicio.setNombre(txtNombre.getText().trim());

				// Validación de nombre duplicado
				if (servicioDao.obtenerEntidadPorNombre(nuevoServicio) != null) {
					lblAviso.setText("Error: Ya existe un servicio con ese nombre.");
					return;
				}

				nuevoServicio.setIdentificador(servicioDao.obtenerUltimoIdentificador() + 1);
				nuevoServicio.setPrecioEstandar(precioParsed);
				nuevoServicio.setActivo(true);

				servicioDao.crearEntidad(nuevoServicio);
				serviciosObs.add(nuevoServicio);
				limpiarFormulario();
				lblAviso.setText("Servicio creado correctamente.");
			} else {
				Servicio srvEditado = this.servicioActual.get();
				srvEditado.setNombre(txtNombre.getText().trim());
				srvEditado.setPrecioEstandar(precioParsed);

				servicioDao.actualizarEntidad(srvEditado);
				limpiarFormulario();
				lblAviso.setText("Servicio actualizado correctamente.");
			}

		} catch (ProyectoClothoException e) {
			lblAviso.setText("Error interno al procesar el servicio.");
			e.printStackTrace();
		}
	}

	@FXML
	private void limpiarFormulario() {
		lblAviso.setText("");
		sincronizandoServicio = true;

		this.txtNombre.clear();
		this.txtPrecioEstandar.clear();

		if (cmbBusquedaNombreServicio != null) {
			cmbBusquedaNombreServicio.setValue(null);
			cmbBusquedaNombreServicio.getEditor().clear();
		}

		this.servicioActual.set(null);
		sincronizandoServicio = false;
	}

	@FXML
	private void eliminarServicio() {
		if (this.servicioActual.get() == null) {
			lblAviso.setText("Debes seleccionar un servicio para eliminarlo.");
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
		if (this.servicioActual.get() != null) {
			Servicio srvABorrar = this.servicioActual.get();
			String nombreBorrado = srvABorrar.getNombre();
			try {
				if (servicioDao.contarDetallesPorServicio(srvABorrar.getIdentificador()) > 0) {
					// Si tiene registros en T_PEDIDO, hacemos directamente un borrado lógico
					srvABorrar.setActivo(false);
					servicioDao.actualizarEntidad(srvABorrar);
					limpiarFormulario();
					lblAviso.setText("Aviso: El servicio tiene históricos. Se ha marcado como INACTIVO.");
				} else {
					// Si nadie lo ha usado nunca, se borra de T_SERVICIO
					servicioDao.borrarEntidadPorClave(srvABorrar.getIdentificador());
					serviciosObs.remove(srvABorrar);
					limpiarFormulario();
					lblAviso.setText("Servicio '" + nombreBorrado + "' eliminado correctamente.");
				}
			} catch (Exception e) {
				lblAviso.setText("Error crítico al desactivar el servicio.");
				e.printStackTrace();
			}
		}
		procesoElimanacion.set(false);

	}

	private void cargarDatosServicio(Servicio servicioSeleccionado) {
		if (sincronizandoServicio) {
			return;
		}

		sincronizandoServicio = true;
		this.servicioActual.set(servicioSeleccionado);

		if (servicioSeleccionado != null) {
			cmbBusquedaNombreServicio.setValue(servicioSeleccionado);
			txtNombre.setText(servicioSeleccionado.getNombre() != null ? servicioSeleccionado.getNombre() : "");
			txtPrecioEstandar.setText(String.valueOf(servicioSeleccionado.getPrecioEstandar()));
		} else {
			limpiarFormulario();
		}

		sincronizandoServicio = false;
	}
}