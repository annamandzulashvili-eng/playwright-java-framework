package qa.core.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

/** Shared, thread-safe Jackson mapper. */
public final class Json {

    private static final ObjectMapper MAPPER = JsonMapper.builder()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .build();

    private Json() {
    }

    public static <T> T read(String json, Class<T> type) {
        try {
            return MAPPER.readValue(json, type);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(
                    "Cannot parse response as " + type.getSimpleName() + ": " + abbreviate(json), e);
        }
    }

    public static String write(Object value) {
        try {
            return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot serialize " + value.getClass().getSimpleName(), e);
        }
    }

    private static String abbreviate(String s) {
        return s == null ? "null" : s.length() <= 300 ? s : s.substring(0, 300) + "...";
    }
}
