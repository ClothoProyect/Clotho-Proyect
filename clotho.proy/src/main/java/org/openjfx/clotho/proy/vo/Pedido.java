package org.openjfx.clotho.proy.vo;

import java.sql.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
	private Date fecha;

	@Column(name = "precio", nullable = true)
	private float precio;

	/* Dominio : "Retirado", "Sin Pagar", "Pagado"*/
	@Column(name = "estado", nullable = true)
	private String estado;

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

	public Date getFecha() {
		return fecha;
	}

	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}

	public float getPrecio() {
		return precio;
	}

	public void setPrecio(float precio) {
		this.precio = precio;
	}

	public String getEstado() {
		return estado;
	}

	public void setEstado(String estado) {
		this.estado = estado;
	}

	public Cliente getCliente() {
		return cliente;
	}

	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}
}
