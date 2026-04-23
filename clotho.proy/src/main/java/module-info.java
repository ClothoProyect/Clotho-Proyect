module org.openjfx.clotho.proy {
    requires transitive javafx.controls;
    requires javafx.fxml;
	requires org.hibernate.orm.core;
	requires jakarta.persistence;

    opens org.openjfx.clotho.proy to javafx.fxml;
    exports org.openjfx.clotho.proy;
}
