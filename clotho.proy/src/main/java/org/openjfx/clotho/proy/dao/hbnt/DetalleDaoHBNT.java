package org.openjfx.clotho.proy.dao.hbnt;

import java.time.LocalDate;
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
			throw new ProyectoClothoException(
					new Exception("No se ha encontrado ningun registro en la Detalle de datos"), getClass(),
					ProyectoClothoException.ERROR_CONSULTA);
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

			Detalle Detalle = sesion.find(Detalle.class, clave);
			sesion.remove(Detalle);

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
			Integer maxId = sesion.createQuery("select max(s.identificador) from Detalle s", Integer.class)
					.getSingleResult();

			return (maxId == null) ? 0 : maxId;
		} catch (Exception e) {
			throw new ProyectoClothoException(
					new Exception("No se ha encontrado ningun registro en la Detalle de datos"), getClass(),
					ProyectoClothoException.ERROR_CONSULTA);
		}
	}

	@Override
	public int obtenerPedidosPorDia(LocalDate fecha) throws ProyectoClothoException {
		try (Session sesion = GestorSesionesHibernate.getSession();) {
			String sentenciaHQL = "SELECT count(distinct d.pedido.identificador) FROM Detalle d WHERE d.pedido.fecha = :fecha";

			Long cantidad = sesion.createQuery(sentenciaHQL, Long.class).setParameter("fecha", fecha).getSingleResult();

			return cantidad != null ? cantidad.intValue() : 0;
		} catch (Exception e) {
			throw new ProyectoClothoException(new Exception("Error al obtener la cantidad de pedidos por día"),
					getClass(), ProyectoClothoException.ERROR_CONSULTA);
		}
	}

	@Override
	public int obtenerPrendasPorDia(LocalDate fecha) throws ProyectoClothoException {
		try (Session sesion = GestorSesionesHibernate.getSession();) {
			// Aquí no usamos distinct porque queremos contar cada prenda individualmente
			String sentenciaHQL = "SELECT count(d.identificador) FROM Detalle d WHERE d.pedido.fecha = :fecha";

			Long cantidad = sesion.createQuery(sentenciaHQL, Long.class).setParameter("fecha", fecha).getSingleResult();

			return cantidad != null ? cantidad.intValue() : 0;
		} catch (Exception e) {
			throw new ProyectoClothoException(new Exception("Error al obtener la cantidad de prendas por día"),
					getClass(), ProyectoClothoException.ERROR_CONSULTA);
		}
	}

	@Override
	public float obtenerIngresosPorDia(LocalDate fecha) throws ProyectoClothoException {
		try (Session sesion = GestorSesionesHibernate.getSession();) {
			// Sumamos el precio unitario de todos los detalles de ese día
			String sentenciaHQL = "SELECT sum(d.precioUnitario) FROM Detalle d WHERE d.pedido.fecha = :fecha";

			// CAMBIO CLAVE: Usamos Double.class porque Hibernate devuelve Double al hacer
			// sum()
			Double ingresos = sesion.createQuery(sentenciaHQL, Double.class).setParameter("fecha", fecha)
					.getSingleResult();

			// Si es null (no hay ventas ese día) devolvemos 0, si no, lo convertimos a
			// float
			return ingresos != null ? ingresos.floatValue() : 0f;

		} catch (Exception e) {
			// Imprimimos la traza original para ver el fallo real en la consola si ocurre
			e.printStackTrace();
			throw new ProyectoClothoException(new Exception("Error al obtener los ingresos por día"), getClass(),
					ProyectoClothoException.ERROR_CONSULTA);
		}
	}

	@Override
	public List<Detalle> obtenerDetallesPorPedido(int idPedido) throws ProyectoClothoException {
		List<Detalle> lista = null;

		// Buscamos todos los detalles (d) cuyo pedido asociado tenga el identificador
		// que le pasamos
		String sentenciaHQL = "SELECT d FROM Detalle d WHERE d.pedido.identificador = :idPedido";

		try (Session sesion = GestorSesionesHibernate.getSession();) {

			SelectionQuery<Detalle> sentenciaConsulta = sesion.createSelectionQuery(sentenciaHQL, Detalle.class);
			sentenciaConsulta.setParameter("idPedido", idPedido);

			lista = sentenciaConsulta.getResultList();

		} catch (Exception e) {
			e.printStackTrace();
			throw new ProyectoClothoException(new Exception("Error al obtener los detalles del pedido"), getClass(),
					ProyectoClothoException.ERROR_CONSULTA);
		}

		return lista;
	}

	@Override
	public float obtenerMediaMensualPorArreglo() throws ProyectoClothoException {
		try (Session sesion = GestorSesionesHibernate.getSession();) {
			LocalDate fechaActual = LocalDate.now();
			
			// Usamos avg() para sacar la media matemática directamente en SQL
			String sentenciaHQL = "SELECT avg(d.precioUnitario) FROM Detalle d "
					+ "WHERE month(d.pedido.fecha) = :mes AND year(d.pedido.fecha) = :anio";
			
			// Hibernate devuelve las medias (avg) siempre en Double por precisión
			Double media = sesion.createQuery(sentenciaHQL, Double.class)
					.setParameter("mes", fechaActual.getMonthValue())
					.setParameter("anio", fechaActual.getYear())
					.getSingleResult();

			return media != null ? (float) Math.round(media * 100.0) / 100.0f : 0f;
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new ProyectoClothoException(new Exception("Error al obtener la media mensual por arreglo"), getClass(), ProyectoClothoException.ERROR_CONSULTA);
		}
	}

	@Override
	public float obtenerTotalMensual() throws ProyectoClothoException {
		try (Session sesion = GestorSesionesHibernate.getSession();) {
			LocalDate fechaActual = LocalDate.now();
			
			String sentenciaHQL = "SELECT sum(d.precioUnitario) FROM Detalle d "
					+ "WHERE month(d.pedido.fecha) = :mes AND year(d.pedido.fecha) = :anio";
			
			Double total = sesion.createQuery(sentenciaHQL, Double.class)
					.setParameter("mes", fechaActual.getMonthValue())
					.setParameter("anio", fechaActual.getYear())
					.getSingleResult();

			return total != null ? total.floatValue() : 0f;
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new ProyectoClothoException(new Exception("Error al obtener el total mensual de ingresos"), getClass(), ProyectoClothoException.ERROR_CONSULTA);
		}
	}

	@Override
	public int obtenerCantidadMensualTickets(LocalDate fecha) throws ProyectoClothoException {
		try (Session sesion = GestorSesionesHibernate.getSession();) {
			// Usamos count(distinct) para no contar el mismo ticket dos veces si tiene varias prendas
			String sentenciaHQL = "SELECT count(distinct d.pedido.identificador) FROM Detalle d "
					+ "WHERE month(d.pedido.fecha) = :mes AND year(d.pedido.fecha) = :anio";
			
			Long cantidad = sesion.createQuery(sentenciaHQL, Long.class)
					.setParameter("mes", fecha.getMonthValue()) // Usamos el mes de la fecha que pasas por parámetro
					.setParameter("anio", fecha.getYear())
					.getSingleResult();

			return cantidad != null ? cantidad.intValue() : 0;
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new ProyectoClothoException(new Exception("Error al obtener la cantidad mensual de tickets"), getClass(), ProyectoClothoException.ERROR_CONSULTA);
		}
	}
}