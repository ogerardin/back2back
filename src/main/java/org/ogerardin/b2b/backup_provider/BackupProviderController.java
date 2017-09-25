package org.ogerardin.b2b.backup_provider;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController
public class BackupProviderController {

    @RequestMapping("/api/ok")
    public String hello() {
        return "OK";
    }


}
