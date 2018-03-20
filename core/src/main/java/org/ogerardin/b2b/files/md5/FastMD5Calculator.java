package org.ogerardin.b2b.files.md5;

import com.twmacinta.util.MD5;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

/**
 * MD5 hash calculator using http://www.twmacinta.com/myjava/fast_md5.php via https://github.com/joyent/java-fast-md5
 */
@Component
public class FastMD5Calculator implements MD5Calculator, StreamingMd5Calculator {

    @Override
    public byte[] md5Hash(byte[] bytes) {
        MD5 md5 = new MD5();
        md5.Update(bytes);
        return md5.Final();
    }


    @Override
    public byte[] md5Hash(InputStream is) throws IOException {
        MD5 md5 = new MD5();
        byte[] buffer = new byte[1024];
        for(int read = is.read(buffer, 0, 1024); read > -1; read = is.read(buffer, 0, 1024)) {
            md5.Update(buffer, 0, read);
        }
        return md5.Final();
    }
}
