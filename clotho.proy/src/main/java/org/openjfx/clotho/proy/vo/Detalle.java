package org.openjfx.clotho.proy.vo;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name = "T_DETALLE")
public class Detalle {
	
	@EmbeddedId
	private DetalleID detalleID;
	
	@Column(name = "precio_unitario", nullable = true)
	private float precioUnitario;
	
	@Column(name = "cantidad", nullable = true)
	private int cantidad;
	
	@ManyToOne(fetch = FetchType.EAGER)
    @MapsId("identificadorPedido")	
    @JoinColumn(name = "identificador_pedido", nullable = false)
	private Pedido pedido;
	
	@ManyToOne(fetch = FetchType.EAGER) 
    @MapsId("identificadorServicio")	
    @JoinColumn(name = "identificador_servicio", nullable = false)
	private Servicio servicio;

	public DetalleID getDetalleID() {
		return detalleID;
	}

	public void setDetalleID(DetalleID detalleID) {
		this.detalleID = detalleID;
	}

	public float getPrecioUnitario() {
		return precioUnitario;
	}

	public void setPrecioUnitario(float precioUnitario) {
		this.precioUnitario = precioUnitario;
	}

	public int getCantidad() {
		return cantidad;
	}

	public void setCantidad(int cantidad) {
		this.cantidad = cantidad;
	}

	public Pedido getPedido() {
		return pedido;
	}

	public void setPedido(Pedido pedido) {
		this.pedido = pedido;
	}

	public Servicio getServicio() {
		return servicio;
	}

	public void setServicio(Servicio servicio) {
		this.servicio = servicio;
	}
}
