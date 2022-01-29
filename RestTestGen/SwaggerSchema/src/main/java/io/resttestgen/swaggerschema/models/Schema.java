
package io.resttestgen.swaggerschema.models;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "loadingURI",
    "pointer"
})
public class Schema {

    @JsonProperty("loadingURI")
    private String loadingURI;
    @JsonProperty("pointer")
    private String pointer;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("loadingURI")
    public String getLoadingURI() {
        return loadingURI;
    }

    @JsonProperty("loadingURI")
    public void setLoadingURI(String loadingURI) {
        this.loadingURI = loadingURI;
    }

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
