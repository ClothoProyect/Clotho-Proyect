package org.openjfx.clotho.proy;

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
import org.openjfx.clotho.proy.documentos.TicketPrinterService;
import org.openjfx.clotho.proy.vo.Cliente;
import org.openjfx.clotho.proy.vo.Detalle;
import org.openjfx.clotho.proy.vo.Factura;
import org.openjfx.clotho.proy.vo.Pedido;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
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
import javafx.util.StringConverter;

public class PedidosController {

	// --- FILTROS DE BÚSQUEDA ---
	@FXML
	private ComboBox<Cliente> cmbFiltroCliente;
	@FXML
	private ComboBox<Cliente> cmbFiltroTelefono;

	private ClienteDAO clienteDao = new ClienteDaoHBNT();

	private ObservableList<Cliente> clientesObs = FXCollections.observableArrayList();

	// Variable para evitar bucles infinitos al sincronizar
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

	// --- TABLA MAESTRA (TICKETS/PEDIDOS) ---
	@FXML
	private TableView<Pedido> tablaTickets;
	// Nota: Declaramos las columnas internamente para configurarlas
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

	// --- TABLA DETALLE (ARREGLOS/SERVICIOS) ---
	@FXML
	private Label lblTicketSeleccionado;
	@FXML
	private TableView<Detalle> tablaDetalles;
	// Nota: Las columnas deben coincidir con tu FXML y con tu clase Detalle.java
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

	// --- DAOs y Observables ---
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

		// AÑADIMOS ESTO: Cargar los clientes en los ComboBox
		cargaListaClientes();
	}

	// --- MÉTODOS COPIADOS Y ADAPTADOS DEL PRINCIPALCONTROLLER ---

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

		// --- COLUMNA 4: TOTAL (CON FORMATO DE MONEDA) ---
		// 1. Le decimos de dónde sacar el dato numérico
		TableColumn<Pedido, Float> colTotal = (TableColumn<Pedido, Float>) tablaTickets.getColumns().get(4);
		colTotal.setCellValueFactory(new PropertyValueFactory<>("precio"));

		// 2. Le decimos cómo debe dibujarlo en pantalla
		colTotal.setCellFactory(columna -> new TableCell<Pedido, Float>() {
			@Override
			protected void updateItem(Float precio, boolean empty) {
				super.updateItem(precio, empty);

				// Si la fila está vacía o el precio es nulo, no mostramos nada
				if (empty || precio == null) {
					setText(null);
				} else {
					// Si hay precio, lo formateamos a 2 decimales y le añadimos el €
					// (Ejemplo: 15.0 -> "15,00 €")
					setText(String.format("%.2f €", precio));
				}
			}
		});
	}

	private void configurarColumnasDetalle() {
		// Columna 0: Prenda - Arreglo
		tablaDetalles.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("servicio"));

		// --- COLUMNA 1: PRECIO (CON FORMATO DE MONEDA) ---
		// Obtenemos la columna del precio (índice 1)
		TableColumn<Detalle, Float> colPrecioDetalle = (TableColumn<Detalle, Float>) tablaDetalles.getColumns().get(1);
		colPrecioDetalle.setCellValueFactory(new PropertyValueFactory<>("precioUnitario"));

		// Le aplicamos el mismo formato que usamos arriba
		colPrecioDetalle.setCellFactory(columna -> new TableCell<Detalle, Float>() {
			@Override
			protected void updateItem(Float precio, boolean empty) {
				super.updateItem(precio, empty);

				// Si la celda está vacía o es null, la dejamos en blanco
				if (empty || precio == null) {
					setText(null);
				} else {
					// Si hay un precio, lo formateamos con 2 decimales y el símbolo €
					setText(String.format("%.2f €", precio));
				}
			}
		});

		// Si más adelante usas la columna de notas (índice 2), la mapearías así:
		// tablaDetalles.getColumns().get(2).setCellValueFactory(new
		// PropertyValueFactory<>("notas"));
	}

	@FXML
	private void ejecutarBusqueda() {
		try {
			// 1. Recoger datos de los filtros
			String filtroTicket = txtFiltroNumTicket.getText() != null ? txtFiltroNumTicket.getText().trim() : "";

			// Usamos una variable temporal para las comprobaciones
			String tempCliente = "";
			if (cmbFiltroCliente.getValue() != null) {
				tempCliente = cmbFiltroCliente.getValue().getNombreCompleto().toLowerCase();
			} else if (cmbFiltroCliente.getEditor().getText() != null) {
				tempCliente = cmbFiltroCliente.getEditor().getText().trim().toLowerCase();
			}

			// ESTA es la variable "final" que Java sí dejará usar dentro del Stream
			final String filtroCliente = tempCliente;

			LocalDate fechaDesde = dpFiltroDesde.getValue();
			LocalDate fechaHasta = dpFiltroHasta.getValue();

			// 2. Obtenemos TODOS los pedidos de la base de datos
			List<Pedido> todosLosPedidos = pedidoDao.obtenerListaTodasEntidades();

			// 3. FILTRAMOS la lista en base a lo que el usuario ha escrito
			List<Pedido> resultados = todosLosPedidos.stream().filter(pedido -> {
				boolean coincideTicket = true;
				boolean coincideCliente = true;
				boolean coincideDesde = true;
				boolean coincideHasta = true;

				// Comprobar Nº de Ticket
				if (!filtroTicket.isEmpty()) {
					coincideTicket = String.valueOf(pedido.getCodigoPedido()).contains(filtroTicket);
				}

				// Comprobar Nombre/Apellidos de Cliente (Usando la variable final)
				if (!filtroCliente.isEmpty() && pedido.getCliente() != null) {
					coincideCliente = pedido.getCliente().getNombreCompleto().toLowerCase().contains(filtroCliente);
				}

				// Comprobar Fecha Desde
				if (fechaDesde != null && pedido.getFecha() != null) {
					// isBefore = ¿Es anterior a la fecha 'Desde'? Si es false, entonces cumple la
					// regla.
					coincideDesde = !pedido.getFecha().isBefore(fechaDesde);
				}

				// Comprobar Fecha Hasta
				if (fechaHasta != null && pedido.getFecha() != null) {
					// isAfter = ¿Es posterior a la fecha 'Hasta'? Si es false, entonces cumple la
					// regla.
					coincideHasta = !pedido.getFecha().isAfter(fechaHasta);
				}

				// El pedido solo pasa a la lista final si cumple TODAS las reglas
				return coincideTicket && coincideCliente && coincideDesde && coincideHasta;

			}).collect(Collectors.toList());

			// 4. Actualizar la tabla maestra con los resultados filtrados
			listaPedidosObs.clear();
			if (resultados != null) {
				listaPedidosObs.addAll(resultados);
			}

			// 5. Limpiar la selección anterior y la tabla de detalles
			tablaTickets.getSelectionModel().clearSelection();
			limpiarDetalles();

		} catch (Exception e) {
			System.err.println("Error al ejecutar la búsqueda: " + e.getMessage());
			e.printStackTrace();
		}
	}

	private void cargarDetallesDeTicket(Pedido pedidoSeleccionado) {
		// 1. Actualizamos la etiqueta visual
		lblTicketSeleccionado.setText("Ticket Nº: " + pedidoSeleccionado.getCodigoPedido() + " - "
				+ pedidoSeleccionado.getCliente().getNombre());

		try {
			// 2. Limpiamos los detalles del ticket anterior
			listaDetallesObs.clear();

			// 3. BUSCAMOS LOS DETALLES REALES
			// Opcion A: Si tu clase Pedido tiene una lista de detalles (OneToMany) cargada
			// por Hibernate:
			// List<Detalle> detalles = pedidoSeleccionado.getDetalles();

			// Opcion B: Si usas el DAO para buscar los detalles por el ID del pedido
			// (Recomendado):
			List<Detalle> detalles = detalleDao.obtenerDetallesPorPedido(pedidoSeleccionado.getIdentificador());

			// 4. Si hay detalles, los metemos en la tabla
			if (detalles != null && !detalles.isEmpty()) {
				listaDetallesObs.addAll(detalles);
			}

			// Nota: He quitado lo del 'txtTotalInferior' porque en el paso anterior
			// lo borramos para solucionar el NullPointerException.

		} catch (Exception e) {
			System.err.println("Error cargando los detalles: " + e.getMessage());
			e.printStackTrace();
		}
	}

	@FXML
	private void generarFactura() {
		// 1. Obtener el pedido seleccionado en la tabla principal
		Pedido pedidoSeleccionado = tablaTickets.getSelectionModel().getSelectedItem();

		if (pedidoSeleccionado == null) {
			System.err.println("Por favor, seleccione un ticket de la tabla para hacer la factura.");
			return;
		}

		try {
			// 2. Traemos todos los arreglos (detalles) de este pedido
			DetalleDaoHBNT detalleDao = new DetalleDaoHBNT();
			FacturaDaoHBNT facturaDao = new FacturaDaoHBNT();
			List<Detalle> detalles = detalleDao.obtenerDetallesPorPedido(pedidoSeleccionado.getIdentificador());

			// 3. Preparamos el generador y el "Diccionario" de datos para la plantilla
			org.openjfx.clotho.proy.documentos.PdfGeneratorService pdfService = new org.openjfx.clotho.proy.documentos.PdfGeneratorService();
			Map<String, Object> model = new java.util.HashMap<>();

			// --- FECHA Y SERIAL ---
			// --- FECHA Y SERIAL ---
			String fechaTexto = LocalDate.now()
					.format(DateTimeFormatter.ofPattern("dd 'de' MMMM 'del' yyyy", new Locale("es", "ES")));
			model.put("fechaCompleta", fechaTexto);
			model.put("serial", String.format("%04d", pedidoSeleccionado.getCodigoPedido()));

			// --- DATOS DEL CLIENTE ---
			Cliente cliente = pedidoSeleccionado.getCliente();
			model.put("cliente",
					Map.of("nombre",
							cliente.getNombreCompleto() != null ? cliente.getNombreCompleto() : "Cliente Contado",
							"calle", cliente.getDireccion() != null ? cliente.getDireccion() : "", "codigoPostal",
							cliente.getCodigoPostal() > 0 ? String.valueOf(cliente.getCodigoPostal()) : "", "nif",
							cliente.getCif() != null ? cliente.getCif() : ""));

			// --- LISTA DE PRODUCTOS Y DESGLOSE DE IVA ---
			List<Map<String, String>> listaProductos = new java.util.ArrayList<>();
			float sumaBaseImponible = 0f;
			float sumaIva = 0f;
			float granTotal = 0f;

			// Asumimos IVA 21%. Cámbialo si tu régimen fiscal es distinto (Ej: 0.10f para
			// 10%)
			final float TIPO_IVA = 0.21f;

			for (Detalle d : detalles) {
				float totalLinea = d.getPrecioUnitario();

				// Cálculo matemático inverso para separar IVA de la Base
				float baseLinea = totalLinea / (1 + TIPO_IVA);
				float ivaLinea = totalLinea - baseLinea;

				// Sumamos a los acumuladores globales
				sumaBaseImponible += baseLinea;
				sumaIva += ivaLinea;
				granTotal += totalLinea;

				// Construimos la descripción (Nombre Servicio + Notas si las hay)
				// Asignamos el código del pedido a la descripción de la factura
				// Combinamos el código del pedido con el nombre del arreglo
				String descripcionCompleta = "Ticket " + pedidoSeleccionado.getCodigoPedido() + " - "
						+ d.getServicio().getNombre();

				// Añadimos el producto a la lista con formato de España (comas y 2 decimales)
				listaProductos.add(Map.of("descripcion", descripcionCompleta, "base",
						String.format(new Locale("es", "ES"), "%.2f", baseLinea), "iva",
						String.format(new Locale("es", "ES"), "%.2f", ivaLinea), "total",
						String.format(new Locale("es", "ES"), "%.2f", totalLinea)));
			}
			model.put("productos", listaProductos);

			// --- TOTALES ---
			model.put("totales",
					Map.of("baseImponible", String.format(new Locale("es", "ES"), "%.2f", sumaBaseImponible),
							"totalIva", String.format(new Locale("es", "ES"), "%.2f", sumaIva), "granTotal",
							String.format(new Locale("es", "ES"), "%.2f", granTotal)));

			// 4. Generamos el PDF
			// 4. Generamos el PDF
			int numeroNuevaFactura = facturaDao.obtenerUltimoIdentificador() + 1;

			Factura nuevaFacturaBD = new Factura();

			// Le asignamos el nuevo identificador
			nuevaFacturaBD.setIdentificador(numeroNuevaFactura);
			nuevaFacturaBD.setPedido(pedidoSeleccionado);
			nuevaFacturaBD.setFecha(LocalDate.now());

			facturaDao.crearEntidad(nuevaFacturaBD);

			String nombreArchivo = "factura_" + numeroNuevaFactura + ".pdf";

			pdfService.generatePdf("FacturaZYPSASTRERIA", model, nombreArchivo);

			System.out.println("Proceso de factura terminado.");
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Error grave al intentar generar la factura: " + e.getMessage());
		}
	}
	
	@FXML
	private void reimprimirTicket() {
		// 1. Obtenemos el pedido seleccionado de la tabla principal
		Pedido pedidoSeleccionado = tablaTickets.getSelectionModel().getSelectedItem();

		if (pedidoSeleccionado == null) {
			System.err.println("Por favor, seleccione un ticket de la tabla para imprimir.");
			return;
		}

		try {
			// 2. Leemos el CheckBox de la interfaz para saber qué tipo de impresión quiere el usuario
			// true = Completo (2 copias con arreglos) | false = Simple (1 copia)
			boolean esCompleto = chkImprimirMarcas != null && chkImprimirMarcas.isSelected();

			// 3. Extraemos los detalles (arreglos) asociados a ese ticket de la base de datos
			// Usamos la variable detalleDao que ya tenías declarada arriba
			List<Detalle> detalles = detalleDao.obtenerDetallesPorPedido(pedidoSeleccionado.getIdentificador());

			// 4. Invocamos al servicio físico de la impresora
			TicketPrinterService printerService = new TicketPrinterService();
			printerService.imprimir(pedidoSeleccionado, detalles, esCompleto);

		} catch (Exception e) {
			System.err.println("Error al intentar reimprimir el ticket físico.");
			e.printStackTrace();
		}
	}
	
	private void limpiarDetalles() {
		lblTicketSeleccionado.setText("Ticket Nº: --");
		listaDetallesObs.clear();
	}
}