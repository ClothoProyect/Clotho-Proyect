package org.openjfx.clotho.proy.dao;

import java.time.LocalDate;
import java.util.List;

import org.openjfx.clotho.proy.exception.ProyectoClothoException;
import org.openjfx.clotho.proy.vo.Detalle;

public interface DetalleDAO extends IOperacionesDAOEntidad<Detalle, Integer> {

	List<Detalle> obtenerDetallesPorPedido(int idPedido) throws ProyectoClothoException;

	float obtenerIngresosPorDia(LocalDate fecha) throws ProyectoClothoException;
	int obtenerPedidosPorDia(LocalDate fecha) throws ProyectoClothoException;
	int obtenerPrendasPorDia(LocalDate fecha) throws ProyectoClothoException;
	float obtenerMediaMensualPorArreglo() throws ProyectoClothoException;
	float obtenerTotalMensual() throws ProyectoClothoException;
	int obtenerCantidadMensualTickets(LocalDate fecha) throws ProyectoClothoException;
}