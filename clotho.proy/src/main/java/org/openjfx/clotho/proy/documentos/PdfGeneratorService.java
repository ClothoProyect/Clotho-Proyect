package org.openjfx.clotho.proy.documentos;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.StringTemplateResolver;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PdfGeneratorService {

	private final TemplateEngine templateEngine;

	public PdfGeneratorService() {
		StringTemplateResolver resolver = new StringTemplateResolver();

		// Le dice a Thymeleaf que por favor no borre las barras de cierre
		resolver.setTemplateMode("XML");

		this.templateEngine = new TemplateEngine();
		this.templateEngine.setTemplateResolver(resolver);
	}

	public void generatePdf(String templateName, Map<String, Object> data, String nombreArchivo) {
		try {
			// Conseguir la ruta de la carpeta personal del usuario
			String rutaUsuario = System.getProperty("user.home");

			// La ruta donde se guarda apunta a Escritorio/facturasZYP
			Path carpetaFacturas = Paths.get(rutaUsuario, "Desktop", "facturasZYP");

			// En caso de no existir la carpeta "facturasZYP" la crea
			if (!Files.exists(carpetaFacturas)) {
				Files.createDirectories(carpetaFacturas);
			}

			// Fusionamos la ruta de la carpeta con el nombre del archivo
			Path rutaCompleta = carpetaFacturas.resolve(nombreArchivo);

			// Se abre la ruta de escritura
			try (OutputStream os = new FileOutputStream(rutaCompleta.toFile())) {

				String ruta = "/templates/" + templateName + ".html";
				InputStream is = getClass().getResourceAsStream(ruta);

				if (is == null) {
					throw new RuntimeException("Error; Java no encuentra el archivo en: " + ruta);
				}

				String templateContent = new String(is.readAllBytes(), StandardCharsets.UTF_8);

				Context context = new Context();
				context.setVariables(data);
				String renderedHtml = templateEngine.process(templateContent, context);

				PdfRendererBuilder builder = new PdfRendererBuilder();
				builder.useFastMode();
				builder.withHtmlContent(renderedHtml, null);
				builder.toStream(os);
				builder.run();
				// Se finaliza la escritura del PDF
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Error al generar el PDF: " + e.getMessage());
		}
	}
}