package org.openjfx.clotho.proy.vo;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class DetalleID implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@Column(name = "identificador_pedido")
	private int identificadorPedido;
	
	@Column(name = "identificador_servicio")
	private int identificadorServicio;
	
}
