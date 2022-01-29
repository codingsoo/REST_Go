package io.resttestgen.nominaltester.fieldgenerator.manufacturetraces;

import com.google.gson.Gson;
import org.apache.commons.text.StringEscapeUtils;

import java.util.Collection;

public class GsonTrace extends ManufactureTrace {
    private final Object object;
    private final Class objectClass;

    public GsonTrace(Object object, Class objectClass) {
        this.object = object;
        this.objectClass = objectClass;
    }

    public Object getObject() {
        return object;
    }

    @Override
    public String constructionString() {
        Gson gson = new Gson();

        String objectRepresentation = StringEscapeUtils.escapeJava(gson.toJson(object));
        String objectName = getObjectName(object);
        String className = objectClass.getSimpleName();

        if (object == null)
            return String.format("%s %s = null;", className, objectName);

        if (Collection.class.isAssignableFrom(object.getClass())) {
            return String.format("List<%s> %s = new Gson().fromJson(\"%s\", new TypeToken<List<%s>>(){}.getType());", className,
                    objectName, objectRepresentation, className);
        }

        if (object.getClass().isEnum()) {
            className = objectClass.getName().replace("$", ".");
            return String.format("%s %s = new Gson().fromJson(\"%s\", %s.class);", className,
                    objectName, objectRepresentation, className);
        }

        return String.format("%s %s = new Gson().fromJson(\"%s\", %s.class);", className,
                objectName, objectRepresentation, className);
    }
}
