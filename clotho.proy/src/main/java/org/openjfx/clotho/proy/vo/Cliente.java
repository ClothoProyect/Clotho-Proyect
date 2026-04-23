package org.openjfx.clotho.proy.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "T_CLIENTE")
public class Cliente {
	@Id
	@Column(name = "identificador", nullable = true)
	private int identificador;
	
	@Column(name = "nombre", length = 70, nullable = true)
	private String nombre;
	
	@Column(name = "apellidos", length = 70)
	private String apellidos;

	@Column(name = "email", length = 100, nullable = false)
	private String email;
	
	@Column(name = "telefono", length = 100, nullable = false)
	private String telefono;

	@Column(name = "cif", length = 70, nullable = true)
	private String cif;

	@Column(name = "direccion", length = 70, nullable = false)
	private String direccion;

	@Column(name = "codigo_postal", length = 7, nullable = true)
	private String codigoPostal;

	@Column(name = "notas_adicionales", length = 70, nullable = true)
	private String notasAdicionales;
	
	public Cliente() {
	}

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

	public String getApellidos() {
		return apellidos;
	}

	public void setApellidos(String apellidos) {
		this.apellidos = apellidos;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getTelefono() {
		return telefono;
	}

	public void setTelefono(String telefono) {
		this.telefono = telefono;
	}

	public String getCif() {
		return cif;
	}

	public void setCif(String cif) {
		this.cif = cif;
	}

	public String getDireccion() {
		return direccion;
	}

	public void setDireccion(String direccion) {
		this.direccion = direccion;
	}

	public String getCodigoPostal() {
		return codigoPostal;
	}

	public void setCodigoPostal(String codigoPostal) {
		this.codigoPostal = codigoPostal;
	}

	public String getNotasAdicionales() {
		return notasAdicionales;
	}

	public void setNotasAdicionales(String notasAdicionales) {
		this.notasAdicionales = notasAdicionales;
	}
}
