package com.sc.sample.parsetojson.adapter.infrastructure.httprequest;


import com.sc.sample.parsetojson.adapter.infrastructure.blacklistip.BlackListConfigException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Optional;


@Configuration
public class IPWhiteListRestClientConfig {

    private final IpInfoWhiteListRestProviderConfig ipInfoWhiteListRestProviderConfig;

    public IPWhiteListRestClientConfig(
            IpInfoWhiteListRestProviderConfig ipInfoWhiteListRestProviderConfig) {
        this.ipInfoWhiteListRestProviderConfig = ipInfoWhiteListRestProviderConfig;
    }
    @Bean
    public RestClient ipProviderRestClient(RestClient.Builder builder) {
        List<IpInfoWhiteListRestProviderConfig.RestProviderVenue> restProviderVenues =
                ipInfoWhiteListRestProviderConfig.getRestProviderVenues();

        System.out.println("ipProviderRestClient called01" + restProviderVenues);

        Optional<IpInfoWhiteListRestProviderConfig.RestProviderVenue> result =
                restProviderVenues.stream()
                .filter(restProviderVenue ->
                        isValidRestProviderVenue(restProviderVenue)
                        && restProviderVenue.isActiveProvider())
                .findFirst();

        if (!result.isPresent()) {
            System.out.println("Not Found or Invalid configuration for white list IP REST providers");
            System.out.println("ipInfoWhiteListRestProviderConfig=%s"
                    .formatted(String.valueOf(ipInfoWhiteListRestProviderConfig)));
            throw new BlackListConfigException(
                    "Not Found or Invalid configuration for white list IP REST providers");
        } else {
            IpInfoWhiteListRestProviderConfig.RestProviderVenue restProviderVenue = result.get();
            System.out.println("Successful Config found white list IP REST provider ->%s"
                    .formatted(restProviderVenue));
            return builder
                .baseUrl(restProviderVenue.getRestBaseUrl()) // use injected config
                .build();
        }
    }

    private static boolean isValidRestProviderVenue(
            IpInfoWhiteListRestProviderConfig.RestProviderVenue restProviderVenue) {
        if(!restProviderVenue.getRestBaseUrl().isBlank()
                && isValidBaseURL(restProviderVenue.getRestBaseUrl())
                && !restProviderVenue.getName().isBlank() )
            return true;
        else {
            System.out.println(
                    "Invalid configuration for white list IP REST provider -> %s"
                            .formatted(restProviderVenue));
            return false;
        }
    }

    private static boolean isValidBaseURL(String baseURL) {
        try {
            new URL(baseURL).toURI();
            return true;
        } catch (MalformedURLException | URISyntaxException e) {
            System.out.println("Invalid config for White list IP REST provider baseURL:%s"
                    .formatted(baseURL));
            return false;
        }
    }
}
