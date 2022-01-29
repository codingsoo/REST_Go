package io.resttestgen.nominaltester.reports.gsonadapters;

import com.google.gson.*;
import io.resttestgen.nominaltester.models.Authentication;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.TreeMap;

/**
 * Gson Adapter for serialization/deserialization of object of class Authentication
 */
public class AuthenticationTypeAdapter implements JsonSerializer<Authentication>, JsonDeserializer<Authentication> {

    @Override
    public Authentication deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject asJsonObject = json.getAsJsonObject();
        boolean authenticated = asJsonObject.get("authenticated").getAsBoolean();
        JsonElement authenticationClassName = asJsonObject.get("authenticationClassName");
        Authentication auth = new Authentication();
        auth.setClientAuthenticated(authenticated);
        if (authenticationClassName != null) {
            String className = asJsonObject.get("authenticationClassName").getAsString();
            try {
                auth.setAuthenticationClass(Class.forName(className));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return auth;
    }

    @Override
    public JsonElement serialize(Authentication src, Type typeOfSrc, JsonSerializationContext context) {
        Map<String, Object> map = new TreeMap<>();
        map.put("authenticated", src.isClientAuthenticated());
        Class<?> authenticationClass = src.getAuthenticationClass();
        if (authenticationClass != null) {
            map.put("authenticationClassName", src.getAuthenticationClass().getName());
        }
        return context.serialize(map);
    }
}
