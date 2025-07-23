module sprint.planning {
    requires de.jensd.fx.glyphs.fontawesome;
    requires jakarta.xml.bind;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;

    requires org.glassfish.jaxb.runtime;

    exports com.example.sprintplanning;

    opens com.example.sprintplanning.model to jakarta.xml.bind;
}
