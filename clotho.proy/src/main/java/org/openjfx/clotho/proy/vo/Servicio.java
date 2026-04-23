package org.openjfx.clotho.proy.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "T_SERVICIO")
public class Servicio {
	@Id
	@Column(name = "identificador", nullable = true)
	private int identificador;

	@Column(name = "nombre", nullable = true, unique = true)
	private String nombre;
	
	@Column(name = "float", nullable = true)
	private float precio;
	
	@ManyToOne
	@JoinColumn(name = "identificador_categoria", referencedColumnName = "identificador", foreignKey = @ForeignKey(name = "FK_SERVICIO_CATEGORIA"))
	private Categoria cliente;

	public int getIdentificador() {
		return identificador;
	}

	public void setIdentificador(int identificador) {
		this.identificador = identificador;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public float getPrecio() {
		return precio;
	}

	public void setPrecio(float precio) {
		this.precio = precio;
	}

	public Categoria getCliente() {
		return cliente;
	}

	public void setCliente(Categoria cliente) {
		this.cliente = cliente;
	}
}
