package ru.otus.console.chat.jobs;

import ru.otus.console.chat.Server;

import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class UnblockUsersJob implements Job {
    private final Server server;
    private final Timer timer;

    public UnblockUsersJob(Server server) {
        this.server = server;
        this.timer = new Timer();
    }

    @Override
    public void run() {
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                Set<String> users = server.getAuthenticationProvider().getUsersForUnblock();
                for (String userName : users) {
                    server.getAuthenticationProvider().blockOrUnblockUser("N", 0, userName);
                }
                Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, 0, 1000);
    }

    @Override
    public void stop() {
        timer.cancel();
    }
}
