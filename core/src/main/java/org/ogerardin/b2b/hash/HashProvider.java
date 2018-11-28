package org.ogerardin.b2b.hash;

public interface HashProvider extends ByteArrayHashCalculator, InputStreamHashCalculator, DigestingInputStreamProvider {

    String name();

}
