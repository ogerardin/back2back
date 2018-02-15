package org.ogerardin.b2b.files.md5;

import java.nio.ByteBuffer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public interface MD5Calculator {

    byte[] md5Hash(byte[] bytes);

    default String hexMd5Hash(byte[] bytes) {
        byte[] hashBytes = md5Hash(bytes);

        ByteBuffer buffer = ByteBuffer.wrap(hashBytes);
        return IntStream.generate(buffer::get).limit(buffer.remaining()) //get a stream of ints
                .map(b -> (0xFF & b)) //as unsigned byte
                .mapToObj(i -> String.format("%02x", i)) //format to hex (0-padded)
                .collect(Collectors.joining()); // join
    }

}
