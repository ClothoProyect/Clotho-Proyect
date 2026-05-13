package org.openjfx.clotho.proy.dao;

import java.time.LocalDate;

import org.openjfx.clotho.proy.exception.ProyectoClothoException;
import org.openjfx.clotho.proy.vo.Pedido;

public interface PedidoDAO extends IOperacionesDAOEntidad<Pedido, Integer> {
	int obtenerUltimoCodigoPedido() throws ProyectoClothoException;
	int obtenerCantidadPedidosPorFecha(LocalDate fecha) throws ProyectoClothoException;
}