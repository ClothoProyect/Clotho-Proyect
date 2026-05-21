package org.openjfx.clotho.proy;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.UnaryOperator;

import org.hibernate.exception.ConstraintViolationException;
import org.openjfx.clotho.proy.dao.ClienteDAO;
import org.openjfx.clotho.proy.dao.hbnt.ClienteDaoHBNT;
import org.openjfx.clotho.proy.dao.hbnt.DetalleDaoHBNT;
import org.openjfx.clotho.proy.dao.hbnt.PedidoDaoHBNT;
import org.openjfx.clotho.proy.dao.hbnt.ServicioDaoHBNT;
import org.openjfx.clotho.proy.documentos.TicketPrinterService;
import org.openjfx.clotho.proy.exception.ProyectoClothoException;
import org.openjfx.clotho.proy.vo.Cliente;
import org.openjfx.clotho.proy.vo.Detalle;
import org.openjfx.clotho.proy.vo.Pedido;
import org.openjfx.clotho.proy.vo.Servicio;
import org.openjfx.clotho.proy.vo.enumerate.EstadoPedido;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;

public class PrincipalController {

	private Integer ultimaColumnaSeleccionada = 0;
	private ClienteDAO clienteDao = new ClienteDaoHBNT();
	private ObservableList<Cliente> clientesObs;
	private List<Detalle> listaDetalles = new ArrayList<Detalle>();
	private ObjectProperty<Cliente> clienteActual = new SimpleObjectProperty<>();
	private ObjectProperty<EstadoPedido> estado = new SimpleObjectProperty<>(EstadoPedido.Sin_Pagar);
	private ServicioDaoHBNT servicioHBNT = new ServicioDaoHBNT();
	private ObservableList<Servicio> serviciosObs;
	private boolean sincronizandoCliente = false;

	@FXML
	private CheckBox chkTarjeta;

	@FXML
	private Button btnImpresionCompleta;

	@FXML
	private Button btnImpresionSimple;

	@FXML
	private ComboBox<Servicio> cmbNombreServicio;

	@FXML
	private TextField txtPrecioServicio;

	@FXML
	private TextField txtDescripcionServicio;

	@FXML
	private TextField txtDescuento;

	@FXML
	private TableView<Detalle> tablaDetalles;

	@FXML
	private ComboBox<Cliente> cmbCliente;

	@FXML
	private ComboBox<Cliente> cmbTelefono;

	@FXML
	private ComboBox<Cliente> cmbCIF;

	@FXML
	private DatePicker fechaPedido;

	@FXML
	private GridPane gridResumenSemana;

	@FXML
	private Label txtMesActual;

	@FXML
	private Label txtTicketMesActual;

	@FXML
	private Label txtMediaArreglos;

	@FXML
	private Label txtTicketsCantidad;

	@FXML
	private Label txtIngresosMes;

	@FXML
	private Label primerDia, segundoDia, tercerDia, cuartoDia, quintoDia, sextoDia, septimoDia;

	@FXML
	private Label primerDiaFecha, segundoDiaFecha, tercerDiaFecha, cuartoDiaFecha, quintoDiaFecha, sextoDiaFecha,
			septimoDiaFecha;

	@FXML
	private Label primerDiaPedidos, segundoDiaPedidos, tercerDiaPedidos, cuartoDiaPedidos, quintoDiaPedidos,
			sextoDiaPedidos, septimoDiaPedidos;

	@FXML
	private Label primerDiaPrendas, segundoDiaPrendas, tercerDiaPrendas, cuartoDiaPrendas, quintoDiaPrendas,
			sextoDiaPrendas, septimoDiaPrendas;

	@FXML
	private Label primerDiaIngresos, segundoDiaIngresos, tercerDiaIngresos, cuartoDiaIngresos, quintoDiaIngresos,
			sextoDiaIngresos, septimoDiaIngresos;

	@FXML
	public void initialize() {
		String mes = LocalDate.now().getMonth().getDisplayName(TextStyle.FULL, new Locale("es", "ES"));
		mes = mes.substring(0, 1).toUpperCase() + mes.substring(1);
		txtMesActual.setText("Ingresos de " + mes);
		txtTicketMesActual.setText("Tickets de " + mes);
		UnaryOperator<TextFormatter.Change> filtroMayusculas = cambio -> {
			cambio.setText(cambio.getText().toUpperCase());
			return cambio;
		};
		txtDescripcionServicio.setTextFormatter(new TextFormatter<>(filtroMayusculas));
		cmbNombreServicio.getEditor().setTextFormatter(new TextFormatter<>(filtroMayusculas));

		cargaListaServicios();
		actualizaResumenSemanal();
		actualizarResumenMensual();
		cambiarColorColumna(2);
		cargaListaClientes();

		TableColumn<Detalle, Float> colPrecio = (TableColumn<Detalle, Float>) tablaDetalles.getColumns().get(1);
		colPrecio.setCellFactory(columna -> new TableCell<Detalle, Float>() {
			@Override
			protected void updateItem(Float precio, boolean empty) {
				super.updateItem(precio, empty);
				if (empty || precio == null) {
					setText(null);
				} else {
					setText(String.format("%.2f €", precio));
				}
			}
		});
	}

	private void procesarImpresionFisica(boolean esCompleto) {
		// 1. Validamos que haya datos listos para imprimir
		if (clienteActual.get() == null || listaDetalles.isEmpty() || estado.get() == null) {
			System.err.println("Error: Seleccione cliente, estado y servicios.");
			return;
		}

		try {
			// 2. Construimos un "Pedido temporal" con la info de la pantalla para dárselo a
			// la impresora
			PedidoDaoHBNT pedidoDAO = new PedidoDaoHBNT();
			Pedido pedidoAImprimir = new Pedido();

			pedidoAImprimir.setCliente(clienteActual.get());

			// Si por algún motivo la fecha está vacía, usamos la de hoy para que no falle
			// la impresión
			pedidoAImprimir.setFecha(fechaPedido.getValue() != null ? fechaPedido.getValue() : LocalDate.now());

			// Predecimos cuál va a ser su número de ticket calculándolo igual que lo hace
			// procesarPedido()
			pedidoAImprimir.setCodigoPedido(pedidoDAO.obtenerUltimoCodigoPedido() + 1);

			// 3. Imprimimos el ticket pasándole la lista de detalles que está en memoria
			// (en la tabla visual)
			TicketPrinterService printerService = new TicketPrinterService();
			printerService.imprimir(pedidoAImprimir, listaDetalles, esCompleto);

			// 4. ¡Listo! Ahora que ya ha salido el papel, lo guardamos en la base de datos
			// y limpiamos la pantalla
			procesarPedido();

		} catch (Exception e) {
			System.err.println("Error al intentar imprimir el ticket físico.");
			e.printStackTrace();
		}
	}

	private void actualizarResumenMensual() {
		try {
			DetalleDaoHBNT detalleDao = new DetalleDaoHBNT();

			// 1. Obtener la media mensual
			float mediaArreglos = detalleDao.obtenerMediaMensualPorArreglo();
			txtMediaArreglos.setText(String.format(new Locale("es", "ES"), "%.2f €", mediaArreglos));

			// 2. Obtener el total de ingresos mensuales
			float ingresosMensuales = detalleDao.obtenerTotalMensual();
			txtIngresosMes.setText(String.format(new Locale("es", "ES"), "%.2f €", ingresosMensuales));

			// 3. Obtener el total de tickets del mes (le pasamos la fecha actual como pedía
			// tu método)
			int cantidadTickets = detalleDao.obtenerCantidadMensualTickets(LocalDate.now());
			txtTicketsCantidad.setText(String.valueOf(cantidadTickets));

		} catch (ProyectoClothoException e) {
			System.err.println("Error al actualizar las estadísticas mensuales.");
			e.printStackTrace();
		}
	}

	private void sincronizarSeleccion(Cliente clienteSeleccionado) {
		if (sincronizandoCliente)
			return;

		sincronizandoCliente = true;

		clienteActual.set(clienteSeleccionado);
		cmbCliente.setValue(clienteSeleccionado);
		cmbTelefono.setValue(clienteSeleccionado);
		cmbCIF.setValue(clienteSeleccionado);

		sincronizandoCliente = false;
	}

	private void cargaListaClientes() {
		try {
			clientesObs = FXCollections.observableArrayList(clienteDao.obtenerListaTodasEntidades());
		} catch (ProyectoClothoException e) {
			e.printStackTrace();
		}

		cmbCliente.setItems(clientesObs);
		cmbTelefono.setItems(clientesObs);
		cmbCIF.setItems(clientesObs);

		cmbCliente.setConverter(new StringConverter<Cliente>() {
			@Override
			public String toString(Cliente cliente) {
				return cliente == null ? "" : cliente.getNombreCompleto();
			}

			@Override
			public Cliente fromString(String string) {
				if (string == null || string.isEmpty())
					return null;
				return clientesObs.stream()
						.filter(c -> c.getNombreCompleto().toLowerCase().contains(string.toLowerCase())).findFirst()
						.orElse(null);
			}
		});

		cmbTelefono.setConverter(new StringConverter<Cliente>() {
			@Override
			public String toString(Cliente cliente) {
				return (cliente == null || cliente.getTelefono() == null) ? "" : cliente.getTelefono();
			}

			@Override
			public Cliente fromString(String string) {
				if (string == null || string.isEmpty())
					return null;
				return clientesObs.stream().filter(c -> c.getTelefono() != null && c.getTelefono().contains(string))
						.findFirst().orElse(null);
			}
		});

		cmbCIF.setConverter(new StringConverter<Cliente>() {
			@Override
			public String toString(Cliente cliente) {
				return (cliente == null || cliente.getCif() == null) ? "" : cliente.getCif();
			}

			@Override
			public Cliente fromString(String string) {
				if (string == null || string.isEmpty())
					return null;
				return clientesObs.stream()
						.filter(c -> c.getCif() != null && c.getCif().toLowerCase().contains(string.toLowerCase()))
						.findFirst().orElse(null);
			}
		});

		cmbCliente.valueProperty().addListener((obs, oldVal, newVal) -> sincronizarSeleccion(newVal));
		cmbTelefono.valueProperty().addListener((obs, oldVal, newVal) -> sincronizarSeleccion(newVal));
		cmbCIF.valueProperty().addListener((obs, oldVal, newVal) -> sincronizarSeleccion(newVal));

		// Reglas para el botón de Impresión Completa
		btnImpresionCompleta.visibleProperty().bind(
				clienteActual.isNotNull().and(estado.isNotNull()).and(Bindings.isNotEmpty(tablaDetalles.getItems())));
		btnImpresionCompleta.managedProperty().bind(btnImpresionCompleta.visibleProperty());

		// Reglas para el botón de Impresión Simple
		btnImpresionSimple.visibleProperty().bind(
				clienteActual.isNotNull().and(estado.isNotNull()).and(Bindings.isNotEmpty(tablaDetalles.getItems())));
		btnImpresionSimple.managedProperty().bind(btnImpresionSimple.visibleProperty());

		// Reglas para el botón de Impresión Simple
		btnImpresionSimple.visibleProperty().bind(
				clienteActual.isNotNull().and(estado.isNotNull()).and(Bindings.isNotEmpty(tablaDetalles.getItems())));
		btnImpresionSimple.managedProperty().bind(btnImpresionSimple.visibleProperty());

		// Reglas para el checkbox de tarjeta
		chkTarjeta.visibleProperty().bind(estado.isEqualTo(EstadoPedido.Pagado));
		chkTarjeta.managedProperty().bind(chkTarjeta.visibleProperty());
		estado.addListener((obs, oldVal, newVal) -> {
			if (newVal != EstadoPedido.Pagado) {
				chkTarjeta.setSelected(false);
			}
		});
	}

	private void cargaListaServicios() {
		try {
			serviciosObs = FXCollections.observableArrayList(servicioHBNT.obtenerListaTodasEntidades());
			cmbNombreServicio.setItems(serviciosObs);

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
					return serviciosObs.stream()
							.filter(s -> s.getNombre().toLowerCase().startsWith(string.toLowerCase())).findFirst()
							.orElseGet(() -> {
								Servicio nuevoSrv = new Servicio();
								nuevoSrv.setNombre(string);
								return nuevoSrv;
							});
				}
			});

			cmbNombreServicio.valueProperty().addListener((obs, oldVal, newVal) -> {
				if (newVal != null) {
					txtPrecioServicio.setText(String.format(Locale.US, "%.2f", newVal.getPrecioEstandar()));
				} else {
					txtPrecioServicio.clear();
				}
			});
		} catch (ProyectoClothoException e) {
			e.printStackTrace();
		}
	}

	@FXML
	public void ImprimirTicketCompleto() {
		procesarPedido();
	}

	@FXML
	public void ImprimirTicketSimple() {
		procesarPedido();
	}

	@FXML
	private void guardarServicio() {
		try {
			String nombreLimpio = "";
			if (cmbNombreServicio.getValue() != null && cmbNombreServicio.getValue().getNombre() != null) {
				nombreLimpio = cmbNombreServicio.getValue().getNombre().trim();
			} else if (cmbNombreServicio.getEditor().getText() != null) {
				nombreLimpio = cmbNombreServicio.getEditor().getText().trim();
			}

			if (nombreLimpio.isEmpty() || nombreLimpio.equalsIgnoreCase("Servicio...")) {
				System.err.println("Campos vacíos, El nombre del servicio no puede estar vacío.");
				return;
			}

			Servicio servicio = new Servicio();
			servicio.setNombre(nombreLimpio);

			Servicio servicioExistente = servicioHBNT.obtenerEntidadPorNombre(servicio);
			if (servicioExistente == null) {
				servicio.setIdentificador(servicioHBNT.obtenerUltimoIdentificador() + 1);
				servicioHBNT.crearEntidad(servicio);
				servicioExistente = servicio;
			}

			Detalle nuevoDetalle = new Detalle();
			nuevoDetalle.setServicio(servicioExistente);

			// 1. Extraemos el precio original
			float precioParsed = Float.parseFloat(txtPrecioServicio.getText().trim());

			// 2. Comprobamos si hay un descuento escrito
			String textoDescuento = txtDescuento.getText().trim();
			if (!textoDescuento.isEmpty()) {
				try {
					float porcentajeDescuento = Float.parseFloat(textoDescuento);
					// Calculamos la rebaja y se la restamos al precio original
					float rebaja = precioParsed * (porcentajeDescuento / 100f);
					precioParsed = precioParsed - rebaja;
				} catch (NumberFormatException e) {
					System.err.println("El valor del descuento no es válido. Se aplicará el precio normal.");
				}
			}

			// 3. Guardamos el precio final (con o sin descuento) en el detalle
			nuevoDetalle.setPrecioUnitario(precioParsed);
			nuevoDetalle.setDescripcion(txtDescripcionServicio.getText().trim());

			// Lo añadimos directamente a la tabla
			this.listaDetalles.add(nuevoDetalle);
			tablaDetalles.getItems().add(nuevoDetalle);

			// LIMPIAMOS LOS CAMPOS para poder añadir otro arreglo al instante
			cmbNombreServicio.setValue(null);
			cmbNombreServicio.getEditor().clear();
			txtPrecioServicio.clear();
			txtDescripcionServicio.clear();
			txtDescuento.clear();

		} catch (ConstraintViolationException e) {
			System.err.println("Servicio Duplicado en BD.");
		} catch (NumberFormatException e) {
			System.err.println("Error de Precio, Por favor, introduce un número válido.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@FXML
	private void abrirVentanaClientes() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("cliente.fxml"));
			Parent root = loader.load();
			ClientesController controllerHijo = loader.getController();
			controllerHijo.setControladorPrincipal(this);

			Image icono = new Image(getClass().getResourceAsStream("/imagenes/Clotho.png"));

			Stage stage = new Stage();

			stage.setOnHidden(windowEvent -> {
				refrescarListaClientes();
			});

			stage.setTitle("Gestion de clientes");
			stage.getIcons().add(icono);
			stage.initModality(Modality.APPLICATION_MODAL);
			stage.setScene(new Scene(root));
			stage.show();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@FXML
	private void abrirVentanaServicios() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("servicios.fxml"));
			Parent root = loader.load();
			ServiciosController controller = loader.getController();
			controller.setControladorPrincipal(this);

			Image icono = new Image(getClass().getResourceAsStream("/imagenes/Clotho.png"));

			Stage stage = new Stage();
			stage.setTitle("Gestion de servicios");
			stage.getIcons().add(icono);
			stage.initModality(Modality.APPLICATION_MODAL);
			stage.setScene(new Scene(root));
			// Renovar la lista de Servicios en la vista de principal al cerrar la ventana
			stage.setOnHidden(windowEvent -> {
				cargaListaServicios();
			});
			stage.show();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@FXML
	private void abrirVentanaPedidos() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("pedidos.fxml"));
			Parent root = loader.load();

			PedidosController controller = loader.getController();

			Image icono = new Image(getClass().getResourceAsStream("/imagenes/Clotho.png"));

			Stage stage = new Stage();
			stage.setTitle("Búsqueda de Tickets");
			stage.getIcons().add(icono);
			stage.initModality(Modality.APPLICATION_MODAL);
			stage.setScene(new Scene(root));
			stage.show();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void recibirDatosCliente(Cliente cliente) {
		this.clienteActual.set(cliente);
		this.cmbCliente.setValue(cliente);
		cargaListaClientes();
	}

	@FXML
	private void handleCancelar() {
		this.clienteActual.set(null);
		this.cmbCliente.setValue(null);

		if (this.cmbTelefono != null) {
			this.cmbTelefono.setValue(null);
		}
		if (this.cmbCIF != null) {
			this.cmbCIF.setValue(null);
		}

		cmbNombreServicio.setValue(null);
		cmbNombreServicio.getEditor().clear();
		txtPrecioServicio.clear();
		txtDescripcionServicio.clear();
		txtDescuento.clear();
		listaDetalles.clear();
		estado.set(EstadoPedido.Sin_Pagar);
		chkTarjeta.setSelected(false);
		fechaPedido.setValue(null);
		tablaDetalles.getItems().clear();
		actualizaResumenSemanal();
		cambiarColorColumna(2);
	}

	@FXML
	private void handleAPagar() {
		estado.set(EstadoPedido.Sin_Pagar);
	}

	@FXML
	private void handlePagado() {
		estado.set(EstadoPedido.Pagado);
	}

	@FXML
	public void procesarPedido() {
		if (clienteActual.get() == null || listaDetalles.isEmpty() || estado.get() == null) {
			System.err.println("Error: Seleccione cliente, estado y servicios.");
			return;
		}
		try {
			PedidoDaoHBNT pedidoDAO = new PedidoDaoHBNT();
			DetalleDaoHBNT detalleDao = new DetalleDaoHBNT();

			Pedido pedido = new Pedido();
			pedido.setCliente(clienteActual.get());
			pedido.setEstado(this.estado.get());
			pedido.setPagoConTarjeta(chkTarjeta.isSelected());
			pedido.setFecha(fechaPedido.getValue());

			pedido.setIdentificador(pedidoDAO.obtenerUltimoIdentificador() + 1);
			pedido.setCodigoPedido(pedidoDAO.obtenerUltimoCodigoPedido() + 1);

			float precio = 0f;
			for (Detalle detalleActual : listaDetalles) {
				precio += detalleActual.getPrecioUnitario();
			}

			pedido.setPrecio(precio);
			pedidoDAO.crearEntidad(pedido);

			int idDetalleContador = detalleDao.obtenerUltimoIdentificador();
			for (Detalle detalle : listaDetalles) {
				idDetalleContador++;
				detalle.setIdentificador(idDetalleContador);
				detalle.setPedido(pedido);
				detalleDao.crearEntidad(detalle);
			}

			handleCancelar();
		} catch (Exception e) {
			e.printStackTrace();
		}
		actualizaResumenSemanal();
		actualizarResumenMensual();
	}

	public void recibirDatosDetalle(Detalle detalle) {
		this.listaDetalles.add(detalle);
		tablaDetalles.getItems().add(detalle);
	}

	@FXML
	private void manejarClickResumen(MouseEvent event) {
		if (event.getClickCount() == 1) {
			Label labelClicado = (Label) event.getSource();
			Integer columnaClicada = GridPane.getColumnIndex(labelClicado);

			cambiarColorColumna(columnaClicada);

			LocalDate fechaSeleccionada = (LocalDate) labelClicado.getUserData();
			if (fechaSeleccionada != null) {
				fechaPedido.setValue(fechaSeleccionada);
			}
			String idClicado = labelClicado.getId();
			String textoFecha = "";

			switch (idClicado) {
			case "primerDia":
				textoFecha = primerDiaFecha.getText();
				break;
			case "segundoDia":
				textoFecha = segundoDiaFecha.getText();
				break;
			case "tercerDia":
				textoFecha = tercerDiaFecha.getText();
				break;
			case "cuartoDia":
				textoFecha = cuartoDiaFecha.getText();
				break;
			case "quintoDia":
				textoFecha = quintoDiaFecha.getText();
				break;
			case "sextoDia":
				textoFecha = sextoDiaFecha.getText();
				break;
			case "septimoDia":
				textoFecha = septimoDiaFecha.getText();
				break;
			}
			fechaSeleccionada = LocalDate.parse(textoFecha);
			fechaPedido.setValue(fechaSeleccionada);
		}
	}

	private void cambiarColorColumna(int columnaSelecionada) {
		if (columnaSelecionada == ultimaColumnaSeleccionada) {
			return;
		}
		for (Node nodo : gridResumenSemana.getChildren()) {
			Integer colNodo = GridPane.getColumnIndex(nodo);

			if (colNodo != null && colNodo > 0) {
				if (colNodo.equals(ultimaColumnaSeleccionada)) {
					nodo.getStyleClass().remove("resumen-selecionado");
				} else if (colNodo.equals(columnaSelecionada)) {
					nodo.getStyleClass().add("resumen-selecionado");
				}
			}
		}
		ultimaColumnaSeleccionada = columnaSelecionada;
	}

	public void actualizaResumenSemanal() {
		Label[] labelsDias = { primerDia, segundoDia, tercerDia, cuartoDia, quintoDia, sextoDia, septimoDia };
		Label[] labelsFechas = { primerDiaFecha, segundoDiaFecha, tercerDiaFecha, cuartoDiaFecha, quintoDiaFecha,
				sextoDiaFecha, septimoDiaFecha };
		Label[] labelsPedidos = { primerDiaPedidos, segundoDiaPedidos, tercerDiaPedidos, cuartoDiaPedidos,
				quintoDiaPedidos, sextoDiaPedidos, septimoDiaPedidos };
		Label[] labelsPrendas = { primerDiaPrendas, segundoDiaPrendas, tercerDiaPrendas, cuartoDiaPrendas,
				quintoDiaPrendas, sextoDiaPrendas, septimoDiaPrendas };
		Label[] labelsIngresos = { primerDiaIngresos, segundoDiaIngresos, tercerDiaIngresos, cuartoDiaIngresos,
				quintoDiaIngresos, sextoDiaIngresos, septimoDiaIngresos };

		DetalleDaoHBNT detalleDao = new DetalleDaoHBNT();

		LocalDate fechaBase = LocalDate.now();
		if (fechaBase.getDayOfWeek() == DayOfWeek.SATURDAY) {
			fechaBase = fechaBase.plusDays(2);
		}
		this.fechaPedido.setValue(LocalDate.now().plusDays(1));

		int sunday = 0;

		for (int i = 0; i < labelsDias.length; i++) {
			LocalDate fechaCalculada = fechaBase.plusDays(i + sunday);
			if (fechaCalculada.getDayOfWeek() == DayOfWeek.SATURDAY) {
				sunday = 1;
			}

			String nombreDia = fechaCalculada.getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("es", "ES"));
			String cantidadPedidos = "";
			String cantidadPrendas = "";
			String ingresosPedidos = "0,00 €";
			try {
				cantidadPedidos = String.valueOf(detalleDao.obtenerPedidosPorDia(fechaCalculada));
				ingresosPedidos = String.format(new Locale("es", "ES"), "%.2f €",
						detalleDao.obtenerIngresosPorDia(fechaCalculada));
				cantidadPrendas = String.valueOf(detalleDao.obtenerPrendasPorDia(fechaCalculada));
			} catch (ProyectoClothoException e) {
				e.printStackTrace();
			}

			labelsDias[i].setText(nombreDia.toUpperCase());
			labelsFechas[i].setText(fechaCalculada.toString());
			labelsPedidos[i].setText(cantidadPedidos);
			labelsPrendas[i].setText(cantidadPrendas);
			labelsIngresos[i].setText(ingresosPedidos);
		}
	}

	public void refrescarListaClientes() {
		try {
			List<Cliente> listaActualizada = clienteDao.obtenerListaTodasEntidades();

			if (clientesObs != null) {
				clientesObs.clear();
				clientesObs.addAll(listaActualizada);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}