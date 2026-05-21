package org.openjfx.clotho.proy.vo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.openjfx.clotho.proy.vo.enumerate.EstadoPedido;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "T_PEDIDO")
public class Pedido {
	@Id
	@Column(name = "identificador", nullable = true)
	private int identificador;

	@Column(name = "codigo_pedido", nullable = false)
	private int codigoPedido;

	@Column(name = "fecha", nullable = false)
	private LocalDate fecha;
	
	@Column(name = "pago_con_tarjeta")
	private boolean pagoConTarjeta;

	@Column(name = "precio_total", nullable = false)
	private float precioTotal;

	@Column(name = "estado", nullable = false)
	@Enumerated(EnumType.STRING)
	private EstadoPedido estado;
	
	@OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Detalle> detalles = new ArrayList<>();

	@ManyToOne
	@JoinColumn(name = "identificador_cliente", referencedColumnName = "identificador", foreignKey = @ForeignKey(name = "FK_PEDIDO_CLIENTE"))
	private Cliente cliente;

	public int getIdentificador() {
		return identificador;
	}

	public void setIdentificador(int identificador) {
		this.identificador = identificador;
	}

	public int getCodigoPedido() {
		return codigoPedido;
	}

	public void setCodigoPedido(int codigoPedido) {
		this.codigoPedido = codigoPedido;
	}

	public LocalDate getFecha() {
		return fecha;
	}

	public void setFecha(LocalDate fecha) {
		this.fecha = fecha;
	}

	public boolean isPagoConTarjeta() {
		return pagoConTarjeta;
	}

	public void setPagoConTarjeta(boolean pagoConTarjeta) {
		this.pagoConTarjeta = pagoConTarjeta;
	}

	public float getPrecio() {
		return precioTotal;
	}

	public void setPrecio(float precio) {
		this.precioTotal = precio;
	}

	public EstadoPedido getEstado() {
		return estado;
	}

	public void setEstado(EstadoPedido estado) {
		this.estado = estado;
	}
	
	public float getPrecioTotal() {
		return precioTotal;
	}

	public void setPrecioTotal(float precioTotal) {
		this.precioTotal = precioTotal;
	}

	public List<Detalle> getDetalles() {
		return detalles;
	}

	public void setDetalles(List<Detalle> detalles) {
		this.detalles = detalles;
	}

	public Cliente getCliente() {
		return cliente;
	}

	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}
}