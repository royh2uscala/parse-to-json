package com.sc.sample.parsetojson.adapter.infrastructure.filter.context;

public final class RequestContextHolder {
    public static final String IP_INFO_RESPONSE_KEY = "IP_INFO_RESPONSE_KEY";
    public static final String REQUEST_ARRIVE_TIME_KEY = "REQUEST_ARRIVE_TIME_KEY";

    private static final ThreadLocal<IpApiResponse> CONTEXT = new ThreadLocal<>();

    private RequestContextHolder() {}

    public static void set(IpApiResponse context) {
        CONTEXT.set(context);
    }

    public static IpApiResponse get() {
        return CONTEXT.get();
    }

    public static void clear() {
        CONTEXT.remove();
    }
}
