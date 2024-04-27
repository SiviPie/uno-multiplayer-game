module am.uno {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires com.almasb.fxgl.all;

    opens am.server to javafx.fxml;
    exports am.server;

    opens am.client to javafx.fxml;
    exports am.client;
}