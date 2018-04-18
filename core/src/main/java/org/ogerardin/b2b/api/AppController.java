package org.ogerardin.b2b.api;

import org.ogerardin.b2b.Main;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/app")
public class AppController {

    @Autowired
    private ApplicationContext appContext;

    @GetMapping("/status")
    String status() {
        return "OK";
    }

    @GetMapping("/version")
    String version() {
        // retrieve version information from MANIFEST.MF
        String implementationVersion = Main.class.getPackage().getImplementationVersion();
        return (implementationVersion != null) ? implementationVersion : "unknown";
    }

    @GetMapping("/shutdown")
    void shutdown() {
        SpringApplication.exit(appContext, (ExitCodeGenerator) () -> 1);
    }

    @GetMapping("/restart")
    void restart() {
        Thread restartThread = new Thread(() -> {
            try {
                Thread.sleep(1000);
                Main.restart();
            } catch (InterruptedException ignored) {
            }
        });
        restartThread.setDaemon(false);
        restartThread.start();
    }
}
