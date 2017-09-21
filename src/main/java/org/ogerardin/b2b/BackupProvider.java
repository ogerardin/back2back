package org.ogerardin.b2b;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController
public class BackupProvider {

    @RequestMapping("/api/ok")
    public String hello() {
        return "OK";
    }


}
