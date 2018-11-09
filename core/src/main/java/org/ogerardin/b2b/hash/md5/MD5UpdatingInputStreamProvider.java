package org.ogerardin.b2b.hash.md5;

import org.ogerardin.b2b.hash.DigestingInputStream;

import java.io.InputStream;

/**
 * Classes that implement this interface provide a way to obtain a {@link Di} from a given {@link InputStream}.
 */
public interface MD5UpdatingInputStreamProvider {

    DigestingInputStream md5UpdatingInputStream(InputStream inputStream);

}
