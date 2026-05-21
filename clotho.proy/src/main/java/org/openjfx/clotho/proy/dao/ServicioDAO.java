package org.openjfx.clotho.proy.dao;

import org.openjfx.clotho.proy.exception.ProyectoClothoException;
import org.openjfx.clotho.proy.vo.Servicio;

public interface ServicioDAO extends IOperacionesDAOEntidad<Servicio, Integer> {
	long contarDetallesPorServicio(int idServicio) throws ProyectoClothoException;
}