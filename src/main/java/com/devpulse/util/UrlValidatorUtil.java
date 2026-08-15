package com.devpulse.util;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;

public class UrlValidatorUtil {

    public static void validateUrl(String urlString) {
        if (urlString == null || urlString.isBlank()) {
            throw new IllegalArgumentException("URL cannot be empty");
        }
        
        try {
            URI uri = new URI(urlString);
            String scheme = uri.getScheme();
            if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
                throw new IllegalArgumentException("Only HTTP and HTTPS protocols are supported");
            }
            
            String host = uri.getHost();
            if (host == null) {
                throw new IllegalArgumentException("Invalid URL format");
            }
            
            // Check against SSRF by resolving host and checking if it's a private IP
            InetAddress inetAddress = InetAddress.getByName(host);
            if (inetAddress.isAnyLocalAddress() || 
                inetAddress.isLoopbackAddress() || 
                inetAddress.isLinkLocalAddress() || 
                inetAddress.isSiteLocalAddress() ||
                inetAddress.isMulticastAddress()) {
                throw new IllegalArgumentException("URLs pointing to internal/private networks are not allowed");
            }
            
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("Unresolvable host in URL");
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid URL: " + e.getMessage());
        }
    }
}
