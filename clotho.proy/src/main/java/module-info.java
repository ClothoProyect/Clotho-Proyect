module org.openjfx.clotho.proy {
    requires transitive javafx.controls;
    requires javafx.fxml;
	requires org.hibernate.orm.core;
	requires jakarta.persistence;
	requires thymeleaf;
	requires java.desktop;
    requires openhtmltopdf.core;
    requires openhtmltopdf.pdfbox;
    

    opens org.openjfx.clotho.proy to javafx.fxml;
    opens org.openjfx.clotho.proy.vo;
    opens org.openjfx.clotho.proy.documentos to thymeleaf;
    
    exports org.openjfx.clotho.proy;
    
}