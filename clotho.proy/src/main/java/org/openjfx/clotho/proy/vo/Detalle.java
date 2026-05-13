package org.openjfx.clotho.proy.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "T_DETALLE")
public class Detalle {
	
	@Id
	@Column(name = "identificador", nullable = true)
	private int identificador;
	
	@Column(name = "precio_unitario", nullable = true)
	private float precioUnitario;
	
	@ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "identificador_pedido", referencedColumnName = "identificador", foreignKey = @ForeignKey(name = "FK_DETALLE_PEDIDO"))
	private Pedido pedido;
	
	@ManyToOne(fetch = FetchType.EAGER) 
    @JoinColumn(name = "identificador_servicio", referencedColumnName = "identificador", foreignKey = @ForeignKey(name = "FK_DETALLE_SERVICIO"))
	private Servicio servicio;

	public int getIdentificador() {
		return identificador;
	}

	public void setIdentificador(int identificador) {
		this.identificador = identificador;
	}

	public float getPrecioUnitario() {
		return precioUnitario;
	}

	public void setPrecioUnitario(float precioUnitario) {
		this.precioUnitario = precioUnitario;
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