package org.openjfx.clotho.proy.dao;

import org.openjfx.clotho.proy.exception.ProyectoClothoException;
import org.openjfx.clotho.proy.vo.Factura;

public interface FacturaDAO extends IOperacionesDAOEntidad<Factura, Integer> {
	int obtenerUltimoCodigoPedido() throws ProyectoClothoException;
	boolean confirmarExistenciaTicket(int identificador) throws ProyectoClothoException;
	Factura obtenerFacturaPorTicket(int identificadorTicket) throws ProyectoClothoException;
}
