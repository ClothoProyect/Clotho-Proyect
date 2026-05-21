package org.openjfx.clotho.proy.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "T_SERVICIO")
public class Servicio {
	@Id
	@Column(name = "identificador", nullable = true)
	private int identificador;

	@Column(name = "nombre", nullable = true, unique = true)
	private String nombre;

	@Column(name = "precio_estandar", nullable = false)
	private float precioEstandar;

	@Column(name = "activo", nullable = false)
	private boolean activo = true;

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

	public float getPrecioEstandar() {
		return precioEstandar;
	}

	public void setPrecioEstandar(float precioEstandar) {
		this.precioEstandar = precioEstandar;
	}

	public boolean isActivo() {
		return activo;
	}

	public void setActivo(boolean activo) {
		this.activo = activo;
	}

	@Override
	public String toString() {
		return this.nombre;
	}
}