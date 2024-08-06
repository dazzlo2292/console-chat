package ru.otus.console.chat.auth;

import ru.otus.console.chat.ClientHandler;

import java.util.Timer;
import java.util.TimerTask;

public class CheckAfkJob implements Job{
    private final ClientHandler clientHandler;
    private final Timer timer;
    private long lastActivity;
    private final int secondLimit;

    public CheckAfkJob(ClientHandler clientHandler, int secondLimit) {
        this.clientHandler = clientHandler;
        this.timer = new Timer();
        this.lastActivity = System.currentTimeMillis();
        this.secondLimit = secondLimit;
    }

    public void resetLastActivity() {
        this.lastActivity = System.currentTimeMillis();
    }

    @Override
    public void run() {
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (System.currentTimeMillis() - lastActivity >= (long)secondLimit * 1000) {
                    clientHandler.send("/you_afk");
                    stop();
                }
            }
        }, 0, 1000);
    }

    @Override
    public void stop() {
        timer.cancel();
    }
}
