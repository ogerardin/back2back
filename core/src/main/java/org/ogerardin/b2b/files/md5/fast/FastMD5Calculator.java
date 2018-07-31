package org.ogerardin.b2b.files.md5.fast;

import com.twmacinta.util.MD5;
import org.ogerardin.b2b.files.md5.HashProviderInputStream;
import org.ogerardin.b2b.files.md5.InputStreamMD5Calculator;
import org.ogerardin.b2b.files.md5.ByteArrayMD5Calculator;
import org.ogerardin.b2b.files.md5.MD5UpdatingInputStreamProvider;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

/**
 * MD5 hash calculator using http://www.twmacinta.com/myjava/fast_md5.php via https://github.com/joyent/java-fast-md5
 */
@Component
public class FastMD5Calculator implements ByteArrayMD5Calculator, InputStreamMD5Calculator, MD5UpdatingInputStreamProvider {

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


    @Override
    public HashProviderInputStream md5UpdatingInputStream(InputStream inputStream) {
        return new FastMd5DigestInputStream(inputStream);
    }
}
