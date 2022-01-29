package io.resttestgen.requestbuilder.parameters;

import com.squareup.okhttp.MediaType;
import io.swagger.v3.oas.models.media.Content;

import java.util.Map;
import java.util.Set;

public class BodyParameter extends RequestParameter {
    private Content content;
    private MediaType mediaType;
    private Object parameterValue;

    public Set<Map.Entry<String, io.swagger.v3.oas.models.media.MediaType>> getAvailableMediaTypes() {
        return content.entrySet();
    }

    public void setContent(Content content) {
        this.content = content;
    }

    public void setParameterValue(MediaType mediaType, Object parameterValue) {
        this.mediaType = mediaType;
        this.parameterValue = parameterValue;
    }

    @Override
    public Object getParameterValue() {
        return parameterValue;
    }

    public MediaType getMediaType() {
        return mediaType;
    }

    @Override
    public boolean isValueSet() {
        return parameterValue != null;
    }
}
