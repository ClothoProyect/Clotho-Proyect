package org.openjfx.clotho.proy.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "T_CATEGORIA")
public class Categoria {
	@Id
	@Column(name = "identificador", nullable = true)
	private int identificador;
	
	@Column(name = "nombre", nullable = true, unique = true)
	private String nombre;

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
}
