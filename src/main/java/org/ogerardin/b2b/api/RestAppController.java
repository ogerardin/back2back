package org.ogerardin.b2b.api;

import org.ogerardin.b2b.Main;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/app")
public class RestAppController {

    @GetMapping("/version")
    String version() {
        // retrieve version information from MANIFEST.MF
        String implementationVersion = Main.class.getPackage().getImplementationVersion();
        return (implementationVersion != null) ? implementationVersion : "unknown";
    }
}
