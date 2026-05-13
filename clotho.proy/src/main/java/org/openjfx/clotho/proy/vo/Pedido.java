package org.openjfx.clotho.proy.vo;

import java.time.LocalDate;

import org.openjfx.clotho.proy.vo.enumerate.EstadoPedido;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "T_PEDIDO")
public class Pedido {
	@Id
	@Column(name = "identificador", nullable = true)
	private int identificador;

	@Column(name = "codigo_pedido", nullable = true)
	private int codigoPedido;

	@Column(name = "fecha", nullable = true)
	private LocalDate fecha;
	
	@Column(name = "pago_con_tarjeta", nullable = true)
	private boolean pagoConTarjeta;

	@Column(name = "precio", nullable = true)
	private float precio;

	@Column(name = "estado", nullable = true)
	@Enumerated(EnumType.STRING)
	private EstadoPedido estado;

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
		return precio;
	}

	public void setPrecio(float precio) {
		this.precio = precio;
	}

	public EstadoPedido getEstado() {
		return estado;
	}

	public void setEstado(EstadoPedido estado) {
		this.estado = estado;
	}

	public Cliente getCliente() {
		return cliente;
	}

	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}
}