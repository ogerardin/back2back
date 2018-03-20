package org.ogerardin.b2b.util;

import java.nio.ByteBuffer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public enum FormattingHelper {
    ;

    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    public static String humanReadableByteCount(long toDoSize) {
        return humanReadableByteCount(toDoSize, false);
    }

    public static String hex(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        return IntStream.generate(buffer::get).limit(buffer.remaining()) //get a stream of ints
                .map(b -> (0xFF & b)) //as unsigned byte
                .mapToObj(i -> String.format("%02x", i)) //format to hex (0-padded)
                .collect(Collectors.joining()); // join
    }
}
