module org.openjfx.clotho.proy {
    requires transitive javafx.controls;
    requires javafx.fxml;
	requires org.hibernate.orm.core;
	requires jakarta.persistence;

    opens org.openjfx.clotho.proy to javafx.fxml;
    opens org.openjfx.clotho.proy.vo to org.hibernate.orm.core;
    exports org.openjfx.clotho.proy;
}
