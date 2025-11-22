package io.edurt.datacap.server.runner;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.edurt.datacap.plugin.PluginManager;
import io.edurt.datacap.scheduler.SchedulerRequest;
import io.edurt.datacap.scheduler.SchedulerService;
import io.edurt.datacap.service.enums.SyncMode;
import io.edurt.datacap.service.initializer.job.DatasetJob;
import io.edurt.datacap.service.repository.DataSetRepository;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Scheduler;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@SuppressFBWarnings(value = {"EI_EXPOSE_REP", "EI_EXPOSE_REP2"})
public class SchedulerRunner
        implements CommandLineRunner
{
    private final PluginManager pluginManager;
    private final DataSetRepository repository;
    private final Scheduler scheduler;

    public SchedulerRunner(PluginManager pluginManager, DataSetRepository repository, Scheduler scheduler)
    {
        this.pluginManager = pluginManager;
        this.repository = repository;
        this.scheduler = scheduler;
    }

    @Override
    public void run(String... args)
    {
        log.info("Start scheduler initializer");
        repository.findAllBySyncMode(SyncMode.TIMING)
                .forEach(item -> {
                    log.info("Dataset [ {} ] will be scheduled", item.getName());
                    pluginManager.getPlugin(item.getScheduler())
                            .ifPresentOrElse(
                                    plugin -> {
                                        SchedulerRequest request = SchedulerRequest.builder()
                                                .name(item.getCode())
                                                .group("datacap")
                                                .expression(item.getExpression())
                                                .jobId(item.getCode())
                                                .createBeforeDelete(true)
                                                .build();

                                        SchedulerService schedulerService = plugin.getService(SchedulerService.class);
                                        if (item.getScheduler().equalsIgnoreCase("LocalScheduler")) {
                                            request.setJob(new DatasetJob());
                                            request.setScheduler(this.scheduler);
                                        }
                                        schedulerService.initialize(request);
                                    },
                                    () -> log.error("Scheduler [ {} ] not found", item.getScheduler())
                            );
                });
        log.info("End scheduler initializer");
    }
}
