package org.openjfx.clotho.proy.dao;

import org.openjfx.clotho.proy.exception.ProyectoClothoException;
import org.openjfx.clotho.proy.vo.Cliente;

public interface ClienteDAO extends IOperacionesDAOEntidad<Cliente, Integer> {
	public Long contarTicketsPorCliente(int identificadorCliente)  throws ProyectoClothoException;
}