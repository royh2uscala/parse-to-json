package com.sc.sample.parsetojson.adapter.infrastructure.httprequest;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;

@Configuration
@EnableScheduling
@ConfigurationProperties(prefix = "app.ip-info-white-list")
public class IPInfoCacheRefreshScheduler implements SchedulingConfigurer {

    private final IpCacheRefreshCronScheduleConfig ipCacheRefreshCronScheduleConfig;
    private final RefreshIpIPCache refreshIpIPCache;

    public IPInfoCacheRefreshScheduler(
            IpCacheRefreshCronScheduleConfig ipCacheRefreshCronScheduleConfig,
            RefreshIpIPCache refreshIpIPCache) {
        this.ipCacheRefreshCronScheduleConfig = ipCacheRefreshCronScheduleConfig;
        this.refreshIpIPCache = refreshIpIPCache;

        System.out.println("%s constructor -> IpCacheRefreshCronSchedule:%s"
                .formatted(IPInfoCacheRefreshScheduler.class.getSimpleName(),
                        ipCacheRefreshCronScheduleConfig.getIpCacheRefreshCronSchedule()));
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.addTriggerTask(
                refreshIpIPCache::refreshIpCache,
                triggerContext -> new CronTrigger(
                        ipCacheRefreshCronScheduleConfig.getIpCacheRefreshCronSchedule())
                                .nextExecution(triggerContext)
        );
    }
}
