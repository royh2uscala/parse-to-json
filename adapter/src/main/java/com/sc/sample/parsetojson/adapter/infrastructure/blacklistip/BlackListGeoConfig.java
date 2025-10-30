package com.sc.sample.parsetojson.adapter.infrastructure.blacklistip;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "app.ip-block-blacklist")
public class BlackListGeoConfig {
        private List<String> isoCountries;
        private List<IspDataCenterVenueWrapper> ispDataCenterVenues;
        private int cacheExpiryMinutes;

    public List<String> getIsoCountries() {
        return isoCountries;
    }

    public void setIsoCountries(List<String> isoCountries) {
        this.isoCountries = isoCountries;
    }

    public List<IspDataCenterVenueWrapper> getIspDataCenterVenues() {
        return ispDataCenterVenues;
    }

    public void setIspDataCenterVenues(List<IspDataCenterVenueWrapper> ispDataCenterVenues) {
        this.ispDataCenterVenues = ispDataCenterVenues;
    }

    public int getCacheExpiryMinutes() {
        return cacheExpiryMinutes;
    }

    public void setCacheExpiryMinutes(int cacheExpiryMinutes) {
        this.cacheExpiryMinutes = cacheExpiryMinutes;
    }

    @Override
    public String toString() {
        return "BlackListGeoConfig{" +
                "isoCountries=" + isoCountries +
                ", ispDataCenterVenues=" + ispDataCenterVenues +
                ", cacheExpiryMinutes=" + cacheExpiryMinutes +
                '}';
    }

    static public class IspDataCenterVenueWrapper {
        private Venue venue;

        public Venue getVenue() {
            return venue;
        }

        public void setVenue(Venue venue) {
            this.venue = venue;
        }

        @Override
        public String toString() {
            return "IspDataCenterVenueWrapper{" +
                    "venue=" + venue +
                    '}';
        }
    }

    static public class Venue {
        private String code;
        private List<String> searchTokens;

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public List<String> getSearchTokens() {
            return searchTokens;
        }

        public void setSearchTokens(List<String> searchTokens) {
            this.searchTokens = searchTokens;
        }

        @Override
        public String toString() {
            return "Venue{" +
                    "code='" + code + '\'' +
                    ", searchTokens=" + searchTokens +
                    '}';
        }
    }
}
