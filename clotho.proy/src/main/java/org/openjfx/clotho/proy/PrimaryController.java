package org.openjfx.clotho.proy;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.openjfx.clotho.proy.dao.ClienteDAO;
import org.openjfx.clotho.proy.dao.hbnt.ClienteDaoHBNT;
import org.openjfx.clotho.proy.dao.hbnt.DetalleDaoHBNT;
import org.openjfx.clotho.proy.dao.hbnt.PedidoDaoHBNT;
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
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;

public class PrimaryController {
	private ClienteDAO clienteDao = new ClienteDaoHBNT();
	private ObservableList<Cliente> clientesObs;
	private List<Servicio> listaServicios = new ArrayList<Servicio>();
    private ObjectProperty<Cliente> clienteActual = new SimpleObjectProperty<>();
    private ObjectProperty<EstadoPedido> estado = new SimpleObjectProperty<>(EstadoPedido.Sin_Pagar);

    @FXML private CheckBox chkTarjeta;
    
    @FXML private Button btnFinalizarPedido;
    
	@FXML private TableView<Servicio> tablaServicios;

	@FXML private ComboBox<Cliente> cmbCliente;

	@FXML private DatePicker fechaPedido;

	@FXML private TextField txtOrario;

	@FXML private TextField txtBuscarCliente;
	
	@FXML private Label primerDia;
	
	@FXML private Label primerDiaFecha;
	
	@FXML private Label primerDiaPedidos;
	
	@FXML private Label primerDiaPrendas;

	@FXML public void initialize() {
		estableceFecha();
		
		cargaListaClientes();
	}

	private void cargaListaClientes() {
		try {
			clientesObs = FXCollections.observableArrayList(clienteDao.obtenerListaTodasEntidades());
		} catch (ProyectoClothoException e) {
			e.printStackTrace();
		}

		cmbCliente.setItems(clientesObs);
		
		cmbCliente.setConverter(new StringConverter<Cliente>() {
            @Override
            public String toString(Cliente cliente) {
                if (cliente == null) {
                    return "";
                }
                return cliente.getNombre();
            }

            @Override
            public Cliente fromString(String string) {
                if (string == null || string.isEmpty()) {
                    return null;
                }
                return clientesObs.stream()
                        .filter(c -> c.getNombre().startsWith(string))
                        .findFirst()
                        .orElse(null);
            }
        });

        // 1. Vincular la selección del ComboBox al cliente actual
        cmbCliente.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                clienteActual.set(newVal);
            }
        });

        // 2. REGLA DE VISIBILIDAD DEL BOTÓN
        btnFinalizarPedido.visibleProperty().bind(
            clienteActual.isNotNull() // Tiene que haber un cliente
            .and(estado.isNotNull()) // Tiene que haber un estado
            .and(Bindings.isNotEmpty(tablaServicios.getItems())) // La tabla no puede estar vacía
        );

        // 3. Ocultar el espacio vacío que deja el botón al desaparecer
        btnFinalizarPedido.managedProperty().bind(btnFinalizarPedido.visibleProperty());
	}

	@FXML
	private void abrirVentanaNuevoCliente() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("cliente.fxml"));
			Parent root = loader.load();
			SecondaryController controllerHijo = loader.getController();
			controllerHijo.setControladorPrincipal(this);
			
			Stage stage = new Stage();
			stage.setTitle("Clientes");
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
			
			Stage stage = new Stage();
			stage.setTitle("Servicio");
			stage.initModality(Modality.APPLICATION_MODAL);
			stage.setScene(new Scene(root));
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
	        
	        // CORRECCIÓN: El controlador debe ser de la clase PedidosController
	        PedidosController controller = loader.getController(); 
	        
	        // Asumiendo que has creado este método en PedidosController.java
	        // controller.setControladorPrincipal(this); 
	        
	        Stage stage = new Stage();
	        stage.setTitle("Búsqueda de Tickets");
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
		this.listaServicios.clear();
        this.estado.set(EstadoPedido.Sin_Pagar);
        this.chkTarjeta.setSelected(false);
		fechaPedido.setValue(null);
		tablaServicios.getItems().clear();
		estableceFecha();
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
		if (clienteActual.get() == null || listaServicios.isEmpty() || estado.get() == null) {
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
			
			for(Servicio servicioActual: listaServicios) {
				precio += servicioActual.getPrecio();
			}
			
			pedido.setPrecio(precio);
			
			pedidoDAO.crearEntidad(pedido);
			
			int idDetalleContador = detalleDao.obtenerUltimoIdentificador();
			for (Servicio s : listaServicios) {
				idDetalleContador++;
				Detalle detalle = new Detalle();
				detalle.setIdentificador(idDetalleContador);
				detalle.setPedido(pedido);
				detalle.setServicio(s);
				detalle.setPrecioUnitario(s.getPrecio());
				detalleDao.crearEntidad(detalle);
			}
			
			handleCancelar();
		} catch (Exception e) {
			e.printStackTrace();
		}
		estableceFecha();
	}
	
	@FXML
	private void manejarDobleClickDia(MouseEvent event) {
		
	}

	public void recibirDatosServicio(Servicio servicio) {
		this.listaServicios.add(servicio);
		tablaServicios.getItems().add(servicio);
	}
	
	public void estableceFecha() {
	    Label[] labelsDias = {primerDia /*, segundoDia, tercerDia, cuartoDia, quintoDia, sextoDia */};
	    Label[] labelsFechas = {primerDiaFecha /*, segundoDiaFecha, tercerDiaFecha... */};
	    Label[] labelsPedidos = {primerDiaPedidos /*, segundoDiaPedidos... */};
	    
	    DetalleDaoHBNT detalleDao = new DetalleDaoHBNT();
	    
	    LocalDate fechaBase = LocalDate.now();
	    if (fechaBase.getDayOfWeek() == DayOfWeek.SATURDAY) {
	        fechaBase = fechaBase.plusDays(2);
	    }
	    this.fechaPedido.setValue(LocalDate.now().plusDays(1));
	    
	    for (int i = 0; i < labelsDias.length; i++) {
	        LocalDate fechaCalculada = fechaBase.plusDays(i);
	        
	        String nombreDia = fechaCalculada.getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("es", "ES"));
	        
	        labelsDias[i].setText(nombreDia.toUpperCase());
	        labelsFechas[i].setText(fechaCalculada.toString());
	        labelsPedidos[i].setText(/*fechaBase.plusDays(i)*/"");
	    }
	}
}