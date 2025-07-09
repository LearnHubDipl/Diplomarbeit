package at.learnhub.repository;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import org.h2.tools.Server;

import java.sql.SQLException;

@ApplicationScoped
public class H2ConsoleStarter {

    private Server webServer;

    void onStart(@Observes StartupEvent ev) {
        try {
            webServer = Server.createWebServer("-web", "-webAllowOthers", "-webPort", "8082").start();
            System.out.println("H2 console started and available at http://localhost:8082");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to start H2 console", e);
        }
    }

    void onStop(@Observes io.quarkus.runtime.ShutdownEvent ev) {
        if (webServer != null) {
            webServer.stop();
        }
    }
}

