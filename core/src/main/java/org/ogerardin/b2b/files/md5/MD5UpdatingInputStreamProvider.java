package org.ogerardin.b2b.files.md5;

import java.io.InputStream;

/**
 * Classes that implement this interface provide a way to obtain a {@link HashProviderInputStream} from a given {@link InputStream}.
 */
public interface MD5UpdatingInputStreamProvider {
    HashProviderInputStream md5UpdatingInputStream(InputStream inputStream);
}
