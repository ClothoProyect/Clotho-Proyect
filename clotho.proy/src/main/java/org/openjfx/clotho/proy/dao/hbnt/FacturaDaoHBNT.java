package org.openjfx.clotho.proy.dao.hbnt;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.hibernate.query.SelectionQuery;
import org.openjfx.clotho.proy.dao.FacturaDAO;
import org.openjfx.clotho.proy.exception.ProyectoClothoException;
import org.openjfx.clotho.proy.gestor.GestorSesionesHibernate;
import org.openjfx.clotho.proy.vo.Factura;

public class FacturaDaoHBNT implements FacturaDAO {

	@Override
	public Factura obtenerEntidadPorClave(Integer clave) throws ProyectoClothoException {
		Factura entidad = null;
		try (Session sesion = GestorSesionesHibernate.getSession();) {
			entidad = sesion.find(Factura.class, clave);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return entidad;
	}

	@Override
	public Factura obtenerEntidadPorNombre(Factura nombre) throws ProyectoClothoException {
		Factura entidad = null;
		return entidad;
	}

	@Override
	public List<Factura> obtenerListaTodasEntidades() throws ProyectoClothoException {
		List<Factura> lista = null;
		String sentenciaHQL = "SELECT b FROM Factura b";
		try (Session sesion = GestorSesionesHibernate.getSession();) {

			SelectionQuery<Factura> sentenciaConsulta = sesion.createSelectionQuery(sentenciaHQL, Factura.class);
			lista = sentenciaConsulta.getResultList();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return lista;
	}

	@Override
	public void crearEntidad(Factura entidad) throws ProyectoClothoException {
		Transaction transaccion = null;
		Session sesion = null;
		
		try {
			sesion = GestorSesionesHibernate.getSession();
			transaccion = sesion.beginTransaction();

			sesion.persist(entidad);

			transaccion.commit();
		} catch (Exception e) {
			if (transaccion != null && transaccion.isActive()) {
				transaccion.rollback();
			}
			e.printStackTrace();
		} finally {
			if (sesion != null) {
				sesion.close();
			}
		}
	}

	@Override
	public void actualizarEntidad(Factura entidad) throws ProyectoClothoException {
		Transaction transaccion = null;
		Session sesion = null;
		
		try {
			sesion = GestorSesionesHibernate.getSession();
			transaccion = sesion.beginTransaction();

			if (!sesion.contains(entidad)) {
				sesion.merge(entidad);
            }

			transaccion.commit();
		} catch (Exception e) {
			if (transaccion != null && transaccion.isActive()) {
				transaccion.rollback();
			}
			e.printStackTrace();
		} finally {
			if (sesion != null) {
				sesion.close();
			}
		}
	}

	@Override
	public void borrarEntidadPorClave(Integer clave) throws ProyectoClothoException {
		Transaction transaccion = null;
		Session sesion = null;
		
		try {
			sesion = GestorSesionesHibernate.getSession();
			transaccion = sesion.beginTransaction();

			Factura factura = sesion.find(Factura.class, clave);
            if (factura != null) {
			    sesion.remove(factura);
            }

			transaccion.commit();
		} catch (Exception e) {
			if (transaccion != null && transaccion.isActive()) {
				transaccion.rollback();
			}
			e.printStackTrace();
		} finally {
			if (sesion != null) {
				sesion.close();
			}
		}
	}

	@Override
	public int obtenerUltimoIdentificador() throws ProyectoClothoException {
		try (Session sesion = GestorSesionesHibernate.getSession();) {
			Integer maxId = sesion.createQuery("select max(f.identificador) from Factura f", Integer.class)
                    .getSingleResult();

			return (maxId == null) ? 0 : maxId;
		} catch (Exception e) {
			throw new ProyectoClothoException(new Exception("Error al obtener el último identificador de Factura"), getClass(), ProyectoClothoException.ERROR_CONSULTA);
		}
	}

	@Override
	public int obtenerUltimoCodigoPedido() throws ProyectoClothoException {
		try (Session sesion = GestorSesionesHibernate.getSession();) {
			Integer maxCodigo = sesion.createQuery("select max(f.codigoFactura) from Factura f", Integer.class)
					.getSingleResult();

			return (maxCodigo == null) ? 0 : maxCodigo;
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new ProyectoClothoException(new Exception("Error al obtener el último código de Factura"), getClass(), ProyectoClothoException.ERROR_CONSULTA);
		}
	}

	@Override
	public boolean confirmarExistenciaTicket(int identificadorPedido) throws ProyectoClothoException {
		boolean existe = false;

		try (Session sesion = GestorSesionesHibernate.getSession();) {
			
			String hql = "SELECT count(f) FROM Factura f WHERE f.pedido.identificador = :idPedido";
			
			Query<Long> query = sesion.createQuery(hql, Long.class);
			query.setParameter("idPedido", identificadorPedido);
			
			Long cantidad = query.uniqueResult();
			
			if (cantidad != null && cantidad > 0) {
				existe = true;
			}
			
		} catch (Exception e) {
			throw new ProyectoClothoException(e, this.getClass(), 2);
		}
		return existe;
	}
	
	@Override
	public Factura obtenerFacturaPorTicket(int identificadorTicket) throws ProyectoClothoException {
		Factura facturaEncontrada = null;

		try (Session sesion = GestorSesionesHibernate.getSession();) {
			String hql = "SELECT f FROM Factura f WHERE f.pedido.identificador = :idTicket";
			
			Query<Factura> query = sesion.createQuery(hql, Factura.class);
			query.setParameter("idTicket", identificadorTicket);
			
			facturaEncontrada = query.uniqueResult();
			
		} catch (Exception e) {
			throw new ProyectoClothoException(e, this.getClass(), 2);
		}
		
		return facturaEncontrada;
	}
}