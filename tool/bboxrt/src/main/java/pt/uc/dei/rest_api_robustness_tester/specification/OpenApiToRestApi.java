package pt.uc.dei.rest_api_robustness_tester.specification;

import java.io.*;
import io.swagger.v3.oas.models.OpenAPI;
import org.apache.commons.lang3.RandomUtils;
import pt.uc.dei.rest_api_robustness_tester.Operation;
import pt.uc.dei.rest_api_robustness_tester.Path;
import pt.uc.dei.rest_api_robustness_tester.Server;
import pt.uc.dei.rest_api_robustness_tester.media.MediaType;
import pt.uc.dei.rest_api_robustness_tester.request.Parameter;
import pt.uc.dei.rest_api_robustness_tester.request.RequestBody;
import pt.uc.dei.rest_api_robustness_tester.response.Response;
import pt.uc.dei.rest_api_robustness_tester.response.StatusCode;
import pt.uc.dei.rest_api_robustness_tester.schema.Schema;
import pt.uc.dei.rest_api_robustness_tester.schema.SchemaBuilder;
import pt.uc.dei.rest_api_robustness_tester.security.*;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.*;

//TODO: create non-recursive alternatives for all recursive functions (e.g., using while-loop)
public class OpenApiToRestApi implements RestApiConverter<OpenAPI>
{
    private boolean ParametersAreEqual(Parameter param1, Parameter param2)
    {
        return param1.name.equals(param2.name) && param1.location.equals(param2.location);
    }
    
    private Path.HttpMethod GetEquivalentHttpMethod(io.swagger.v3.oas.models.PathItem.HttpMethod httpOpenAPI)
    {
        switch(httpOpenAPI)
        {
            case DELETE:
                return Path.HttpMethod.DELETE;
            case GET:
                return Path.HttpMethod.GET;
            case POST:
                return Path.HttpMethod.POST;
            case PUT:
                return Path.HttpMethod.PUT;
        }
        
        return null;
    }
    
    private SchemaBuilder ResolveSchema(OpenAPI openAPI, io.swagger.v3.oas.models.media.Schema<?> schemaOpenAPI,ArrayList<String> parents)
    {
        String fatherAux = null;
        String currentFather = null;
        if(schemaOpenAPI.get$ref() != null){
            if(schemaOpenAPI.get$ref().split("/").length != 0 )
                fatherAux = schemaOpenAPI.get$ref().split("/")[schemaOpenAPI.get$ref().split("/").length - 1];

            if(parents != null && !parents.contains(fatherAux)){
                currentFather = fatherAux;
                schemaOpenAPI = GetSchemaRef(openAPI, schemaOpenAPI.get$ref(),schemaOpenAPI, parents);
            }
            else if(parents != null && parents.contains(fatherAux)) {
                for(String schemaName : openAPI.getComponents().getSchemas().keySet())
                    if(schemaName.equals(fatherAux.trim())){

                        io.swagger.v3.oas.models.media.Schema aux = openAPI.getComponents().getSchemas().get(schemaName);

                        //Map<String, io.swagger.v3.oas.models.media.Schema> hsmap = aux.getProperties();
                        Map<String,io.swagger.v3.oas.models.media.Schema> hsmap = new HashMap<String,io.swagger.v3.oas.models.media.Schema>();

                        for(Object p : aux.getProperties().keySet()){
                            hsmap.put((String)p, (io.swagger.v3.oas.models.media.Schema) aux.getProperties().get(p));
                        }


                        ArrayList<String> removeProps  = new ArrayList<>();
                        for(String propert : hsmap.keySet() ){

                            io.swagger.v3.oas.models.media.Schema currentSchema =  hsmap.get(propert);
                            if(currentSchema.getClass() == io.swagger.v3.oas.models.media.ArraySchema.class){
                                String ref = ((io.swagger.v3.oas.models.media.ArraySchema)currentSchema).getItems().get$ref();
                                if(parents.contains(ref.split("/")[ref.split("/").length - 1]))
                                    removeProps.add(propert);
                            }


                            if(currentSchema.get$ref() != null){
                                String currentSchemaREF = currentSchema.get$ref();
                                if(parents.contains(currentSchemaREF.split("/")[currentSchemaREF.split("/").length - 1]))
                                    removeProps.add(propert);
                            }
                        }


                        for(String rmov : removeProps)
                            hsmap.remove(rmov);

                        aux.setProperties(hsmap);

                        schemaOpenAPI = aux;
                    }
            }

            if(fatherAux != null && parents != null && !parents.contains(fatherAux))
                parents.add(fatherAux);
        }

        
        SchemaBuilder schemaRestAPI = Schema.Builder();


        //TODO: see about examples
        schemaRestAPI.format = schemaOpenAPI.getFormat();
        schemaRestAPI.type = schemaOpenAPI.getType();
        if(schemaOpenAPI.getPattern() != null)
            schemaRestAPI.pattern = schemaOpenAPI.getPattern();
        if(schemaOpenAPI.getMultipleOf()!= null)
            schemaRestAPI.multipleOf = "" + schemaOpenAPI.getMultipleOf();
        if(schemaOpenAPI.getDefault() != null)
                schemaRestAPI.defaultValue = "" + schemaOpenAPI.getDefault();
        if(schemaOpenAPI.getReadOnly() != null)
            schemaRestAPI.readOnly = schemaOpenAPI.getReadOnly();
        if(schemaOpenAPI.getWriteOnly() != null)
            schemaRestAPI.writeOnly = schemaOpenAPI.getWriteOnly();
        if(schemaOpenAPI.getNullable()!= null)
            schemaRestAPI.nullable = schemaOpenAPI.getNullable();
        if(schemaOpenAPI.getProperties() != null){
            for(String propertyOpenAPI : schemaOpenAPI.getProperties().keySet()){
                if(schemaOpenAPI.getProperties().get(propertyOpenAPI) != null)
                    schemaRestAPI.properties.put(propertyOpenAPI, ResolveSchema(openAPI, schemaOpenAPI.getProperties().get(propertyOpenAPI), parents));
            }


        }

        if(schemaOpenAPI.getClass() == io.swagger.v3.oas.models.media.ArraySchema.class){
            schemaRestAPI.items = ResolveSchema(openAPI, ((io.swagger.v3.oas.models.media.ArraySchema)schemaOpenAPI).getItems(),parents);
        }

        if(schemaOpenAPI.getRequired() != null)
        {
            schemaRestAPI.required = new ArrayList<>();
            schemaRestAPI.required.addAll(schemaOpenAPI.getRequired());
        }
        if(schemaOpenAPI.getEnum()!= null)
        {
            schemaRestAPI.enumValues = new ArrayList<>();
            for(Object enumValue : schemaOpenAPI.getEnum())
                schemaRestAPI.enumValues.add("" + enumValue);
        }
        if(schemaOpenAPI.getMaximum() != null)
        {
            if(schemaOpenAPI.getExclusiveMaximum() != null)
                schemaRestAPI.maximum = "" + (schemaOpenAPI.getExclusiveMaximum()? schemaOpenAPI.getMaximum().
                        subtract(BigDecimal.ONE) : schemaOpenAPI.getMaximum());
            else
                schemaRestAPI.maximum = "" + schemaOpenAPI.getMaximum();
        }
        if(schemaOpenAPI.getMaxLength() != null)
            schemaRestAPI.maximum = "" + schemaOpenAPI.getMaxLength();
        if(schemaOpenAPI.getMaxItems()!= null)
            schemaRestAPI.maximum = "" + schemaOpenAPI.getMaxItems();
        if(schemaOpenAPI.getMaxProperties()!= null)
            schemaRestAPI.maximum = "" + schemaOpenAPI.getMaxProperties();
        if(schemaOpenAPI.getMinimum()!= null)
        {
            if(schemaOpenAPI.getExclusiveMinimum() != null)
                schemaRestAPI.minimum = "" + (schemaOpenAPI.getExclusiveMinimum()? schemaOpenAPI.getMinimum().
                        add(BigDecimal.ONE) : schemaOpenAPI.getMinimum());
            else
                schemaRestAPI.minimum = "" + schemaOpenAPI.getMinimum();
        }
        if(schemaOpenAPI.getMinLength() != null)
            schemaRestAPI.minimum = "" + schemaOpenAPI.getMinLength();
        if(schemaOpenAPI.getMinItems()!= null)
            schemaRestAPI.minimum = "" + schemaOpenAPI.getMinItems();
        if(schemaOpenAPI.getMinProperties()!= null)
            schemaRestAPI.minimum = "" + schemaOpenAPI.getMinProperties();
    
        if(schemaOpenAPI.getClass() == io.swagger.v3.oas.models.media.ComposedSchema.class)
        {
            io.swagger.v3.oas.models.media.ComposedSchema composedSchemaOpenAPI = (io.swagger.v3.oas.models.media.ComposedSchema)schemaOpenAPI;
            if(composedSchemaOpenAPI.getOneOf() != null)
            {
                int i = RandomUtils.nextInt(0, composedSchemaOpenAPI.getOneOf().size());
                schemaRestAPI = ResolveSchema(openAPI, composedSchemaOpenAPI.getOneOf().get(i),parents);
            }
            if(composedSchemaOpenAPI.getAllOf() != null)
            {
                List<SchemaBuilder> schemas = new ArrayList<>();
                composedSchemaOpenAPI.getAllOf().forEach(s -> {schemas.add(ResolveSchema(openAPI, s, null));});
                SchemaBuilder schema = Schema.Builder();
    
                schema.type = "object";
                schemas.forEach(s -> {schema.properties.putAll(s.properties);});
                
                schemaRestAPI = schema;
            }
            if(composedSchemaOpenAPI.getAnyOf() != null)
            {
                List<SchemaBuilder> schemas = new ArrayList<>();
                composedSchemaOpenAPI.getAnyOf().forEach(s -> {schemas.add(ResolveSchema(openAPI, s, null));});
                SchemaBuilder schema = Schema.Builder();
    
                schema.type = "object";
                schemas.forEach(s -> {schema.properties.putAll(s.properties);});
    
                schemaRestAPI = schema;
            }
        }
        
        //FIXME: this shouldnt be here, it is probably a bug in the OpenAPI parser library
        //      some object-typed schemas are missing their property type: object when parsed
        if(schemaRestAPI.type == null && !schemaRestAPI.properties.isEmpty())
        {
            schemaRestAPI.type = "object";
            schemaRestAPI.format = null;
        }

        if(currentFather != null && fatherAux != null && currentFather.equals(fatherAux) && parents != null && parents.contains(currentFather))
            parents.remove(currentFather);
        return schemaRestAPI;
    }
    
    private io.swagger.v3.oas.models.media.Schema GetSchemaRef(OpenAPI openAPI, String ref, io.swagger.v3.oas.models.media.Schema schemaOpenAPI, ArrayList<String> parents)
    {
        String base = "#/components/schemas/";
    
        //TODO: this should not be here -> it is probably a parsing bug in swagger-parser
        if(ref.startsWith("#/definitions/"))
            ref = ref.replace("#/definitions/", base);
    
        if(ref.startsWith(base))
        {

            /*ArrayList<Character> charsAux = new ArrayList<Character>();
            for (char ch: ref.toCharArray()) {
                charsAux.add(ch);
            }

            StringBuilder builder = new StringBuilder(charsAux.size());
            for(Character ch: charsAux)
            {
                builder.append(ch);
                if(builder.length() == base.length() && base.equals(builder.toString())){
                    builder =  new StringBuilder(charsAux.size() - base.length());
                }
            }

            String trimmedRef = builder.toString();*/

            //Within-document $ref
            //System.out.println(ref);
            String trimmedRef = ref.replace(base,"");

            String auxRemove = "controlNestedRef/";
            String trimmedRefaux = trimmedRef;


            if(trimmedRef.split("/").length > 1) {
                //Nested $ref ???
                System.err.println("Nested $ref (" + ref + ")" + 191);

                /*trimmedRefaux = trimmedRef.replace(auxRemove,"");
                for(String schemaName : openAPI.getComponents().getSchemas().keySet())
                    if(schemaName.equals(trimmedRefaux.trim())){
                        schemaOpenAPI.set$ref(null);
                        return openAPI.getComponents().getSchemas().get(schemaName);
                    }*/
            }
            else
            {
                for(String schemaName : openAPI.getComponents().getSchemas().keySet())
                    if(schemaName.equals(trimmedRef.trim())){
                        //schemaOpenAPI.set$ref(base + "controlNestedRef/" + trimmedRef);
                        //schemaOpenAPI.set$ref(null);
                        return openAPI.getComponents().getSchemas().get(schemaName);
                    }

            }
        }
        else
        {
            //Between-document $ref
            System.err.println("Between-document $ref (" + ref + ")");
        }
        
        return null;
    }
    
    private Parameter ResolveParameter(OpenAPI openAPI, io.swagger.v3.oas.models.parameters.Parameter paramOpenAPI)
    {
        if(paramOpenAPI.get$ref() != null)
            paramOpenAPI = GetParameterRef(openAPI, paramOpenAPI.get$ref());
        
        Parameter paramRestAPI = new Parameter();
        
        paramRestAPI.name = paramOpenAPI.getName();
        paramRestAPI.location = Parameter.Location.GetLocationForValue(paramOpenAPI.getIn());
        paramRestAPI.required = paramOpenAPI.getRequired() == null? paramRestAPI.required : paramOpenAPI.getRequired();
        paramRestAPI.deprecated = paramOpenAPI.getDeprecated() == null? paramRestAPI.deprecated : paramOpenAPI.getDeprecated();
        if(paramOpenAPI.getSchema() != null)
            paramRestAPI.schema = ResolveSchema(openAPI, paramOpenAPI.getSchema(), new ArrayList<String>());
        if(paramOpenAPI.getContent()!= null)
        {
            String mediaTypeStringOpenAPI = new ArrayList<>(paramOpenAPI.getContent().keySet()).get(0);
            io.swagger.v3.oas.models.media.MediaType mediaTypeOpenAPI = paramOpenAPI.getContent().get(mediaTypeStringOpenAPI);
            
            MediaType mediaTypeRestAPI = new MediaType();
            paramRestAPI.content = mediaTypeRestAPI;

            mediaTypeRestAPI.mediaType = mediaTypeStringOpenAPI;
            mediaTypeRestAPI.schema = ResolveSchema(openAPI, mediaTypeOpenAPI.getSchema(),new ArrayList<String>());
        }

        //checkpoint aqui na linha 217
        return paramRestAPI;
    }
    
    private io.swagger.v3.oas.models.parameters.Parameter GetParameterRef(OpenAPI openAPI, String ref)
    {
        String base = "#/components/parameters/";
        
        if(ref.startsWith(base))
        {
            //Within-document $ref
            String trimmedRef = ref.replace(base, "");
            
            if(trimmedRef.split("/").length > 1)
            {
                //Nested $ref ???
                System.err.println("Nested $ref (" + ref + ")" + 251);
            }
            else
            {
                for(String parameterName : openAPI.getComponents().getParameters().keySet())
                    if(parameterName.equals(trimmedRef.trim()))
                        return openAPI.getComponents().getParameters().get(parameterName);
            }
        }
        else
        {
            //Between-document $ref
            System.err.println("Between-document $ref (" + ref + ")");
        }
        
        return null;
    }
    
    private Response ResolveResponse(OpenAPI openAPI, io.swagger.v3.oas.models.responses.ApiResponse respOpenAPI)
    {
        if(respOpenAPI.get$ref() != null)
            respOpenAPI = GetResponseRef(openAPI, respOpenAPI.get$ref());
        
        Response respRestAPI = new Response();
        
        respRestAPI.description = respOpenAPI.getDescription();
        
        if(respOpenAPI.getContent() != null)
        {
            for (String mediaTypeStringOpenAPI : respOpenAPI.getContent().keySet())
            {
                io.swagger.v3.oas.models.media.MediaType mediaTypeOpenAPI = respOpenAPI.getContent().get(mediaTypeStringOpenAPI);
        
                MediaType mediaTypeRestAPI = new MediaType();
                respRestAPI.mediaTypes.add(mediaTypeRestAPI);
        
                mediaTypeRestAPI.mediaType = mediaTypeStringOpenAPI;
                if(mediaTypeOpenAPI.getSchema() != null)
                    mediaTypeRestAPI.schema = ResolveSchema(openAPI, mediaTypeOpenAPI.getSchema(),new ArrayList<String>());
            }
        }
        
        return respRestAPI;
    }
    
    private io.swagger.v3.oas.models.responses.ApiResponse GetResponseRef(OpenAPI openAPI, String ref)
    {
        String base = "#/components/responses/";
        
        if(ref.startsWith(base))
        {
            //Within-document $ref
            String trimmedRef = ref.replace(base, "");
            
            if(trimmedRef.split("/").length > 1)
            {
                //Nested $ref ???
                System.err.println("Nested $ref (" + ref + ")" + 308);
            }
            else
            {
                for(String parameterName : openAPI.getComponents().getResponses().keySet())
                    if(parameterName.equals(trimmedRef.trim()))
                        return openAPI.getComponents().getResponses().get(parameterName);
            }
        }
        else
        {
            //Between-document $ref
            System.err.println("Between-document $ref (" + ref + ")");
        }
        
        return null;
    }
    
    private RequestBody ResolveRequestBody(OpenAPI openAPI, io.swagger.v3.oas.models.parameters.RequestBody reqOpenAPI)
    {
        if(reqOpenAPI.get$ref() != null)
            reqOpenAPI = GetRequestBodyRef(openAPI, reqOpenAPI.get$ref());
        
        RequestBody reqRestAPI = new RequestBody();
        
        reqRestAPI.required = reqOpenAPI.getRequired() == null? reqRestAPI.required : reqOpenAPI.getRequired();
        
        for(String mediaTypeStringOpenAPI : reqOpenAPI.getContent().keySet())
        {
            io.swagger.v3.oas.models.media.MediaType mediaTypeOpenAPI = reqOpenAPI.getContent().get(mediaTypeStringOpenAPI);

            MediaType mediaTypeRestAPI = new MediaType();
            reqRestAPI.mediaTypes.add(mediaTypeRestAPI);

            mediaTypeRestAPI.mediaType = mediaTypeStringOpenAPI;
            mediaTypeRestAPI.schema = ResolveSchema(openAPI, mediaTypeOpenAPI.getSchema(),new ArrayList<String>());
        }
        
        return reqRestAPI;
    }
    
    private io.swagger.v3.oas.models.parameters.RequestBody GetRequestBodyRef(OpenAPI openAPI, String ref)
    {
        String base = "#/components/requestBodies/";
        
        if(ref.startsWith(base))
        {
            //Within-document $ref
            String trimmedRef = ref.replace(base, "");
            
            if(trimmedRef.split("/").length > 1)
            {
                //Nested $ref ???
                System.err.println("Nested $ref (" + ref + ")" + 361);
            }
            else
            {
                for(String parameterName : openAPI.getComponents().getRequestBodies().keySet())
                    if(parameterName.equals(trimmedRef.trim()))
                        return openAPI.getComponents().getRequestBodies().get(parameterName);
            }
        }
        else
        {
            //Between-document $ref
            System.err.println("Between-document $ref (" + ref + ")");
        }
        
        return null;
    }
    
    private io.swagger.v3.oas.models.security.SecurityScheme GetSecuritySchemeRef(OpenAPI openAPI, String ref)
    {
        String base = "#/components/securitySchemes/";
        
        if(ref.startsWith(base))
        {
            //Within-document $ref
            String trimmedRef = ref.replace(base, "");
            
            if(trimmedRef.split("/").length > 1)
            {
                //Nested $ref ???
                System.err.println("Nested $ref (" + ref + ")" +  391);
            }
            else
            {
                for(String securitySchemeName : openAPI.getComponents().getSecuritySchemes().keySet())
                    if(securitySchemeName.equals(trimmedRef.trim()))
                        return openAPI.getComponents().getSecuritySchemes().get(securitySchemeName);
            }
        }
        else
        {
            //Between-document $ref
            System.err.println("Between-document $ref (" + ref + ")");
        }
        
        return null;
    }
    
    private OAuthFlow ResolveOAuthFlow(OpenAPI openAPI, io.swagger.v3.oas.models.security.OAuthFlow oAuthFlowOpenAPI)
    {
        OAuthFlow oAuthFlowRestAPI = new OAuthFlow();
    
        oAuthFlowRestAPI.authorizationUrl = oAuthFlowOpenAPI.getAuthorizationUrl();
        oAuthFlowRestAPI.refreshUrl = oAuthFlowOpenAPI.getRefreshUrl();
        oAuthFlowRestAPI.tokenUrl = oAuthFlowOpenAPI.getTokenUrl();
        if(oAuthFlowOpenAPI.getScopes() != null)
            oAuthFlowRestAPI.scopes.addAll(oAuthFlowOpenAPI.getScopes().values());
        
        return oAuthFlowRestAPI;
    }
    
    private SecurityScheme ResolveSecurityScheme(OpenAPI openAPI, io.swagger.v3.oas.models.security.SecurityScheme securitySchemeOpenAPI)
    {
        if(securitySchemeOpenAPI.get$ref() != null)
            securitySchemeOpenAPI = GetSecuritySchemeRef(openAPI, securitySchemeOpenAPI.get$ref());
        
        switch(securitySchemeOpenAPI.getType())
        {
            case APIKEY:
                ApiKey apiKey = new ApiKey();
                apiKey.name = securitySchemeOpenAPI.getName();
                apiKey.location = ApiKey.Location.GetLocationForValue(securitySchemeOpenAPI.getIn().toString());
                return apiKey;
            case HTTP:
                Http http = new Http();
                http.bearerFormat = securitySchemeOpenAPI.getBearerFormat();
                http.scheme = securitySchemeOpenAPI.getScheme();
                return http;
            case OAUTH2:
                OAuth2 oAuth2 = new OAuth2();
                if(securitySchemeOpenAPI.getFlows().getAuthorizationCode() != null)
                    oAuth2.authorizationCode = ResolveOAuthFlow(openAPI, securitySchemeOpenAPI.getFlows().getAuthorizationCode());
                if(securitySchemeOpenAPI.getFlows().getClientCredentials() != null)
                    oAuth2.clientCredentials = ResolveOAuthFlow(openAPI, securitySchemeOpenAPI.getFlows().getClientCredentials());
                if(securitySchemeOpenAPI.getFlows().getImplicit() != null)
                    oAuth2.implicit = ResolveOAuthFlow(openAPI, securitySchemeOpenAPI.getFlows().getImplicit());
                if(securitySchemeOpenAPI.getFlows().getPassword() != null)
                    oAuth2.password = ResolveOAuthFlow(openAPI, securitySchemeOpenAPI.getFlows().getPassword());
                return oAuth2;
            case OPENIDCONNECT:
                OpenIdConnect openIdConnect = new OpenIdConnect();
                openIdConnect.openIdConnectUrl = securitySchemeOpenAPI.getOpenIdConnectUrl();
                return openIdConnect;
        }
        
        return null;
    }
    
    @Override
    public RestApiSpecification Convert(OpenAPI openAPI)
    {
        RestApiSpecification restAPISpecification = new RestApiSpecification();

        restAPISpecification.specNameVersion = "OpenAPI " + openAPI.getOpenapi();

        restAPISpecification.apiNameVersion = openAPI.getInfo().getTitle() + " " + openAPI.getInfo().getVersion();

        for (io.swagger.v3.oas.models.servers.Server serverOpenAPI : openAPI.getServers())
        {
            restAPISpecification.servers.add(new Server(serverOpenAPI.getUrl()));
        }
        
        if(openAPI.getSecurity() != null)
        {
            for(io.swagger.v3.oas.models.security.SecurityRequirement secReqOpenAPI : openAPI.getSecurity())
            {
                SecurityRequirement secReqRestAPI = new SecurityRequirement();
                secReqRestAPI.requirement.putAll(secReqOpenAPI);
                restAPISpecification.securityRequirements.add(secReqRestAPI);
            }
        }

        if(openAPI.getComponents()!= null && openAPI.getComponents().getSecuritySchemes() != null)
        {
            for(String key : openAPI.getComponents().getSecuritySchemes().keySet())
            {
                io.swagger.v3.oas.models.security.SecurityScheme securitySchemeOpenAPI = openAPI.getComponents().getSecuritySchemes().get(key);
                
                restAPISpecification.securitySchemes.put(key, ResolveSecurityScheme(openAPI, securitySchemeOpenAPI));
            }
        }

        for (String pathURI : openAPI.getPaths().keySet())
        {
            io.swagger.v3.oas.models.PathItem pathOpenAPI = openAPI.getPaths().get(pathURI);
            
            Path pathRestAPI = new Path();
            restAPISpecification.paths.add(pathRestAPI);
            
            List<Parameter> pathParamRestAPI = new ArrayList<>();
            if(pathOpenAPI.getParameters() != null)
            {
                for(io.swagger.v3.oas.models.parameters.Parameter paramOpenAPI : pathOpenAPI.getParameters())
                {
                    Parameter paramRestAPI = ResolveParameter(openAPI, paramOpenAPI);
                    pathParamRestAPI.add(paramRestAPI);
                }
            }
            
            if(pathOpenAPI.get$ref() != null)
            {
                //$ref (pathOpenAPI)
                System.err.println("$ref (pathOpenAPI)");
            }
            else
            {
                pathRestAPI.uri = pathURI;
                
                if(pathOpenAPI.getServers() != null)
                    for(io.swagger.v3.oas.models.servers.Server serverOpenAPI : pathOpenAPI.getServers())
                        pathRestAPI.servers.add(new Server(serverOpenAPI.getUrl()));
            }
            
            for(io.swagger.v3.oas.models.PathItem.HttpMethod httpOpenAPI : pathOpenAPI.readOperationsMap().keySet())
            {
                if(GetEquivalentHttpMethod(httpOpenAPI) == null)
                    continue;
                
                io.swagger.v3.oas.models.Operation opOpenAPI = pathOpenAPI.readOperationsMap().get(httpOpenAPI);
                
                Operation opRestAPI = new Operation();
                pathRestAPI.operations.put(GetEquivalentHttpMethod(httpOpenAPI), opRestAPI);
                
                if(opOpenAPI.getOperationId() == null)
                    opRestAPI.operationID = GetEquivalentHttpMethod(httpOpenAPI) + "_" + pathRestAPI.uri;
                else
                    opRestAPI.operationID = opOpenAPI.getOperationId();
                
                if(opOpenAPI.getServers() != null)
                    for(io.swagger.v3.oas.models.servers.Server serverOpenAPI : opOpenAPI.getServers())
                        opRestAPI.servers.add(new Server(serverOpenAPI.getUrl()));

                if(opOpenAPI.getSecurity() != null)
                {
                    for(io.swagger.v3.oas.models.security.SecurityRequirement secReqOpenAPI : opOpenAPI.getSecurity())
                    {
                        SecurityRequirement secReqRestAPI = new SecurityRequirement();
                        secReqRestAPI.requirement.putAll(secReqOpenAPI);
                        opRestAPI.securityRequirements.add(secReqRestAPI);
                    }
                }
                
                opRestAPI.deprecated = opOpenAPI.getDeprecated() == null? opRestAPI.deprecated : opOpenAPI.getDeprecated();

                if(opOpenAPI.getParameters() != null)
                {
                    for(io.swagger.v3.oas.models.parameters.Parameter paramOpenAPI : opOpenAPI.getParameters())
                    {
                        Parameter paramRestAPI = ResolveParameter(openAPI, paramOpenAPI);
                        opRestAPI.parameters.add(paramRestAPI);
                    }
                    
                    for(Parameter pathParameter : pathParamRestAPI)
                    {
                        int matches = 0;
                        for(Parameter opParameter : opRestAPI.parameters)
                            if(ParametersAreEqual(pathParameter, opParameter))
                                matches++;
                        if(matches == 0)
                            opRestAPI.parameters.add(pathParameter);
                    }
                    
                    for(Parameter paramRestAPI : opRestAPI.parameters)
                    {
                        if(paramRestAPI.location == Parameter.Location.Path)
                        {
                            String[] uriSplit = pathRestAPI.uri.split(Server.PATH_SEP);
                            for(int i = 0; i < uriSplit.length; i++)
                                if(uriSplit[i].contains(paramRestAPI.name))
                                    paramRestAPI.pathPositionFromTheEnd = uriSplit.length - 1 - i;
                        }
                    }
                }
                
                if(opOpenAPI.getRequestBody() != null)
                {
                    io.swagger.v3.oas.models.parameters.RequestBody reqOpenAPI = opOpenAPI.getRequestBody();
                    
                    RequestBody reqRestAPI = ResolveRequestBody(openAPI, reqOpenAPI);
                    opRestAPI.requestBody = reqRestAPI;
                }
                
                for(String statusOpenAPI : opOpenAPI.getResponses().keySet())
                {
                    io.swagger.v3.oas.models.responses.ApiResponse respOpenAPI = opOpenAPI.getResponses().get(statusOpenAPI);
                    
                    Response respRestAPI = ResolveResponse(openAPI, respOpenAPI);
                    opRestAPI.responses.put(StatusCode.FromString(statusOpenAPI), respRestAPI);
                }
            }
        }

        return restAPISpecification;
    }
}
