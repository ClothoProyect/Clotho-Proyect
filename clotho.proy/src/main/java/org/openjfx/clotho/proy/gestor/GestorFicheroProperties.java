package org.openjfx.clotho.proy.gestor;

import java.io.FileInputStream;
import java.util.Properties;

public class GestorFicheroProperties {
	private static final String RUTA_FICHERO_CONFIG = "config/config.properties";
	private static Properties properties;

	private GestorFicheroProperties() {
	}

	static {
		properties = new Properties();
		try {
			properties.load(new FileInputStream(RUTA_FICHERO_CONFIG));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String getValorConfig(String clave) {
		String valor = null;
		valor = properties.getProperty(clave);
		return valor;
	}
}
