package org.ogerardin.b2b.hash;

import org.ogerardin.b2b.hash.DigestingInputStream;

import java.io.InputStream;

/**
 * Classes that implement this interface provide a way to obtain a {@link DigestingInputStream} from a given {@link InputStream}.
 */
public interface DigestingInputStreamProvider {

    DigestingInputStream digestingInputStream(InputStream inputStream);

}
