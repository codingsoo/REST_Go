
package io.resttestgen.swaggerschema.models;

import com.fasterxml.jackson.annotation.*;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "pointer"
})
public class Instance {

    @JsonProperty("pointer")
    private String pointer;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("pointer")
    public String getPointer() {
        return pointer;
    }

    @JsonProperty("pointer")
    public void setPointer(String pointer) {
        this.pointer = pointer;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
