package org.openjfx.clotho.proy;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.stage.Stage;

public class AnularController {

    private boolean confirmado = false;
    
    @FXML 
    private void confirmar(ActionEvent e) {
    	confirmado = true;
    	cerrar(e);
    	}
    
    @FXML private void cancelar(ActionEvent e) {
    	confirmado = false;
    	cerrar(e);
    	}
    
    private void cerrar(ActionEvent e) { 
        ((Stage)((Node)e.getSource()).getScene().getWindow()).close(); 
    }
    
    public boolean isConfirmado() {
        return confirmado;
    }
}