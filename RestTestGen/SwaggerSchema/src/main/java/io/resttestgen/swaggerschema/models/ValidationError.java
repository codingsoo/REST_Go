
package io.resttestgen.swaggerschema.models;

import com.fasterxml.jackson.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "level",
    "schema",
    "instance",
    "domain",
    "keyword",
    "message",
    "required",
    "missing",
    "additionalProperties"
})
public class ValidationError {

    @JsonProperty("level")
    private String level;
    @JsonProperty("schema")
    private Schema schema;
    @JsonProperty("instance")
    private Instance instance;
    @JsonProperty("domain")
    private String domain;
    @JsonProperty("keyword")
    private String keyword;
    @JsonProperty("message")
    private String message;
    @JsonProperty("required")
    private List<String> required = null;
    @JsonProperty("missing")
    private List<String> missing = null;

    @JsonProperty("fieldDetails")
    private Map<String, Object> fieldDetails = new HashMap<String, Object>();

    @JsonProperty("level")
    public String getLevel() {
        return level;
    }

    @JsonProperty("level")
    public void setLevel(String level) {
        this.level = level;
    }

    @JsonProperty("schema")
    public Schema getSchema() {
        return schema;
    }

    @JsonProperty("schema")
    public void setSchema(Schema schema) {
        this.schema = schema;
    }

    @JsonProperty("instance")
    public Instance getInstance() {
        return instance;
    }

    @JsonProperty("instance")
    public void setInstance(Instance instance) {
        this.instance = instance;
    }

    @JsonProperty("domain")
    public String getDomain() {
        return domain;
    }

    @JsonProperty("domain")
    public void setDomain(String domain) {
        this.domain = domain;
    }

    @JsonProperty("keyword")
    public String getKeyword() {
        return keyword;
    }

    @JsonProperty("keyword")
    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    @JsonProperty("message")
    public String getMessage() {
        return message;
    }

    @JsonProperty("message")
    public void setMessage(String message) {
        this.message = message;
    }

    @JsonProperty("required")
    public List<String> getRequired() {
        return required;
    }

    @JsonProperty("required")
    public void setRequired(List<String> required) {
        this.required = required;
    }

    @JsonProperty("missing")
    public List<String> getMissing() {
        return missing;
    }

    @JsonProperty("missing")
    public void setMissing(List<String> missing) {
        this.missing = missing;
    }

    public Map<String, Object> getFieldDetails() {
        return this.fieldDetails;
    }

    public void setFieldDetails(String name, Object value) {
        this.fieldDetails.put(name, value);
    }

}
