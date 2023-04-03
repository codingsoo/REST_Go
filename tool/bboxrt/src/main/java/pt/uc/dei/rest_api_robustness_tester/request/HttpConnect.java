package pt.uc.dei.rest_api_robustness_tester.request;

import org.apache.http.client.methods.HttpRequestBase;

import java.net.URI;

/**
 * HTTP CONNECT method.
 * <p>
 * The HTTP CONNECT method is defined in section 9.9 of
 * <a href="http://www.ietf.org/rfc/rfc2616.txt">RFC2616</a>:
 * </p>
 * <blockquote>
 * This specification reserves the method name CONNECT for use with a
 * proxy that can dynamically switch to being a tunnel (e.g. SSL tunneling).
 * </blockquote>
 *
 */
public class HttpConnect extends HttpRequestBase
{
    public final static String METHOD_NAME = "CONNECT";
    
    public HttpConnect(final URI uri) {
        super();
        setURI(uri);
    }
    
    /**
     * @throws IllegalArgumentException if the uri is invalid.
     */
    public HttpConnect(final String uri) {
        super();
        setURI(URI.create(uri));
    }
    
    @Override
    public String getMethod() {
        return METHOD_NAME;
    }
}
