package org.openjfx.clotho.proy;

import java.time.LocalDate;
import java.util.List;

import org.openjfx.clotho.proy.dao.hbnt.DetalleDaoHBNT;
import org.openjfx.clotho.proy.dao.hbnt.PedidoDaoHBNT;
import org.openjfx.clotho.proy.vo.Detalle;
import org.openjfx.clotho.proy.vo.Pedido;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;

public class PedidosController {

    // --- FILTROS DE BÚSQUEDA ---
    @FXML private ToggleGroup grupoAnio;
    @FXML private TextField txtFiltroNumTicket;
    @FXML private TextField txtFiltroCliente;
    @FXML private DatePicker dpFiltroDesde;
    @FXML private DatePicker dpFiltroHasta;

    // --- TABLA MAESTRA (TICKETS/PEDIDOS) ---
    @FXML private TableView<Pedido> tablaTickets;
    // Nota: Declaramos las columnas internamente para configurarlas
    @FXML private TableColumn<Pedido, Integer> colTicketId;
    @FXML private TableColumn<Pedido, String> colTicketCliente;
    @FXML private TableColumn<Pedido, LocalDate> colTicketFecha;
    @FXML private TableColumn<Pedido, Float> colTicketTotal;
    @FXML private TableColumn<Pedido, String> colTicketEstado;

    // --- TABLA DETALLE (ARREGLOS/SERVICIOS) ---
    @FXML private Label lblTicketSeleccionado;
    @FXML private TableView<Detalle> tablaDetalles;
    // Nota: Las columnas deben coincidir con tu FXML y con tu clase Detalle.java
    @FXML private TableColumn<Detalle, Integer> colDetCantidad;
    @FXML private TableColumn<Detalle, String> colDetServicio;
    @FXML private TableColumn<Detalle, Float> colDetPrecio;
    @FXML private TableColumn<Detalle, Float> colDetTotal;

    // --- PANELES INFERIORES Y CONTROLES ---
    @FXML private CheckBox chkImprimirMarcas;
    @FXML private TextField txtTotalInferior;

    // --- DAOs y Observables ---
    private PedidoDaoHBNT pedidoDao = new PedidoDaoHBNT();
    private DetalleDaoHBNT detalleDao = new DetalleDaoHBNT();
    private ObservableList<Pedido> listaPedidosObs = FXCollections.observableArrayList();
    private ObservableList<Detalle> listaDetallesObs = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        configurarColumnasMaestro();
        configurarColumnasDetalle();

        // Inicializar las tablas con las listas observables vacías
        tablaTickets.setItems(listaPedidosObs);
        tablaDetalles.setItems(listaDetallesObs);

        // PATRÓN MAESTRO-DETALLE: Listener de selección en la tabla de Tickets
        tablaTickets.getSelectionModel().selectedItemProperty().addListener((observable, oldSelection, newSelection) -> {
            if (newSelection != null) {
                // Si el usuario selecciona un ticket, cargamos sus detalles
                cargarDetallesDeTicket(newSelection);
            } else {
                // Si la selección se borra, limpiamos la tabla de abajo
                limpiarDetalles();
            }
        });

        // Valores por defecto para los filtros de fecha
        dpFiltroDesde.setValue(LocalDate.now().minusDays(7)); // Última semana
        dpFiltroHasta.setValue(LocalDate.now());
    }

    private void configurarColumnasMaestro() {
        // IMPORTANTE: Los strings ("codigoPedido", "cliente", etc.) DEBEN coincidir 
        // exactamente con los nombres de las variables en tu clase Pedido.java
        
        // Asumiendo que tu FXML no tiene fx:id en las columnas, debes mapearlas por índice o 
        // añadir fx:id en el FXML a cada TableColumn.
        // Aquí te muestro la forma haciéndolo por índice para que coincida con el FXML anterior:
        
        if(tablaTickets.getColumns().size() >= 10) {
            tablaTickets.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("codigoPedido"));
            tablaTickets.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("cliente")); // Asume que cliente tiene un toString()
            tablaTickets.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("fecha"));
            // ... Mapear el resto según los atributos de Pedido.java ...
            tablaTickets.getColumns().get(5).setCellValueFactory(new PropertyValueFactory<>("precio"));
        }
    }

    private void configurarColumnasDetalle() {
        // IMPORTANTE: Los strings ("cantidad", "servicio", etc.) DEBEN coincidir 
        // exactamente con los nombres de las variables en tu clase Detalle.java
        
        if(tablaDetalles.getColumns().size() >= 7) {
            tablaDetalles.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("cantidad"));
            tablaDetalles.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("servicio")); // Asume que servicio tiene un toString()
            tablaDetalles.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("precioUnitario"));
            // ... Mapear el resto según los atributos de Detalle.java ...
        }
    }

    @FXML
    private void ejecutarBusqueda() {
        try {
            // 1. Recoger datos de los filtros
            String filtroTicket = txtFiltroNumTicket.getText().trim();
            String filtroCliente = txtFiltroCliente.getText().trim();
            LocalDate fechaDesde = dpFiltroDesde.getValue();
            LocalDate fechaHasta = dpFiltroHasta.getValue();

            // 2. Aquí llamarías a un método de tu DAO que filtre en base de datos.
            // Ejemplo simulado:
            // List<Pedido> resultados = pedidoDao.buscarConFiltros(filtroTicket, filtroCliente, fechaDesde, fechaHasta);
            
            // Para evitar errores de compilación, cargo todos temporalmente:
            List<Pedido> resultados = pedidoDao.obtenerListaTodasEntidades();

            // 3. Actualizar la tabla maestra
            listaPedidosObs.clear();
            if (resultados != null) {
                listaPedidosObs.addAll(resultados);
            }
            
            // 4. Limpiar la selección anterior y la tabla de detalles
            tablaTickets.getSelectionModel().clearSelection();
            limpiarDetalles();
            
        } catch (Exception e) {
            System.err.println("Error al ejecutar la búsqueda: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void cargarDetallesDeTicket(Pedido pedidoSeleccionado) {
        // 1. Actualizamos la etiqueta visual
        lblTicketSeleccionado.setText("Ticket Nº: " + pedidoSeleccionado.getCodigoPedido() + 
                                      " - " + pedidoSeleccionado.getCliente().getNombre());
        
        // 2. Buscamos los detalles en la base de datos (requiere método en DetalleDaoHBNT)
        try {
            // Ejemplo simulado:
            // List<Detalle> detalles = detalleDao.obtenerDetallesPorPedido(pedidoSeleccionado.getIdentificador());
            
            // Para la compilación de este ejemplo asumo que el pedido trae sus detalles
            // List<Detalle> detalles = pedidoSeleccionado.getDetalles(); 
            
            listaDetallesObs.clear();
            // if (detalles != null) { listaDetallesObs.addAll(detalles); }
            
            // 3. Actualizamos el total inferior de la suma de los arreglos
            float total = 0f;
            for(Detalle d : listaDetallesObs) {
                total += (d.getPrecioUnitario());
            }
            txtTotalInferior.setText(String.format("%.2f €", total));
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void limpiarDetalles() {
        lblTicketSeleccionado.setText("Ticket Nº: --");
        listaDetallesObs.clear();
        txtTotalInferior.setText("0.00 €");
    }
}