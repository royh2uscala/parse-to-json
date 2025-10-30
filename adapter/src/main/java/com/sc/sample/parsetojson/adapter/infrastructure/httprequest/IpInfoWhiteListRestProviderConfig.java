package com.sc.sample.parsetojson.adapter.infrastructure.httprequest;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "app.ip-info-white-list")
public class IpInfoWhiteListRestProviderConfig implements IpCacheRefreshCronScheduleConfig {

    private String ipCacheRefreshCronSchedule;
    private List<RestProviderVenue> restProviderVenues;

    public List<RestProviderVenue> getRestProviderVenues() {
        return restProviderVenues;
    }

    @Override
    public String getIpCacheRefreshCronSchedule() {
        return ipCacheRefreshCronSchedule;
    }
    public void setRestProviderVenues(List<RestProviderVenue> restProviderVenues) {
        this.restProviderVenues = restProviderVenues;
    }

    public void setIpCacheRefreshCronSchedule(String ipCacheRefreshCronSchedule) {
        this.ipCacheRefreshCronSchedule = ipCacheRefreshCronSchedule;
    }

    @Override
    public String toString() {
        return "IpInfoWhiteListRestProviderConfig{" +
                "ipCacheRefreshCronSchedule='" + ipCacheRefreshCronSchedule + '\'' +
                ", restProviderVenues=" + restProviderVenues +
                '}';
    }

    static public class RestProviderVenue implements WhiteListRestProviderConfig {
        String name;
        String restBaseUrl;
        boolean activeProvider;

        @Override public String getName() { return name; }
        @Override public String getRestBaseUrl() { return restBaseUrl; }
        @Override public boolean isActiveProvider() { return activeProvider; }

        public void setName(String name) {
            this.name = name;
        }

        public void setRestBaseUrl(String restBaseUrl) {
            this.restBaseUrl = restBaseUrl;
        }

        public void setActiveProvider(boolean activeProvider) {
            this.activeProvider = activeProvider;
        }

        @Override
        public String toString() {
            return "RestProviderVenue{" +
                    "name='" + name + '\'' +
                    ", restBaseUrl='" + restBaseUrl + '\'' +
                    ", activeProvider=" + activeProvider +
                    '}';
        }
    }

}
