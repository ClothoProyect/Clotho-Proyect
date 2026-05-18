package org.openjfx.clotho.proy;

import java.util.function.UnaryOperator;

import org.hibernate.exception.ConstraintViolationException;
import org.openjfx.clotho.proy.dao.hbnt.ServicioDaoHBNT;
import org.openjfx.clotho.proy.exception.ProyectoClothoException;
import org.openjfx.clotho.proy.vo.Detalle;
import org.openjfx.clotho.proy.vo.Servicio;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.stage.Stage;
import javafx.util.StringConverter;

public class ServiciosController {

	// 1. Cambiamos TextField por ComboBox
	@FXML
	private ComboBox<Servicio> cmbNombreServicio;
	@FXML
	private TextField txtPrecioServicio;
	@FXML
	private TextField txtDescripcionServicio;
	@FXML
	private TextField txtDescuento;
	@FXML
	private Label lblAnuncio;
	
	private PrincipalController controladorPrincipal;
	private ServicioDaoHBNT servicioHBNT = new ServicioDaoHBNT();
	private ObservableList<Servicio> serviciosObs;

	public void setControladorPrincipal(PrincipalController principal) {
		this.controladorPrincipal = principal;
	}

	@FXML
	public void initialize() {
		// Conversor de mayusculas a tiempo real
		UnaryOperator<TextFormatter.Change> filtroMayusculas = cambio -> {
			cambio.setText(cambio.getText().toUpperCase());
			return cambio;
		};

		txtDescripcionServicio.setTextFormatter(new TextFormatter<>(filtroMayusculas));

		cmbNombreServicio.getEditor().setTextFormatter(new TextFormatter<>(filtroMayusculas));

		cargaListaServicios();
	}

	private void cargaListaServicios() {
		try {
			// Cargamos todos los servicios de la BD
			serviciosObs = FXCollections.observableArrayList(servicioHBNT.obtenerListaTodasEntidades());
			cmbNombreServicio.setItems(serviciosObs);

			// Configuramos el conversor para mostrar el nombre y permitir la búsqueda
			cmbNombreServicio.setConverter(new StringConverter<Servicio>() {
				@Override
				public String toString(Servicio servicio) {
					if (servicio == null)
						return "";
					return servicio.getNombre();
				}

				@Override
				public Servicio fromString(String string) {
					if (string == null || string.isEmpty())
						return null;

					// Busca coincidencias en la lista
					return serviciosObs.stream()
							.filter(s -> s.getNombre().toLowerCase().startsWith(string.toLowerCase())).findFirst()
							// Si no existe (es un servicio nuevo que el usuario está tecleando),
							// creamos un objeto Servicio temporal solo con el nombre
							.orElseGet(() -> {
								Servicio nuevoSrv = new Servicio();
								nuevoSrv.setNombre(string);
								return nuevoSrv;
							});
				}
			});

		} catch (ProyectoClothoException e) {
			e.printStackTrace();
		}
	}

	@FXML
	private void guardarServicio() {
		ServicioDaoHBNT servicioHBNT = new ServicioDaoHBNT();
		try {
			// 1. Limpiamos y preparamos el nombre del SERVICIO
			String nombreLimpio = "";

			// Si el usuario seleccionó un servicio de la lista desplegable
			if (cmbNombreServicio.getValue() != null && cmbNombreServicio.getValue().getNombre() != null) {
				nombreLimpio = cmbNombreServicio.getValue().getNombre().trim();
			}
			// Si el usuario escribió un texto libremente
			else if (cmbNombreServicio.getEditor().getText() != null) {
				nombreLimpio = cmbNombreServicio.getEditor().getText().trim();
			}

			// Si está vacío o es igual al texto de ayuda por accidente, cortamos
			if (nombreLimpio.isEmpty() || nombreLimpio.equalsIgnoreCase("Seleccione o escriba un servicio")) {
				System.err.println("Campos vacíos, El nombre del servicio no puede estar vacío.");
				return;
			}

			Servicio servicio = new Servicio();
			servicio.setNombre(nombreLimpio);

			// 2. Buscamos si el SERVICIO ya existe en el catálogo
			Servicio servicioExistente = servicioHBNT.obtenerEntidadPorNombre(servicio);

			if (servicioExistente == null) {
				// Si no existe, lo creamos
				servicio.setIdentificador(servicioHBNT.obtenerUltimoIdentificador() + 1);
				// servicio.setDescripcion(txtDescripcionServicio.getText().trim());

				servicioHBNT.crearEntidad(servicio);
				servicioExistente = servicio;
			}

			// 3. AHORA CREAMOS EL DETALLE DEL PEDIDO
			Detalle nuevoDetalle = new Detalle();
			nuevoDetalle.setServicio(servicioExistente);

			// Extraemos el precio
			float precioParsed = Float.parseFloat(txtPrecioServicio.getText().trim());
			nuevoDetalle.setPrecioUnitario(precioParsed);

			// AÑADE ESTA LÍNEA: Guardamos la descripción en el detalle
			nuevoDetalle.setDescripcion(txtDescripcionServicio.getText().trim());

			// 4. Enviamos el DETALLE COMPLETO a la tabla principal
			controladorPrincipal.recibirDatosDetalle(nuevoDetalle);
			
			// Cerramos la ventana emergente
			Stage stage = (Stage) cmbNombreServicio.getScene().getWindow();
			stage.close();

		} catch (ConstraintViolationException e) {
			// Corregido: Ahora usa nombreLimpio en vez del promptText
			System.err.println("Servicio Duplicado, Ya existe un servicio llamado '"
					+ cmbNombreServicio.getEditor().getText().trim() + "' en la base de datos.");
		} catch (NumberFormatException e) {
			System.err.println("Error de Precio, Por favor, introduce un número válido para el precio (ej. 15.50).");
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Error del sistema, Ha ocurrido un error inesperado.");
		}
	}

	@FXML
	private void finalizarServicio() {
		Stage stage = (Stage) cmbNombreServicio.getScene().getWindow();
		stage.close();
	}
}