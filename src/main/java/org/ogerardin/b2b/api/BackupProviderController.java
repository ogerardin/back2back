package org.ogerardin.b2b.api;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BackupProviderController {

    @RequestMapping("/api/ok")
    public String hello() {
        return "OK";
    }


}
