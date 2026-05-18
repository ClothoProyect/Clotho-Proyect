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

		// Fuerza a Thymeleaf a no borrar las barras de cierre
		resolver.setTemplateMode("XML");

		this.templateEngine = new TemplateEngine();
		this.templateEngine.setTemplateResolver(resolver);
	}

	// Cambiamos el parámetro 'outputPath' por 'nombreArchivo' para que sea más
	// descriptivo
	public void generatePdf(String templateName, Map<String, Object> data, String nombreArchivo) {
		try {
			// 1. Obtener la ruta de la carpeta personal del usuario del sistema operativo
			String rutaUsuario = System.getProperty("user.home");

			// 2. Construimos la ruta apuntando a Descargas/facturasZYP
			Path directorioFacturas = Paths.get(rutaUsuario, "Downloads", "facturasZYP");

			// 3. Si la carpeta "facturasZYP" no existe en Descargas, la creamos
			// automáticamente
			if (!Files.exists(directorioFacturas)) {
				Files.createDirectories(directorioFacturas);
			}

			// 4. Combinamos la ruta de la carpeta con el nombre del archivo final
			Path rutaCompleta = directorioFacturas.resolve(nombreArchivo);

			// 5. Abrimos el flujo de escritura directamente en la ruta de destino final
			try (OutputStream os = new FileOutputStream(rutaCompleta.toFile())) {

				String ruta = "/templates/" + templateName + ".html";
				InputStream is = getClass().getResourceAsStream(ruta);

				if (is == null) {
					throw new RuntimeException("¡Error Crítico! Java no encuentra el archivo en: " + ruta);
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

				System.out.println("PDF generado con éxito en la ruta: " + rutaCompleta.toString());
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Error al generar el PDF: " + e.getMessage());
		}
	}
}