package org.openjfx.clotho.proy;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.openjfx.clotho.proy.dao.ClienteDAO;
import org.openjfx.clotho.proy.dao.hbnt.ClienteDaoHBNT;
import org.openjfx.clotho.proy.dao.hbnt.DetalleDaoHBNT;
import org.openjfx.clotho.proy.dao.hbnt.FacturaDaoHBNT;
import org.openjfx.clotho.proy.dao.hbnt.PedidoDaoHBNT;
import org.openjfx.clotho.proy.documentos.PdfGeneratorService;
import org.openjfx.clotho.proy.documentos.TicketPrinterService;
import org.openjfx.clotho.proy.gestor.GestorFicheroProperties;
import org.openjfx.clotho.proy.vo.Cliente;
import org.openjfx.clotho.proy.vo.Detalle;
import org.openjfx.clotho.proy.vo.Factura;
import org.openjfx.clotho.proy.vo.Pedido;
import org.openjfx.clotho.proy.vo.enumerate.EstadoPedido;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;

public class PedidosController {

	private PedidoDaoHBNT pedidoDao = new PedidoDaoHBNT();
	private DetalleDaoHBNT detalleDao = new DetalleDaoHBNT();
	private ObservableList<Pedido> listaPedidosObs = FXCollections.observableArrayList();
	private ObservableList<Detalle> listaDetallesObs = FXCollections.observableArrayList();
	private ClienteDAO clienteDao = new ClienteDaoHBNT();
	private ObservableList<Cliente> clientesObs = FXCollections.observableArrayList();
	private boolean sincronizandoCliente = false;

	// ComboBox para buscar registros de la clase Cliente
	@FXML
	private ComboBox<Cliente> cmbFiltroCliente;
	@FXML
	private ComboBox<Cliente> cmbFiltroTelefono;

	// Filtros de busqueda
	@FXML
	private TextField txtFiltroNumTicket;
	@FXML
	private TextField txtFiltroCliente;
	@FXML
	private TextField txtFiltroAnio;
	@FXML
	private DatePicker dpFiltroDesde;
	@FXML
	private DatePicker dpFiltroHasta;

	// Tabla de Tickets
	@FXML
	private TableView<Pedido> tablaTickets;
	@FXML
	private TableColumn<Pedido, Integer> colTicketId;
	@FXML
	private TableColumn<Pedido, String> colTicketCliente;
	@FXML
	private TableColumn<Pedido, LocalDate> colTicketFecha;
	@FXML
	private TableColumn<Pedido, Float> colTicketTotal;
	@FXML
	private TableColumn<Pedido, String> colTicketEstado;

	@FXML
	private Label lblTicketSeleccionado;
	
	// Tabla de Detalles
	@FXML
	private TableView<Detalle> tablaDetalles;
	@FXML
	private TableColumn<Detalle, Integer> colDetCantidad;
	@FXML
	private TableColumn<Detalle, String> colDetServicio;
	@FXML
	private TableColumn<Detalle, Float> colDetPrecio;
	@FXML
	private TableColumn<Detalle, Float> colDetTotal;

	@FXML
	private CheckBox chkImprimirMarcas;

	@FXML
	private Label lblAlerta;

	@FXML
	public void initialize() {
		configurarColumnasTickets();
		configurarColumnasDetalle();

		tablaTickets.setItems(listaPedidosObs);
		tablaDetalles.setItems(listaDetallesObs);

		tablaTickets.getSelectionModel().selectedItemProperty()
				.addListener((observable, oldSelection, newSelection) -> {
					if (newSelection != null) {
						cargarDetallesDeTicket(newSelection);
					} else {
						limpiarDetalles();
					}
				});

		dpFiltroDesde.setValue(LocalDate.now().minusDays(14));
		dpFiltroHasta.setValue(LocalDate.now().plusDays(14));

		cargaListaClientes();
	}

	// Asigna el  cliente selecionado del comboBox
	private void sincronizarSeleccion(Cliente clienteSeleccionado) {
		if (sincronizandoCliente)
			return;

		sincronizandoCliente = true;
		cmbFiltroCliente.setValue(clienteSeleccionado);
		cmbFiltroTelefono.setValue(clienteSeleccionado);
		sincronizandoCliente = false;
	}

	// Carga los comboBox de clientes
	private void cargaListaClientes() {
		try {
			clientesObs = FXCollections.observableArrayList(clienteDao.obtenerListaTodasEntidades());
		} catch (Exception e) {
			e.printStackTrace();
		}

		cmbFiltroCliente.setItems(clientesObs);
		cmbFiltroTelefono.setItems(clientesObs);

		cmbFiltroCliente.setConverter(new StringConverter<Cliente>() {
			@Override
			public String toString(Cliente cliente) {
				return cliente == null ? "" : cliente.getNombreCompleto();
			}

			@Override
			public Cliente fromString(String string) {
				if (string == null || string.isEmpty())
					return null;
				return clientesObs.stream()
						.filter(c -> c.getNombreCompleto() != null
								&& c.getNombreCompleto().toLowerCase().contains(string.toLowerCase()))
						.findFirst().orElse(null);
			}
		});

		cmbFiltroTelefono.setConverter(new StringConverter<Cliente>() {
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

		cmbFiltroCliente.valueProperty().addListener((obs, oldVal, newVal) -> sincronizarSeleccion(newVal));
		cmbFiltroTelefono.valueProperty().addListener((obs, oldVal, newVal) -> sincronizarSeleccion(newVal));
	}

	// Formato de la tabla de Tickets
	private void configurarColumnasTickets() {
		tablaTickets.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("codigoPedido"));
		tablaTickets.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("cliente"));
		tablaTickets.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("fecha"));
		tablaTickets.getColumns().get(3).setCellValueFactory(new PropertyValueFactory<>("estado"));

		TableColumn<Pedido, Float> colTotal = (TableColumn<Pedido, Float>) tablaTickets.getColumns().get(4);
		colTotal.setCellValueFactory(new PropertyValueFactory<>("precio"));
		// Formato del precio en euros
		colTotal.setCellFactory(columna -> new TableCell<Pedido, Float>() {
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

	// Formato de la tabla de servicios
	private void configurarColumnasDetalle() {
		tablaDetalles.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("servicio"));

		TableColumn<Detalle, Float> colPrecioDetalle = (TableColumn<Detalle, Float>) tablaDetalles.getColumns().get(1);
		colPrecioDetalle.setCellValueFactory(new PropertyValueFactory<>("precioUnitario"));
		// Formato del precio en euros
		colPrecioDetalle.setCellFactory(columna -> new TableCell<Detalle, Float>() {
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

	@FXML
	private void ejecutarBusqueda() {
		try {
			// Se toman todas las variables de busqueda
			String filtroTicket = txtFiltroNumTicket.getText() != null ? txtFiltroNumTicket.getText().trim() : "";

			String filtroAnio = txtFiltroAnio.getText() != null ? txtFiltroAnio.getText().trim() : "";

			String tempCliente = "";
			if (cmbFiltroCliente.getValue() != null) {
				tempCliente = cmbFiltroCliente.getValue().getNombreCompleto().toLowerCase();
			} else if (cmbFiltroCliente.getEditor().getText() != null) {
				tempCliente = cmbFiltroCliente.getEditor().getText().trim().toLowerCase();
			}

			final String filtroCliente = tempCliente;

			LocalDate fechaDesde = dpFiltroDesde.getValue();
			LocalDate fechaHasta = dpFiltroHasta.getValue();

			List<Pedido> todosLosPedidos = pedidoDao.obtenerListaTodasEntidades();

			List<Pedido> resultados = todosLosPedidos.stream().filter(pedido -> {
				// Se iltra con flujo de datos
				boolean coincideTicket = true;
				boolean coincideCliente = true;
				boolean coincideAnio = true;
				boolean coincideDesde = true;
				boolean coincideHasta = true;
				// Se comprueba que filtros se estan usando para usarlos en la busqueda
				if (!filtroTicket.isEmpty()) {
					coincideTicket = String.valueOf(pedido.getCodigoPedido()).contains(filtroTicket);
				}
				if (!filtroCliente.isEmpty() && pedido.getCliente() != null) {
					coincideCliente = pedido.getCliente().getNombreCompleto().toLowerCase().contains(filtroCliente);
				}
				if (!filtroAnio.isEmpty() && pedido.getFecha() != null) {
					coincideAnio = String.valueOf(pedido.getFecha().getYear()).contains(filtroAnio);
				}
				if (fechaDesde != null && pedido.getFecha() != null) {
					coincideDesde = !pedido.getFecha().isBefore(fechaDesde);
				}
				if (fechaHasta != null && pedido.getFecha() != null) {
					coincideHasta = !pedido.getFecha().isAfter(fechaHasta);
				}

				return coincideTicket && coincideAnio && coincideCliente && coincideDesde && coincideHasta;
			}).collect(Collectors.toList());

			listaPedidosObs.clear();
			if (resultados != null) {
				listaPedidosObs.addAll(resultados);
			}

			tablaTickets.getSelectionModel().clearSelection();
			limpiarDetalles();

		} catch (Exception e) {
			System.err.println("Error al ejecutar la búsqueda: " + e.getMessage());
			e.printStackTrace();
		}
	}

	@FXML
	private void reiniciarBusqueda() {
		// Limpia los textfield
		txtFiltroNumTicket.clear();
		txtFiltroAnio.clear();

		// Limpia los ComboBox
		if (cmbFiltroCliente != null) {
			cmbFiltroCliente.setValue(null);
			cmbFiltroCliente.getEditor().clear();
		}

		if (cmbFiltroTelefono != null) {
			cmbFiltroTelefono.setValue(null);
			cmbFiltroTelefono.getEditor().clear();
		}

		// Asigna las fechas a su valor "por defecto"
		dpFiltroDesde.setValue(LocalDate.now().minusDays(14));
		dpFiltroHasta.setValue(LocalDate.now().plusDays(14));

		// Limpia el texto de alerta
		lblAlerta.setText("");

		// Llama a la busqueda para mostrar todos los registros
		ejecutarBusqueda();
	}

	private void cargarDetallesDeTicket(Pedido pedidoSeleccionado) {
		lblTicketSeleccionado.setText("Ticket Nº: " + pedidoSeleccionado.getCodigoPedido() + " - "
				+ pedidoSeleccionado.getCliente().getNombre());

		try {
			listaDetallesObs.clear();

			List<Detalle> detalles = detalleDao.obtenerDetallesPorPedido(pedidoSeleccionado.getIdentificador());

			if (detalles != null && !detalles.isEmpty()) {
				listaDetallesObs.addAll(detalles);
			}

		} catch (Exception e) {
			System.err.println("Error cargando los detalles: " + e.getMessage());
			e.printStackTrace();
		}
	}

	@FXML
	private void generarFactura() {
		Pedido pedidoSeleccionado = tablaTickets.getSelectionModel().getSelectedItem();

		if (pedidoSeleccionado == null) {
			lblAlerta.setText("Seleccione un ticket para hacer la factura");
			return;
		} else if (pedidoSeleccionado.getEstado() == EstadoPedido.Cancelado) {
			lblAlerta.setText("Error: ticket cancelado");
			return;
		}

		lblAlerta.setText("Generando factura, por favor espere...");

		Task<Void> tareaFactura = new Task<>() {
			@Override
			protected Void call() throws Exception {
				DetalleDaoHBNT detalleDao = new DetalleDaoHBNT();
				FacturaDaoHBNT facturaDao = new FacturaDaoHBNT();

				boolean existenciaPrevia = facturaDao.confirmarExistenciaTicket(pedidoSeleccionado.getIdentificador());
				int numeroFactura = 0;
				Factura facturaImprimir = new Factura();

				if (!existenciaPrevia) {
					// Lógica de factura nueva
					numeroFactura = facturaDao.obtenerUltimoIdentificador() + 1;
					facturaImprimir.setIdentificador(numeroFactura);
					facturaImprimir.setPedido(pedidoSeleccionado);
					facturaImprimir.setFecha(LocalDate.now());
					facturaImprimir.setSerial(pedidoSeleccionado.getCodigoPedido() + "-" + pedidoSeleccionado.getFecha().getYear());
					facturaDao.crearEntidad(facturaImprimir);
				} else {
					// Como ya existe hay que notificarle al usuario
					Platform.runLater(() -> lblAlerta.setText("Factura previa encontrada, regenerando PDF..."));

					facturaImprimir = facturaDao.obtenerFacturaPorTicket(pedidoSeleccionado.getIdentificador());
					numeroFactura = facturaImprimir.getIdentificador();
				}

				// Generar el nuevo PFD
				PdfGeneratorService pdfService = new PdfGeneratorService();
				Map<String, Object> model = new HashMap<>();
				List<Detalle> detalles = detalleDao.obtenerDetallesPorPedido(pedidoSeleccionado.getIdentificador());

				String fechaTexto = LocalDate.now()
						.format(DateTimeFormatter.ofPattern("dd 'de' MMMM 'del' yyyy", new Locale("es", "ES")));
				model.put("fechaCompleta", fechaTexto);

				model.put("serial", String.format("%04d", numeroFactura));

				Cliente cliente = pedidoSeleccionado.getCliente();
				model.put("cliente",
						Map.of("nombre",
								cliente.getNombreCompleto() != null ? cliente.getNombreCompleto() : "Cliente Contado",
								"calle", cliente.getDireccion() != null ? cliente.getDireccion() : "", "codigoPostal",
								cliente.getCodigoPostal() > 0 ? String.valueOf(cliente.getCodigoPostal()) : "", "nif",
								cliente.getCif() != null ? cliente.getCif() : ""));

				List<Map<String, String>> listaProductos = new ArrayList<>();
				float sumaBaseImponible = 0f;
				float sumaIva = 0f;
				float granTotal = 0f;
				String ivaProp = GestorFicheroProperties.getValorConfig("iva.general");
				final float TIPO_IVA = Float.parseFloat(ivaProp) / 100f;

				for (Detalle d : detalles) {
					float totalLinea = d.getPrecioUnitario();

					float baseLinea = totalLinea / (1 + TIPO_IVA);
					float ivaLinea = totalLinea - baseLinea;

					sumaBaseImponible += baseLinea;
					sumaIva += ivaLinea;
					granTotal += totalLinea;

					String descripcionCompleta = "Ticket " + pedidoSeleccionado.getCodigoPedido() + " - "
							+ d.getServicio().getNombre();

					listaProductos.add(Map.of("descripcion", descripcionCompleta, "base",
							String.format(new Locale("es", "ES"), "%.2f", baseLinea), "iva",
							String.format(new Locale("es", "ES"), "%.2f", ivaLinea), "total",
							String.format(new Locale("es", "ES"), "%.2f", totalLinea)));
				}
				model.put("productos", listaProductos);

				model.put("iva", String.valueOf(ivaProp));

				model.put("totales",
						Map.of("baseImponible", String.format(new Locale("es", "ES"), "%.2f", sumaBaseImponible),
								"totalIva", String.format(new Locale("es", "ES"), "%.2f", sumaIva), "granTotal",
								String.format(new Locale("es", "ES"), "%.2f", granTotal)));

				String nombreArchivo = "factura_" + numeroFactura + ".pdf";

				pdfService.generatePdf("FacturaZYPSASTRERIA", model, nombreArchivo);

				return null;
			}
		};

		tareaFactura.setOnSucceeded(workerStateEvent -> lblAlerta.setText("Factura generada con éxito"));

		tareaFactura.setOnFailed(workerStateEvent -> {
			lblAlerta.setText("Error crítico al renderizar el PDF.");
			tareaFactura.getException().printStackTrace();
		});

		// Arrancamos el hilo secundario para liberar el hilo de la interfaz
		new Thread(tareaFactura).start();
	}

	@FXML
	private void reimprimirTicket() {
		Pedido pedidoSeleccionado = tablaTickets.getSelectionModel().getSelectedItem();

		if (pedidoSeleccionado == null) {
			lblAlerta.setText("Por favor, seleccione un ticket de la tabla para imprimir.");
			return;
		}

		try {
			boolean esCompleto = chkImprimirMarcas != null && chkImprimirMarcas.isSelected();

			List<Detalle> detalles = detalleDao.obtenerDetallesPorPedido(pedidoSeleccionado.getIdentificador());

			TicketPrinterService printerService = new TicketPrinterService();
			printerService.imprimir(pedidoSeleccionado, detalles, esCompleto);

		} catch (Exception e) {
			lblAlerta.setText("Error al intentar reimprimir el ticket físico.");
			e.printStackTrace();
		}
	}

	@FXML
	private void handleRetirarTicket() {
		Pedido pedidoSeleccionado = tablaTickets.getSelectionModel().getSelectedItem();

		if (pedidoSeleccionado == null) {
			lblAlerta.setText("Por favor, seleccione un ticket de la tabla para retirarlo.");
			return;
		}

		if (pedidoSeleccionado.getEstado() == EstadoPedido.Sin_Pagar) {
			mostrarVentanaCobro(pedidoSeleccionado);
		} else {
			ejecutarRetirada(pedidoSeleccionado);
		}
	}

	@FXML
	private void handleAnularTicket() {
		Pedido pedidoSeleccionado = tablaTickets.getSelectionModel().getSelectedItem();

		if (pedidoSeleccionado == null) {
			lblAlerta.setText("Seleccione un ticket de la tabla para anularlo");
			return;
		} else if (pedidoSeleccionado.getEstado() != EstadoPedido.Sin_Pagar) {
			lblAlerta.setText("Solo se pueden cancelar tickets impagos");
			return;
		}

		mostrarVentanaAnulacion(pedidoSeleccionado);
	}

	private void mostrarVentanaAnulacion(Pedido pedido) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("anular.fxml"));
			Parent root = loader.load();

			AnularController controladorAnular = loader.getController();

			Image icono = new Image(getClass().getResourceAsStream("/imagenes/Clotho.png"));

			Stage stage = new Stage();
			stage.initModality(Modality.APPLICATION_MODAL);
			stage.setTitle("Confirmar Anulación");
			stage.getIcons().add(icono);
			stage.setScene(new Scene(root));
			stage.showAndWait();

			if (controladorAnular.isConfirmado()) {
				ejecutarAnulacion(pedido);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Metodo de confirmacion de anular un ticket
	private void ejecutarAnulacion(Pedido pedido) {
		try {
			pedido.setEstado(EstadoPedido.Cancelado);

			this.pedidoDao.actualizarEntidad(pedido);

			tablaTickets.refresh();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Ventana emergente si el ticket aun no ha sido pagado
	private void mostrarVentanaCobro(Pedido pedido) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("cobro.fxml"));
			Parent root = loader.load();

			CobroController controladorCobro = loader.getController();
			controladorCobro.setImporte(pedido.getPrecio());

			Image icono = new Image(getClass().getResourceAsStream("/imagenes/Clotho.png"));

			Stage stage = new Stage();
			stage.initModality(Modality.APPLICATION_MODAL);
			stage.setTitle("Cobro Pendiente");
			stage.getIcons().add(icono);
			stage.setScene(new Scene(root));

			stage.showAndWait();

			if (controladorCobro.isConfirmado()) {
				ejecutarRetirada(pedido);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Metodo para actualizar un ticket a "Retirado"
	private void ejecutarRetirada(Pedido pedido) {
		try {
			pedido.setEstado(EstadoPedido.Retirado);
			this.pedidoDao.actualizarEntidad(pedido);
			tablaTickets.refresh();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void limpiarDetalles() {
		lblTicketSeleccionado.setText("Ticket Nº: --");
		listaDetallesObs.clear();
	}
}