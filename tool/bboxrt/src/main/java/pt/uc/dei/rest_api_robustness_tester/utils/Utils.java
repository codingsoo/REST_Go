package pt.uc.dei.rest_api_robustness_tester.utils;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.http.*;
import org.apache.http.client.methods.HttpRequestWrapper;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Collection;

public abstract class Utils
{
    public static String GetRawRequest(HttpRequest httpRequest) throws IOException
    {
        HttpRequestWrapper wrappedRequest = HttpRequestWrapper.wrap(httpRequest);
        StringBuilder raw = new     StringBuilder();
        
        raw.append(wrappedRequest.getRequestLine()).append("\n");
        
        for(Header h : wrappedRequest.getAllHeaders())
            raw.append(h.getName()).append(": ").append(h.getValue()).append("\n");
        
        raw.append("\n");
        
        if (httpRequest instanceof HttpEntityEnclosingRequest)
        {
            RequestBuilder requestBuilder = RequestBuilder.copy(httpRequest);
            if(requestBuilder.getEntity() != null)
            {
                BufferedHttpEntity repeatableEntity = new BufferedHttpEntity(requestBuilder.getEntity());
                if(repeatableEntity.getContent() != null)
                    raw.append(new String(IOUtils.toByteArray(repeatableEntity.getContent())));
            }
        }
        
        return raw.toString();
    }
    
    public static String InlineString(String original) {return InlineString(original, false);}
    public static String InlineString(String original, boolean cleanTabs)
    {
        return original.replace("\\n", "").
                replaceAll(cleanTabs? "\t+" : "", "").
                replaceAll("\n+", "").
                trim();
    }
    
    @SafeVarargs
    public static <T> T RandomElement(T ... t)
    {
        return t[RandomUtils.nextInt(0, t.length)];
    }
    
    public static char RandomChar(String set)
    {
        return set.charAt(RandomUtils.nextInt(0, set.length()));
    }
    
    public static String RandomString(String set, int length)
    {
        StringBuilder r = new StringBuilder();
        for(int i = 0; i < length; i++)
            r.append(RandomChar(set));
        
        return r.toString();
    }
    
    public static String AsciiNonPrintableCharacters()
    {
        StringBuilder asciiNonPrintableCharacters = new StringBuilder();
        
        for(int i = 0; i < 32; i++)
            asciiNonPrintableCharacters.append((char)i);
        
        return asciiNonPrintableCharacters.toString();
    }
    
    public static String AsciiPrintableCharacters()
    {
        StringBuilder asciiPrintableCharacters = new StringBuilder();
    
        for(int i = 32; i < 128; i++)
            asciiPrintableCharacters.append((char)i);
    
        return asciiPrintableCharacters.toString();
    }
    
    public static String AsciiExtendedCharacters()
    {
        StringBuilder asciiExtendedCharacters = new StringBuilder();
        
        for(int i = 128; i < 256; i++)
            asciiExtendedCharacters.append((char)i);
        
        return asciiExtendedCharacters.toString();
    }
    
    public static String AsciiAlphanumericCharacters()
    {
        return "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    }

    public static int getRandomNumber(int min, int max) {
        return (int) ((Math.random() * (max - min)) + min);
    }

    public static float getRandomNumberFloat(float min, float max) {
        return (float) ((Math.random() * (max - min)) + min);
    }

    public static long getRandomNumberLong(long min, long max) {
        return (long) ((Math.random() * (max - min)) + min);
    }

    public static double getRandomNumberDouble(double min, double max) {
        return (double) ((Math.random() * (max - min)) + min);
    }
    
    public static String AsciiAlphabeticalCharacters()
    {
        return "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    }
    
    public static String AsciiNumericalCharacters()
    {
        return "0123456789";
    }
    
    public static HttpClientBuilder DisableSSL(HttpClientBuilder httpClientBuilder)
    {
        try
        {
            httpClientBuilder.
                    setSSLContext(new SSLContextBuilder().loadTrustMaterial(null, (chain, authType) -> true).build()).
                    setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        
        return httpClientBuilder;
    }
}
