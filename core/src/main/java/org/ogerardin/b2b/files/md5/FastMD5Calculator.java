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

    private static final int BUFFER_SIZE = 1024;

    @Override
    public byte[] md5Hash(byte[] bytes) {
        MD5 md5 = new MD5();
        md5.Update(bytes);
        return md5.Final();
    }


    @Override
    public byte[] md5Hash(InputStream inputStream) throws IOException {
        MD5 md5 = new MD5();
        byte[] buffer = new byte[BUFFER_SIZE];
        for(int read = inputStream.read(buffer, 0, buffer.length); read > -1; read = inputStream.read(buffer, 0, buffer.length)) {
            md5.Update(buffer, 0, read);
        }
        byte[] hash = md5.Final();
        inputStream.close();
        return hash;
    }
}
