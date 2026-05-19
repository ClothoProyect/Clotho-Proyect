package org.openjfx.clotho.proy;

import java.util.Locale;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class CobroController {
	
	@FXML
	private Label lblImporte;

	private boolean confirmado = false;

	public void setImporte(float importe) {
		lblImporte.setText(String.format(new Locale("es", "ES"), "%.2f €", importe));
	}

	public boolean isConfirmado() {
		return confirmado;
	}

	@FXML
	private void confirmar() {
		confirmado = true;
		cerrarVentana();
	}

	@FXML
	private void cancelar() {
		confirmado = false;
		cerrarVentana();
	}

	private void cerrarVentana() {
		Stage stage = (Stage) lblImporte.getScene().getWindow();
		stage.close();
	}
}