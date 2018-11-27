package org.ogerardin.b2b.embeddedmongo;

import de.flapdoodle.embed.process.io.IStreamProcessor;
import de.flapdoodle.embed.process.io.Slf4jLevel;
import org.ogerardin.b2b.util.Maps;
import org.slf4j.Logger;

import java.util.Map;

/**
 * Stream processor for feeding MongoDB log output to a specified {@link Logger}.
 * Each line is handled as follows:
 * -remove line breaks
 * -strip leading timestamp
 * -map log severity (one character) to a Slf4j level and log to this level
 */
class MongoSlf4jStreamProcessor implements IStreamProcessor {
    private static final Map<Character, Slf4jLevel> LEVEL_MAP = Maps.mapOf(
            'D', Slf4jLevel.DEBUG,
            'I', Slf4jLevel.INFO,
            'W', Slf4jLevel.WARN,
            'E', Slf4jLevel.ERROR,
            'F', Slf4jLevel.ERROR
    );

    private final Logger logger;
    private final Slf4jLevel defaultLevel;

    MongoSlf4jStreamProcessor(Logger logger, Slf4jLevel defaultLevel) {
        this.logger = logger;
        this.defaultLevel = defaultLevel;
    }

    @Override
    public void process(String block) {
        String line = block
                .replaceAll("[\n\r]+", "")
                // remove timestamps
                .replaceAll("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{3}(\\+\\d{4})? ", "");

        Slf4jLevel level = defaultLevel;
        if (line.charAt(1) == ' ') {
            Character mongoLevel = line.charAt(0);
            Slf4jLevel mappedLevel = LEVEL_MAP.get(mongoLevel);
            if (mappedLevel != null) {
                level = mappedLevel;
                line = line.substring(2);
            }
        }
        level.log(logger, line);
    }

    @Override
    public void onProcessed() {
    }

}
