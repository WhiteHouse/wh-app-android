package gov.whitehouse.util;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class DateFormatter implements JsonDeserializer<Date>, JsonSerializer<Date> {

    private final DateFormat[] formats;

    /**
     * Create date formatter
     */
    public DateFormatter() {
        formats = new DateFormat[1];
        formats[0] = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        final TimeZone timeZone = TimeZone.getTimeZone("Zulu"); //$NON-NLS-1$
        for (DateFormat format : formats) {
            format.setTimeZone(timeZone);
        }
    }

    public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        JsonParseException exception = null;
        final String value = json.getAsString();
        for (DateFormat format : formats) {
            try {
                synchronized (format) {
                    return format.parse(value);
                }
            } catch (ParseException e) {
                exception = new JsonParseException(e);
            }
        }
        throw exception;
    }

    public JsonElement serialize(Date date, Type type, JsonSerializationContext context) {
        final DateFormat primary = formats[0];
        String formatted;
        synchronized (primary) {
            formatted = primary.format(date);
        }
        return new JsonPrimitive(formatted);
    }
}
