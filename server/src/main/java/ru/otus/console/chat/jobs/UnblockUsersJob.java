package ru.otus.console.chat.jobs;

import ru.otus.console.chat.Server;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class UnblockUsersJob implements Job {
    private final Server server;
    private final ScheduledExecutorService service;

    public UnblockUsersJob(Server server) {
        this.server = server;
        this.service = Executors.newSingleThreadScheduledExecutor();
    }

    @Override
    public void run() {
        service.scheduleWithFixedDelay((Runnable) () -> {
            Set<String> users = server.getAuthenticationProvider().getUsersForUnblock();
            for (String userName : users) {
                server.getAuthenticationProvider().blockOrUnblockUser("N", 0, userName);
            }
        }, 0, 10, TimeUnit.SECONDS );
    }

    @Override
    public void stop() {
        service.shutdown();
    }
}
