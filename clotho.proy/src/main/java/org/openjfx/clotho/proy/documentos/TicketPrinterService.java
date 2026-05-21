package org.openjfx.clotho.proy.documentos;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;

import org.openjfx.clotho.proy.vo.Cliente;
import org.openjfx.clotho.proy.vo.Detalle;
import org.openjfx.clotho.proy.vo.Pedido;

public class TicketPrinterService {

	private static final String NOMBRE_IMPRESORA = "EPSON TM-U220 ReceiptE4";

	// Comandos ESC/POS
	private static final byte[] INIT = { 0x1B, 0x40 };
	private static final byte[] BOLD_ON = { 0x1B, 0x45, 1 };
	private static final byte[] BOLD_OFF = { 0x1B, 0x45, 0 };
	private static final byte[] ALINEAR_CENTRO = { 0x1B, 0x61, 1 };
	private static final byte[] ALINEAR_IZQ = { 0x1B, 0x61, 0 };
	private static final byte[] ALINEAR_DER = { 0x1B, 0x61, 2 };
	private static final byte[] TAMANYO_NORMAL = { 0x1B, 0x21, 0x00 };
	private static final byte[] DOBLE_ALTO = { 0x1B, 0x21, 0x10 };
	private static final byte[] AVANCE_Y_CORTE = { 0x1B, 0x64, 0x07, 0x1D, 0x56, 1 };

	public void imprimir(Pedido pedido, List<Detalle> detalles, boolean esCompleto) {
		PrintService service = buscarImpresora(NOMBRE_IMPRESORA);

		if (service != null) {
			try {
				byte[] bytesAImprimir = generarSecuenciaDeImpresion(pedido, detalles, esCompleto);

				DocFlavor flavor = DocFlavor.BYTE_ARRAY.AUTOSENSE;
				Doc doc = new SimpleDoc(bytesAImprimir, flavor, null);

				DocPrintJob job = service.createPrintJob();
				job.print(doc, null);

			} catch (PrintException | IOException e) {
				System.err.println("Excepción crítica durante la impresión: " + e.getMessage());
			}
		} else {
			System.err.println("Error: No se detectó la impresora '" + NOMBRE_IMPRESORA + "'.");
		}
	}

	private PrintService buscarImpresora(String nombre) {
		PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
		for (PrintService service : services) {
			if (service.getName().equalsIgnoreCase(nombre)) {
				return service;
			}
		}
		return null;
	}

	private byte[] generarSecuenciaDeImpresion(Pedido pedido, List<Detalle> detalles, boolean esCompleto) throws IOException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();

		int copias = esCompleto ? 2 : 1;

		for (int i = 0; i < copias; i++) {
			escribirTicketPrincipal(buffer, pedido, detalles);
			// Horario del local
			buffer.write(TAMANYO_NORMAL);
			buffer.write(ALINEAR_CENTRO);
			buffer.write("LUNES/VIERNES   10:00HS - 14:00HS\n".getBytes());
			buffer.write(ALINEAR_DER);
			buffer.write("15:00HS - 20:00HS\n".getBytes());
			buffer.write(ALINEAR_IZQ);
			buffer.write("SABADO         10:00HS - 14:00HS\n".getBytes());
			buffer.write(ALINEAR_CENTRO);
			buffer.write(BOLD_ON);
			buffer.write("HORARIO DE RETIRADA DESDE 17HS\n".getBytes());
			buffer.write(BOLD_OFF);
			
			// Empuja el ticket completo fuera de la máquina y lo corta
			buffer.write(AVANCE_Y_CORTE);
		}

		if (esCompleto) {
			escribirMarcas(buffer, pedido, detalles);
		}

		return buffer.toByteArray();
	}

	private void escribirTicketPrincipal(ByteArrayOutputStream buffer, Pedido pedido, List<Detalle> detalles) throws IOException {
		// Informacion del local
		buffer.write(INIT);
		buffer.write(ALINEAR_CENTRO);
		buffer.write(DOBLE_ALTO);
		buffer.write(BOLD_ON);
		buffer.write("ZYP SASTRERIA ARREGLOS DE ROPA\n".getBytes());
		buffer.write(BOLD_OFF);
		buffer.write(TAMANYO_NORMAL);
		buffer.write("CALLE CLAUDIO COELLO 109\n".getBytes());
		buffer.write("614998004 - 634873060\n".getBytes());
		buffer.write("zypsastreriaclaucoe@gmail.com\n".getBytes());
		buffer.write(ALINEAR_CENTRO);
		buffer.write(BOLD_ON);
		buffer.write("--------------------------------\n".getBytes());
		buffer.write(BOLD_OFF);

		// Informacion de la hora y fecha de impresión
		buffer.write(ALINEAR_IZQ);
		buffer.write(DOBLE_ALTO);
		buffer.write((String.valueOf(pedido.getCodigoPedido())).getBytes());
		buffer.write((" Del: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + "\n").getBytes());

		Cliente cliente = pedido.getCliente();
		String nombreCliente = (cliente != null && cliente.getNombreCompleto() != null) ? cliente.getNombreCompleto() : "Contado";
		buffer.write((nombreCliente + "\n\n").getBytes());
		buffer.write(TAMANYO_NORMAL);

		buffer.write(ALINEAR_CENTRO);
		float total = 0f;
		for (Detalle d : detalles) {
			String nombreSrv = d.getServicio().getNombre();
			String precio = String.format(new Locale("es", "ES"), "%.2f EUR", d.getPrecioUnitario()); 
			int numEspacios = 32 - nombreSrv.length() - precio.length();
			if (numEspacios < 1) numEspacios = 1;

			// Relleno de espacio en medio dinamico
			buffer.write((nombreSrv).getBytes());
			for (int i = 0; i < numEspacios; i++) {
				buffer.write((" ").getBytes());
			}
			buffer.write((precio + "\n").getBytes());

			total += d.getPrecioUnitario();
		}

		String textTotal = "TOTAL(IVA INCLUIDO): ";
		String textTotalPrecio = String.format(new Locale("es", "ES"), "%.2f EUR", total);

		int numEspacios = 32 - textTotal.length() - textTotalPrecio.length();
		if (numEspacios < 1) numEspacios = 1;

		
		// Relleno de espacio en medio dinamico
		buffer.write((textTotal).getBytes());
		for (int i = 0; i < numEspacios; i++) {
			buffer.write((" ").getBytes());
		}
		buffer.write(BOLD_ON);
		buffer.write((textTotalPrecio + "\n\n").getBytes());
		buffer.write(DOBLE_ALTO);
		buffer.write(("  " + pedido.getEstado().toString() + "\n\n").getBytes());
		buffer.write(BOLD_OFF);

		buffer.write(("RETIRAR ").getBytes());
		buffer.write(DOBLE_ALTO);
		buffer.write(BOLD_ON);
		// Fecha en la que el ticket se asigna a retirar
		buffer.write((pedido.getFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + "\n").getBytes());
		buffer.write(BOLD_OFF);
	}

	private void escribirMarcas(ByteArrayOutputStream buffer, Pedido pedido, List<Detalle> detalles) throws IOException {
		Cliente cliente = pedido.getCliente();
		String nombreCliente = (cliente != null && cliente.getNombreCompleto() != null) ? cliente.getNombreCompleto() : "Contado";
		// Variable que aumenta con la cantidad de registros en detalle
		int num = 1;
		
		for (Detalle d : detalles) {
			buffer.write(INIT);
			String codNumero = String.valueOf(pedido.getCodigoPedido()) + "." + String.valueOf(num++) + "\n";
			
			buffer.write(DOBLE_ALTO);
			buffer.write((nombreCliente + "\n").getBytes());
			buffer.write(ALINEAR_IZQ);
			buffer.write(codNumero.getBytes());

			buffer.write(TAMANYO_NORMAL);
			buffer.write(ALINEAR_CENTRO);
			
			String nombreSrv = d.getServicio().getNombre();
			String precio = String.format(new Locale("es", "ES"), "%.2f EUR", d.getPrecioUnitario());
			int numEspacios = 32 - nombreSrv.length() - precio.length();
			if (numEspacios < 1) numEspacios = 1;
			
			buffer.write((nombreSrv).getBytes());
			
			// Relleno de espacio en medio dinamico
			for (int i = 0; i < numEspacios; i++) {
				buffer.write((" ").getBytes());
			}
			buffer.write((precio + "\n").getBytes());
			buffer.write(((pedido.getFecha().getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("es", "ES")).toUpperCase() + " ").getBytes()));
			buffer.write((pedido.getFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + "\n").getBytes());
			
			// Empuja el detalle fuera de la máquina y lo corta
			buffer.write(AVANCE_Y_CORTE);
		}
	}
}