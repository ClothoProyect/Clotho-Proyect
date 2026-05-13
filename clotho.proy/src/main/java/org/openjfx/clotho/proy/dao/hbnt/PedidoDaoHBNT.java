package org.openjfx.clotho.proy.dao.hbnt;

import java.time.LocalDate;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.SelectionQuery;
import org.openjfx.clotho.proy.dao.PedidoDAO;
import org.openjfx.clotho.proy.exception.ProyectoClothoException;
import org.openjfx.clotho.proy.gestor.GestorSesionesHibernate;
import org.openjfx.clotho.proy.vo.Pedido;

public class PedidoDaoHBNT implements PedidoDAO {

	@Override
	public Pedido obtenerEntidadPorClave(Integer clave) throws ProyectoClothoException {
		Pedido entidad = null;
		try (Session sesion = GestorSesionesHibernate.getSession();) {
			entidad = sesion.find(Pedido.class, clave);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return entidad;
	}

	@Override
	public Pedido obtenerEntidadPorNombre(Pedido nombre) throws ProyectoClothoException {
		Pedido entidad = null;
		String sentenciaHQL = "SELECT b FROM Pedido b where b.nombre like '" + nombre + "'";
		try (Session sesion = GestorSesionesHibernate.getSession();) {
			SelectionQuery<Pedido> sentenciaConsulta = sesion.createSelectionQuery(sentenciaHQL, Pedido.class);
			entidad = sentenciaConsulta.getSingleResultOrNull();
		} catch (Exception e) {
			throw new ProyectoClothoException(new Exception("No se ha encontrado ningun registro en la Pedido de datos"), getClass(), ProyectoClothoException.ERROR_CONSULTA);
		}
		return entidad;
	}

	@Override
	public List<Pedido> obtenerListaTodasEntidades() throws ProyectoClothoException {
		List<Pedido> lista = null;
		String sentenciaHQL = "SELECT b FROM Pedido b";
		try (Session sesion = GestorSesionesHibernate.getSession();) {

			SelectionQuery<Pedido> sentenciaConsulta = sesion.createSelectionQuery(sentenciaHQL, Pedido.class);
			lista = sentenciaConsulta.getResultList();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return lista;
	}

	@Override
	public void crearEntidad(Pedido entidad) throws ProyectoClothoException {
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
	public void actualizarEntidad(Pedido entidad) throws ProyectoClothoException {
		Transaction transaccion = null;
		Session sesion = null;
		
		try {
			sesion = GestorSesionesHibernate.getSession();
			transaccion = sesion.beginTransaction();

			if (!sesion.contains(entidad))
				sesion.merge(entidad);

			transaccion.commit();
		}  catch (Exception e) {
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

			Pedido Pedido = sesion.find(Pedido.class, clave);
			sesion.remove(Pedido);

			transaccion.commit();
		}  catch (Exception e) {
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
			Integer maxId = sesion.createQuery("select max(s.identificador) from Pedido s", Integer.class)
                    .getSingleResult();

			return (maxId == null) ? 0 : maxId;
		} catch (Exception e) {
			throw new ProyectoClothoException(new Exception("No se ha encontrado ningun registro en la Pedido de datos"), getClass(), ProyectoClothoException.ERROR_CONSULTA);
		}
	}

	@Override
	public int obtenerUltimoCodigoPedido() throws ProyectoClothoException {
	    Integer ultimoCodigo = 0;
	    int anioActual = LocalDate.now().getYear();
	    String sentenciaHQL = "SELECT MAX(p.codigoPedido) FROM Pedido p WHERE YEAR(p.fecha) = :anioActual";
	    
	    try (Session sesion = GestorSesionesHibernate.getSession()) {
	        ultimoCodigo = sesion.createSelectionQuery(sentenciaHQL, Integer.class)
	                             .setParameter("anioActual", anioActual)
	                             .getSingleResultOrNull();
	        
	        if (ultimoCodigo == null) {
	            return 0;
	        }
	        return ultimoCodigo;
	        
	    } catch (Exception e) {
	        System.err.println("Error en obtenerUltimoCodigoPedido: " + e.getMessage());
	        return 0; 
	    }
	}

	@Override
	public int obtenerCantidadPedidosPorFecha(LocalDate fecha) throws ProyectoClothoException {
		try (Session sesion = GestorSesionesHibernate.getSession();) {
			Integer maxId = sesion.createQuery("select max(s.identificador) from Pedido s", Integer.class)
                    .getSingleResult();

			return (maxId == null) ? 0 : maxId;
		} catch (Exception e) {
			throw new ProyectoClothoException(new Exception("No se ha encontrado ningun registro en la Pedido de datos"), getClass(), ProyectoClothoException.ERROR_CONSULTA);
		}
	}

}