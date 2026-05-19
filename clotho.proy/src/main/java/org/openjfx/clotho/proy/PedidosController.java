package org.openjfx.clotho.proy;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
import org.openjfx.clotho.proy.vo.Cliente;
import org.openjfx.clotho.proy.vo.Detalle;
import org.openjfx.clotho.proy.vo.Factura;
import org.openjfx.clotho.proy.vo.Pedido;
import org.openjfx.clotho.proy.vo.enumerate.EstadoPedido;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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

	@FXML
	private ComboBox<Cliente> cmbFiltroCliente;
	@FXML
	private ComboBox<Cliente> cmbFiltroTelefono;

	private ClienteDAO clienteDao = new ClienteDaoHBNT();

	private ObservableList<Cliente> clientesObs = FXCollections.observableArrayList();

	private boolean sincronizandoCliente = false;
	@FXML
	private ToggleGroup grupoAnio;
	@FXML
	private TextField txtFiltroNumTicket;
	@FXML
	private TextField txtFiltroCliente;
	@FXML
	private DatePicker dpFiltroDesde;
	@FXML
	private DatePicker dpFiltroHasta;

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

	// --- PANELES INFERIORES Y CONTROLES ---
	@FXML
	private CheckBox chkImprimirMarcas;

	@FXML
	private Label lblAlerta;

	private PedidoDaoHBNT pedidoDao = new PedidoDaoHBNT();
	private DetalleDaoHBNT detalleDao = new DetalleDaoHBNT();
	private ObservableList<Pedido> listaPedidosObs = FXCollections.observableArrayList();
	private ObservableList<Detalle> listaDetallesObs = FXCollections.observableArrayList();

	@FXML
	public void initialize() {
		configurarColumnasMaestro();
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

	private void sincronizarSeleccion(Cliente clienteSeleccionado) {
		if (sincronizandoCliente)
			return;

		sincronizandoCliente = true;
		cmbFiltroCliente.setValue(clienteSeleccionado);
		cmbFiltroTelefono.setValue(clienteSeleccionado);
		sincronizandoCliente = false;
	}

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

	private void configurarColumnasMaestro() {

		tablaTickets.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("codigoPedido")); // Columna 0:
																											// Nº Ticket
		tablaTickets.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("cliente")); // Columna 1:
																										// Cliente
		tablaTickets.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("fecha")); // Columna 2: Fecha
																									// Ticket
		tablaTickets.getColumns().get(3).setCellValueFactory(new PropertyValueFactory<>("estado")); // Columna 3: Estado

		TableColumn<Pedido, Float> colTotal = (TableColumn<Pedido, Float>) tablaTickets.getColumns().get(4);
		colTotal.setCellValueFactory(new PropertyValueFactory<>("precio"));

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

	private void configurarColumnasDetalle() {
		tablaDetalles.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("servicio"));

		TableColumn<Detalle, Float> colPrecioDetalle = (TableColumn<Detalle, Float>) tablaDetalles.getColumns().get(1);
		colPrecioDetalle.setCellValueFactory(new PropertyValueFactory<>("precioUnitario"));

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
			String filtroTicket = txtFiltroNumTicket.getText() != null ? txtFiltroNumTicket.getText().trim() : "";

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
				boolean coincideTicket = true;
				boolean coincideCliente = true;
				boolean coincideDesde = true;
				boolean coincideHasta = true;

				if (!filtroTicket.isEmpty()) {
					coincideTicket = String.valueOf(pedido.getCodigoPedido()).contains(filtroTicket);
				}

				if (!filtroCliente.isEmpty() && pedido.getCliente() != null) {
					coincideCliente = pedido.getCliente().getNombreCompleto().toLowerCase().contains(filtroCliente);
				}

				if (fechaDesde != null && pedido.getFecha() != null) {
					coincideDesde = !pedido.getFecha().isBefore(fechaDesde);
				}

				if (fechaHasta != null && pedido.getFecha() != null) {
					coincideHasta = !pedido.getFecha().isAfter(fechaHasta);
				}

				return coincideTicket && coincideCliente && coincideDesde && coincideHasta;

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

	private void cargarDetallesDeTicket(Pedido pedidoSeleccionado) {
		lblTicketSeleccionado.setText("Ticket Nº: " + pedidoSeleccionado.getCodigoPedido() + " - "
				+ pedidoSeleccionado.getCliente().getNombre());

		try {
			listaDetallesObs.clear();

			// (Recomendado):
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
		try {
			Pedido pedidoSeleccionado = tablaTickets.getSelectionModel().getSelectedItem();
			DetalleDaoHBNT detalleDao = new DetalleDaoHBNT();
			FacturaDaoHBNT facturaDao = new FacturaDaoHBNT();

			if (pedidoSeleccionado == null) {
				lblAlerta.setText("Seleccione un\nticket para\nhacer la factura");
				return;
			} else if (pedidoSeleccionado.getEstado() == EstadoPedido.Cancelado) {
				lblAlerta.setText("Error\nticket cancelado");
				return;
			}
			boolean existenciaPrevia = facturaDao.confirmarExistenciaTicket(pedidoSeleccionado.getIdentificador());

			if (existenciaPrevia) {
				lblAlerta.setText("Factura generada\npreviamente");
			}
			
			int numeroFactura = 0;
			Factura facturaImprimir = new Factura();

			if (!existenciaPrevia) {
				numeroFactura = facturaDao.obtenerUltimoIdentificador() + 1;
				facturaImprimir.setIdentificador(numeroFactura);
				facturaImprimir.setPedido(pedidoSeleccionado);
				facturaImprimir.setFecha(LocalDate.now());
				facturaDao.crearEntidad(facturaImprimir);
			} else {
				facturaImprimir = facturaDao.obtenerFacturaPorTicket(pedidoSeleccionado.getIdentificador());
				numeroFactura = facturaImprimir.getIdentificador();
			}

			PdfGeneratorService pdfService = new PdfGeneratorService();
			Map<String, Object> model = new java.util.HashMap<>();

			List<Detalle> detalles = detalleDao.obtenerDetallesPorPedido(pedidoSeleccionado.getIdentificador());

			String fechaTexto = LocalDate.now()
					.format(DateTimeFormatter.ofPattern("dd 'de' MMMM 'del' yyyy", new Locale("es", "ES")));
			model.put("fechaCompleta", fechaTexto);
			model.put("serial", String.format("%04d", pedidoSeleccionado.getCodigoPedido()));

			Cliente cliente = pedidoSeleccionado.getCliente();
			model.put("cliente",
					Map.of("nombre",
							cliente.getNombreCompleto() != null ? cliente.getNombreCompleto() : "Cliente Contado",
							"calle", cliente.getDireccion() != null ? cliente.getDireccion() : "", "codigoPostal",
							cliente.getCodigoPostal() > 0 ? String.valueOf(cliente.getCodigoPostal()) : "", "nif",
							cliente.getCif() != null ? cliente.getCif() : ""));

			List<Map<String, String>> listaProductos = new java.util.ArrayList<>();
			float sumaBaseImponible = 0f;
			float sumaIva = 0f;
			float granTotal = 0f;

			final float TIPO_IVA = 0.21f;

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

			model.put("totales",
					Map.of("baseImponible", String.format(new Locale("es", "ES"), "%.2f", sumaBaseImponible),
							"totalIva", String.format(new Locale("es", "ES"), "%.2f", sumaIva), "granTotal",
							String.format(new Locale("es", "ES"), "%.2f", granTotal)));

			String nombreArchivo = "factura_" + numeroFactura + ".pdf";

			pdfService.generatePdf("FacturaZYPSASTRERIA", model, nombreArchivo);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@FXML
	private void reimprimirTicket() {
		Pedido pedidoSeleccionado = tablaTickets.getSelectionModel().getSelectedItem();

		if (pedidoSeleccionado == null) {
			System.err.println("Por favor, seleccione un ticket de la tabla para imprimir.");
			return;
		}

		try {
			boolean esCompleto = chkImprimirMarcas != null && chkImprimirMarcas.isSelected();

			List<Detalle> detalles = detalleDao.obtenerDetallesPorPedido(pedidoSeleccionado.getIdentificador());

			TicketPrinterService printerService = new TicketPrinterService();
			printerService.imprimir(pedidoSeleccionado, detalles, esCompleto);

		} catch (Exception e) {
			System.err.println("Error al intentar reimprimir el ticket físico.");
			e.printStackTrace();
		}
	}

	@FXML
	private void handleRetirarTicket() {
		Pedido pedidoSeleccionado = tablaTickets.getSelectionModel().getSelectedItem();

		if (pedidoSeleccionado == null) {
			System.err.println("Por favor, seleccione un ticket de la tabla para retirarlo.");
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
			lblAlerta.setText("Seleccione un\nticket de la\ntabla para anularlo");
			return;
		} else if (pedidoSeleccionado.getEstado() != EstadoPedido.Sin_Pagar) {
			lblAlerta.setText("Solo se pueden\ncancelar tickets\nimpagos");
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

	private void ejecutarAnulacion(Pedido pedido) {
		try {
			pedido.setEstado(EstadoPedido.Cancelado);

			this.pedidoDao.actualizarEntidad(pedido);

			tablaTickets.refresh();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

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