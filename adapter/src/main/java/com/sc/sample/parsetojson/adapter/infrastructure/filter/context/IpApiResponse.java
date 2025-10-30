package com.sc.sample.parsetojson.adapter.infrastructure.filter.context;

public record IpApiResponse(
        String query,
        String status,
        String country,
        String countryCode,
        String isp,
        String org,
        String as
) {
    @Override
    public String toString() {
        return "IpApiResponse{" +
                "query='" + query + '\'' +
                ", status='" + status + '\'' +
                ", country='" + country + '\'' +
                ", countryCode='" + countryCode + '\'' +
                ", isp='" + isp + '\'' +
                ", org='" + org + '\'' +
                ", as='" + as + '\'' +
                '}';
    }
}
