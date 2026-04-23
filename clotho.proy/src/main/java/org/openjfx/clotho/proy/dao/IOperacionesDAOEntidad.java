package org.openjfx.clotho.proy.dao;

import java.util.List;

import org.openjfx.clotho.proy.exception.ProyectoClothoException;

public interface IOperacionesDAOEntidad<T, ID> {
	T obtenerEntidadPorClave(ID clave)  throws ProyectoClothoException ;
	T obtenerEntidadPorNombre(T nombre)  throws ProyectoClothoException ;
	List<T> obtenerListaTodasEntidades()  throws ProyectoClothoException ;
	void crearEntidad(T entidad)  throws ProyectoClothoException ;
	void actualizarEntidad(T entidad)  throws ProyectoClothoException ;
	void borrarEntidadPorClave(ID clave)  throws ProyectoClothoException ;
}
