package org.openjfx.clotho.proy.dao.hbnt;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.SelectionQuery;
import org.openjfx.clotho.proy.dao.ServicioDAO;
import org.openjfx.clotho.proy.exception.ProyectoClothoException;
import org.openjfx.clotho.proy.gestor.GestorSesionesHibernate;
import org.openjfx.clotho.proy.vo.Servicio;

public class ServicioDaoHBNT implements ServicioDAO {

	@Override
	public Servicio obtenerEntidadPorClave(Integer clave) throws ProyectoClothoException {
		Servicio entidad = null;
		try (Session sesion = GestorSesionesHibernate.getSession();) {
			entidad = sesion.find(Servicio.class, clave);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return entidad;
	}

	@Override
	public Servicio obtenerEntidadPorNombre(Servicio nombre) throws ProyectoClothoException {
		Servicio entidad = null;
		String sentenciaHQL = "SELECT b FROM Servicio b where b.nombre like '" + nombre + "'";
		try (Session sesion = GestorSesionesHibernate.getSession();) {
			SelectionQuery<Servicio> sentenciaConsulta = sesion.createSelectionQuery(sentenciaHQL, Servicio.class);
			entidad = sentenciaConsulta.getSingleResultOrNull();
		} catch (Exception e) {
			throw new ProyectoClothoException(new Exception("No se ha encontrado ningun registro en la Servicio de datos"), getClass(), ProyectoClothoException.ERROR_CONSULTA);
		}
		return entidad;
	}

	@Override
	public List<Servicio> obtenerListaTodasEntidades() throws ProyectoClothoException {
		List<Servicio> lista = null;
		String sentenciaHQL = "SELECT b FROM Servicio b";
		try (Session sesion = GestorSesionesHibernate.getSession();) {

			SelectionQuery<Servicio> sentenciaConsulta = sesion.createSelectionQuery(sentenciaHQL, Servicio.class);
			lista = sentenciaConsulta.getResultList();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return lista;
	}

	@Override
	public void crearEntidad(Servicio entidad) throws ProyectoClothoException {
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
	public void actualizarEntidad(Servicio entidad) throws ProyectoClothoException {
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

			Servicio Servicio = sesion.find(Servicio.class, clave);
			sesion.remove(Servicio);

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
