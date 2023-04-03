package pt.uc.dei.rest_api_robustness_tester;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.http.*;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.config.SocketConfig;
import org.apache.http.entity.*;
import org.apache.http.impl.bootstrap.HttpServer;
import org.apache.http.impl.bootstrap.ServerBootstrap;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.apache.http.protocol.HttpRequestHandlerMapper;
import pt.uc.dei.rest_api_robustness_tester.faultload.FaultloadExecutor;
import pt.uc.dei.rest_api_robustness_tester.response.StatusCode;
import pt.uc.dei.rest_api_robustness_tester.utils.Config;
import pt.uc.dei.rest_api_robustness_tester.utils.Utils;
import pt.uc.dei.rest_api_robustness_tester.utils.Writer;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.TimeUnit;

public class ProxyServer implements HttpRequestHandlerMapper, HttpRequestHandler
{
    private final HttpRequestHandler CONNECT_HANDLER = (request, response, context) ->
    {
        System.out.println("[CONNECT REQUEST] " + request.getRequestLine());
        
        response.setStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_BAD_REQUEST);
        try
        {
            RequestBuilder requestBuilder = RequestBuilder.copy(request);
            
            URI targetHostURI = null;
            if(requestBuilder.getHeaders(HTTP.TARGET_HOST).length > 0)
                targetHostURI = new URI("" + new Server(requestBuilder.getHeaders(HTTP.TARGET_HOST)[0].getValue()));
            URI requestLineURI = new URI("" + new Server(requestBuilder.getUri().toString()));
    
            URI uri = GetMostCompleteURI(targetHostURI, requestLineURI);
            if (uri == null)
                throw new Exception("No suitable Host URI found in HTTP Request");
            this.proxyHost = new HttpHost(URIUtils.extractHost(uri));

            response.setStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_OK);
            System.out.println("Established CONNECT tunnel to " + this.proxyHost.toString());
        }
        catch(Exception e)
        {
            System.out.println("Failed to establish CONNECT tunnel to " + this.proxyHost.toString());
            e.printStackTrace();
        }
    
        System.out.println("[CONNECT RESPONSE] " + response.getStatusLine());
    };
    
    private final HttpServer httpServer;
    private HttpHost proxyHost;
    private final FaultloadExecutor faultloadExecutor;
    public final Writer writer;
    private final HttpClientBuilder httpClientBuilder;
    
    public ProxyServer(int port, FaultloadExecutor faultloadExecutor, Writer writer)
    {
        this(port, faultloadExecutor, writer, null);
    }
    public ProxyServer(int port, FaultloadExecutor faultloadExecutor, Writer writer, HttpHost proxyHost)
    {
        this.faultloadExecutor = faultloadExecutor;
        this.writer = writer;
        this.proxyHost = proxyHost;
        this.httpClientBuilder = Utils.DisableSSL(HttpClientBuilder.create()).
                setDefaultRequestConfig(RequestConfig.copy(RequestConfig.DEFAULT).
                        setConnectTimeout(Config.Instance().connTimeout * 1000).
                        setSocketTimeout(Config.Instance().connTimeout * 1000).
                        setConnectionRequestTimeout(Config.Instance().connTimeout * 1000).
                        build()).
                disableCookieManagement();
        
        this.httpServer = ServerBootstrap.bootstrap().
                setSocketConfig(SocketConfig.copy(SocketConfig.DEFAULT).
                        setSoTimeout(Config.Instance().connTimeout * 1000).
                        build()).
                setListenerPort(port).
                setHandlerMapper(this).
                create();
    }
    
    public void SetProxyHost(HttpHost proxyHost)
    {
        this.proxyHost = proxyHost;
    }
    
    public void Start() throws IOException
    {
        this.httpServer.start();
    }
    
    public void Stop() throws InterruptedException
    {
        this.httpServer.shutdown(5, TimeUnit.SECONDS);
    }
    
    public HttpServer GetHttpServer()
    {
        return this.httpServer;
    }
    
    @Override
    public HttpRequestHandler lookup(HttpRequest request)
    {
        if(request.getRequestLine().getMethod().equalsIgnoreCase("connect"))
            return CONNECT_HANDLER;
        
        return this;
    }
    
    @Override
    public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException
    {
        System.out.println("[REQUEST] " + request.getRequestLine());
        
        try (CloseableHttpClient httpClient = httpClientBuilder.build())
        {
            RequestBuilder requestBuilder = RequestBuilder.copy(request);
            if(requestBuilder.getEntity() != null)
                requestBuilder.setEntity(new BufferedHttpEntity(requestBuilder.getEntity()));

    
            writer.Add("Server", "" + new Server(requestBuilder.getUri().toString())).
                    Add("Method", "" + requestBuilder.getMethod()).
                    Add("Endpoint", requestBuilder.getUri().getPath()).
                    Add("Operation ID", "---").
                    Add("Request", Utils.GetRawRequest(requestBuilder.build()));
            
            HttpHost httpHost = null;
            if(this.proxyHost != null)
                httpHost = this.proxyHost;
            else
            {
                URI targetHostURI = null;
                if(requestBuilder.getHeaders(HTTP.TARGET_HOST).length > 0)
                    targetHostURI = new URI("" + new Server(requestBuilder.getHeaders(HTTP.TARGET_HOST)[0].getValue()));
                URI requestLineURI = new URI("" + new Server(requestBuilder.getUri().toString()));

//                System.out.println("requestLineURI: " + requestLineURI.toString());
//                System.out.println("targetHostURI: " + targetHostURI.toString());

                URI uri = GetMostCompleteURI(targetHostURI, requestLineURI);
                if (uri == null)
                    throw new Exception("No suitable Host URI found in HTTP Request");
                httpHost = new HttpHost(URIUtils.extractHost(uri));
            }
            
            //TODO: these headers should be Global constants (i.e., config parameters)
            String[] requestHeadersToRemove = new String[]
                    {
                            HTTP.CONTENT_LEN,
                            HTTP.TARGET_HOST,
                    };
            for(String header : requestHeadersToRemove)
                requestBuilder.removeHeaders(header);
            
            if(faultloadExecutor != null)
                faultloadExecutor.Execute(requestBuilder);
            
            try (CloseableHttpResponse proxyResponse = httpClient.execute(httpHost, requestBuilder.build()))
            {
                System.out.println("[RESPONSE] " + proxyResponse.getStatusLine());
    
                StatusCode statusCode = StatusCode.FromInt(proxyResponse.getStatusLine().getStatusCode());
                writer.Add("Status code", "" + statusCode).
                        Add("Status reason", proxyResponse.getStatusLine().getReasonPhrase());
                
                response.setLocale(proxyResponse.getLocale());
                response.setStatusLine(proxyResponse.getStatusLine());

                response.setHeaders(proxyResponse.getAllHeaders());
    
                //TODO: these headers should be Global constants (i.e., config parameters)
                String[] responseHeadersToRemove = new String[]
                        {
                                "Set-Cookie",
                                HTTP.TRANSFER_ENCODING,
                        };
                responseHeadersToRemove = ArrayUtils.addAll(requestHeadersToRemove, responseHeadersToRemove);
                for(String header : responseHeadersToRemove)
                    response.removeHeaders(header);
    
                byte[] entity = IOUtils.toByteArray(proxyResponse.getEntity().getContent());
    
                writer.Add("Response", new String(entity));
                
                response.setEntity(new ByteArrayEntity(entity));
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    private URI GetMostCompleteURI(URI targetHostURI, URI requestLineURI)
    {
        if(targetHostURI == null && requestLineURI != null)
            return requestLineURI;
        else if(targetHostURI != null && requestLineURI == null)
            return targetHostURI;
        else if(targetHostURI == null && requestLineURI == null)
            return null;
        
        if(targetHostURI.equals(requestLineURI))
            return targetHostURI;
        
        if(targetHostURI.isAbsolute() && requestLineURI.isAbsolute())
        {
            if(URIUtils.extractHost(targetHostURI) != null && URIUtils.extractHost(requestLineURI) != null)
                return targetHostURI;
            else if(URIUtils.extractHost(targetHostURI) != null)
                return targetHostURI;
            else if(URIUtils.extractHost(requestLineURI) != null)
                return requestLineURI;
            else
                System.err.println("Both the Host header URI and the Request line URI are absolute URIs, but neither " +
                        "may be converted to an HttpHost object");
        }
        else if(targetHostURI.isAbsolute())
        {
            if(URIUtils.extractHost(targetHostURI) != null)
                return targetHostURI;
            else
                System.err.println("Host header URI is an absolute URI but cannot be converted to HttpHost");
        }
        else if(requestLineURI.isAbsolute())
        {
            if(URIUtils.extractHost(requestLineURI) != null)
                return requestLineURI;
            else
                System.err.println("Request line URI is an absolute URI but cannot be converted to HttpHost");
        }
        else
            System.err.println("Neither the Host header URI nor the Request line URI are absolute URIs");
        
        return null;
    }
}
