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

    public static IpApiResponse copy(IpApiResponse o) {
        return new IpApiResponse(o.query, o.status, o.country, o.countryCode, o.isp, o.org, o.as);
    }
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
