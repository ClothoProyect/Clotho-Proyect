package org.openjfx.clotho.proy.dao.hbnt;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.SelectionQuery;
import org.openjfx.clotho.proy.dao.DetalleDAO;
import org.openjfx.clotho.proy.exception.ProyectoClothoException;
import org.openjfx.clotho.proy.gestor.GestorSesionesHibernate;
import org.openjfx.clotho.proy.vo.Detalle;

public class DetalleDaoHBNT implements DetalleDAO {

	@Override
	public Detalle obtenerEntidadPorClave(Integer clave) throws ProyectoClothoException {
		Detalle entidad = null;
		try (Session sesion = GestorSesionesHibernate.getSession();) {
			entidad = sesion.find(Detalle.class, clave);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return entidad;
	}

	@Override
	public Detalle obtenerEntidadPorNombre(Detalle nombre) throws ProyectoClothoException {
		Detalle entidad = null;
		String sentenciaHQL = "SELECT b FROM Detalle b where b.nombre like '" + nombre + "'";
		try (Session sesion = GestorSesionesHibernate.getSession();) {
			SelectionQuery<Detalle> sentenciaConsulta = sesion.createSelectionQuery(sentenciaHQL, Detalle.class);
			entidad = sentenciaConsulta.getSingleResultOrNull();
		} catch (Exception e) {
			throw new ProyectoClothoException(new Exception("No se ha encontrado ningun registro en la Detalle de datos"), getClass(), ProyectoClothoException.ERROR_CONSULTA);
		}
		return entidad;
	}

	@Override
	public List<Detalle> obtenerListaTodasEntidades() throws ProyectoClothoException {
		List<Detalle> lista = null;
		String sentenciaHQL = "SELECT b FROM Detalle b";
		try (Session sesion = GestorSesionesHibernate.getSession();) {

			SelectionQuery<Detalle> sentenciaConsulta = sesion.createSelectionQuery(sentenciaHQL, Detalle.class);
			lista = sentenciaConsulta.getResultList();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return lista;
	}

	@Override
	public void crearEntidad(Detalle entidad) throws ProyectoClothoException {
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
	public void actualizarEntidad(Detalle entidad) throws ProyectoClothoException {
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

			Detalle Detalle = sesion.find(Detalle.class, clave);
			sesion.remove(Detalle);

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

}
