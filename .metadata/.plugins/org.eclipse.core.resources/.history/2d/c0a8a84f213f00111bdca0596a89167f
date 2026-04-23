package org.openjfx.clotho.proy.dao;

import java.util.List;

public interface IOperacionesDAOEntidad<T, ID> {
	T obtenerEntidadPorClave(ID clave);
	T obtenerEntidadPorNombre(T nombre);
	List<T> obtenerListaTodasEntidades();
	void crearEntidad(T entidad);
	void actualizarEntidad(T entidad);
	void borrarEntidadPorClave(ID clave);
}
