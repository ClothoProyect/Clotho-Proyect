package org.openjfx.clotho.proy.dao.hbnt;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.SelectionQuery;
import org.openjfx.clotho.proy.dao.CategoriaDAO;
import org.openjfx.clotho.proy.exception.ProyectoClothoException;
import org.openjfx.clotho.proy.gestor.GestorSesionesHibernate;
import org.openjfx.clotho.proy.vo.Categoria;

public class CategoriaDaoHBNT implements CategoriaDAO {

	@Override
	public Categoria obtenerEntidadPorClave(Integer clave) throws ProyectoClothoException {
		Categoria entidad = null;
		try (Session sesion = GestorSesionesHibernate.getSession();) {
			entidad = sesion.find(Categoria.class, clave);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return entidad;
	}

	@Override
	public Categoria obtenerEntidadPorNombre(Categoria nombre) throws ProyectoClothoException {
		Categoria entidad = null;
		String sentenciaHQL = "SELECT b FROM Categoria b where b.nombre like '" + nombre + "'";
		try (Session sesion = GestorSesionesHibernate.getSession();) {
			SelectionQuery<Categoria> sentenciaConsulta = sesion.createSelectionQuery(sentenciaHQL, Categoria.class);
			entidad = sentenciaConsulta.getSingleResultOrNull();
		} catch (Exception e) {
			throw new ProyectoClothoException(new Exception("No se ha encontrado ningun registro en la Categoria de datos"), getClass(), ProyectoClothoException.ERROR_CONSULTA);
		}
		return entidad;
	}

	@Override
	public List<Categoria> obtenerListaTodasEntidades() throws ProyectoClothoException {
		List<Categoria> lista = null;
		String sentenciaHQL = "SELECT b FROM Categoria b";
		try (Session sesion = GestorSesionesHibernate.getSession();) {

			SelectionQuery<Categoria> sentenciaConsulta = sesion.createSelectionQuery(sentenciaHQL, Categoria.class);
			lista = sentenciaConsulta.getResultList();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return lista;
	}

	@Override
	public void crearEntidad(Categoria entidad) throws ProyectoClothoException {
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
	public void actualizarEntidad(Categoria entidad) throws ProyectoClothoException {
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

			Categoria Categoria = sesion.find(Categoria.class, clave);
			sesion.remove(Categoria);

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
