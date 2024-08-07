package ru.otus.console.chat.jobs;

import ru.otus.console.chat.ClientHandler;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CheckAfkJob implements Job {
    private final ClientHandler clientHandler;
    private final ScheduledExecutorService service;
    private long lastActivity;
    private final int timeMillisLimit;

    public CheckAfkJob(ClientHandler clientHandler, int secondLimit) {
        this.clientHandler = clientHandler;
        this.service = Executors.newSingleThreadScheduledExecutor();
        this.lastActivity = System.currentTimeMillis();
        this.timeMillisLimit = secondLimit * 1000;
    }

    public void resetLastActivity() {
        this.lastActivity = System.currentTimeMillis();
    }

    @Override
    public void run() {
        service.scheduleWithFixedDelay((Runnable) ()-> {
            if (System.currentTimeMillis() - lastActivity >= timeMillisLimit) {
                clientHandler.send("/you_afk");
                stop();
            }
        }, 0, 10, TimeUnit.SECONDS);

    }

    @Override
    public void stop() {
        service.shutdown();
    }
}
