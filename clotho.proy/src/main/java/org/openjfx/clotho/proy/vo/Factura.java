package org.openjfx.clotho.proy.vo;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "T_FACTURA")
public class Factura {
	@Id
	@Column(name = "identificador", nullable = true)
	private int identificador;
	
	@Column(name = "fecha", nullable = true)
	private LocalDate fecha;
	
	@Column(name = "serial", nullable = true)
	private String serial;
	
	@ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "identificador_pedido", referencedColumnName = "identificador", foreignKey = @ForeignKey(name = "FK_FACTURA_PEDIDO"))
	private Pedido pedido;

	public int getIdentificador() {
		return identificador;
	}

	public void setIdentificador(int identificador) {
		this.identificador = identificador;
	}

	public LocalDate getFecha() {
		return fecha;
	}

	public void setFecha(LocalDate fecha) {
		this.fecha = fecha;
	}

	public Pedido getPedido() {
		return pedido;
	}

	public void setPedido(Pedido pedido) {
		this.pedido = pedido;
	}
}
