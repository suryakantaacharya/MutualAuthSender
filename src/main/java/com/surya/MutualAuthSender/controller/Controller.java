package com.surya.MutualAuthSender.controller;

import java.io.FileInputStream;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManagerFactory;

import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import io.netty.handler.ssl.SslContextBuilder;
import reactor.netty.http.client.HttpClient;

@RestController
public class Controller {
	
	@GetMapping(value = "/send")
	public String testAuth() {
		
        KeyStore keyStore = loadKeyStore("/home/surya/Developer/KeyGenClient/client-keystore.p12", "suryatest");

		
        KeyStore trustStore = loadKeyStore("/home/surya/Developer/KeyGenClient/client-truststore.p12", "suryatest");

        KeyManagerFactory keyManagerFactory = getKeyManagerFactory(keyStore, "suryatest");

        TrustManagerFactory trustManagerFactory = getTrustManagerFactory(trustStore);

		HttpClient httpClient = HttpClient.create().secure(sslContextSpec -> {
			try {
				sslContextSpec.sslContext(SslContextBuilder.forClient().keyManager(keyManagerFactory)
						.trustManager(trustManagerFactory).build());
			} catch (SSLException e) {
				e.printStackTrace();
			}
		});
		
		WebClient webClient = WebClient.builder().clientConnector(new ReactorClientHttpConnector(httpClient))
				.baseUrl("https://localhost:8080") 
				.build();
		
        String responseBody = webClient.get()
                .uri("/recieve") 
                .retrieve()
                .bodyToMono(String.class)
                .block(); 

		
		
		
		return responseBody;
	}
	
    private static KeyStore loadKeyStore(String keystorePath, String keystorePassword) {
        try (FileInputStream fis = new FileInputStream(keystorePath)) {
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(fis, keystorePassword.toCharArray());
            return keyStore;
        } catch (Exception e) {
            throw new RuntimeException("Error loading keystore: " + e.getMessage(), e);
        }
    }
    
    private static KeyManagerFactory getKeyManagerFactory(KeyStore keyStore, String keyPassword) {
        try {
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, keyPassword.toCharArray());
            return keyManagerFactory;
        } catch (Exception e) {
            throw new RuntimeException("Error initializing KeyManagerFactory: " + e.getMessage(), e);
        }
    }

    private static TrustManagerFactory getTrustManagerFactory(KeyStore trustStore) {
        try {
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(trustStore);
            return trustManagerFactory;
        } catch (Exception e) {
            throw new RuntimeException("Error initializing TrustManagerFactory: " + e.getMessage(), e);
        }
    }

}
