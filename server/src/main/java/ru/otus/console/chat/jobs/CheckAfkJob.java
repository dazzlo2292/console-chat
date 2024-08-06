package ru.otus.console.chat.jobs;

import ru.otus.console.chat.ClientHandler;

import java.util.Timer;
import java.util.TimerTask;

public class CheckAfkJob implements Job {
    private final ClientHandler clientHandler;
    private final Timer timer;
    private long lastActivity;
    private final int timeMillisLimit;

    public CheckAfkJob(ClientHandler clientHandler, int secondLimit) {
        this.clientHandler = clientHandler;
        this.timer = new Timer();
        this.lastActivity = System.currentTimeMillis();
        this.timeMillisLimit = secondLimit * 1000;
    }

    public void resetLastActivity() {
        this.lastActivity = System.currentTimeMillis();
    }

    @Override
    public void run() {
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (System.currentTimeMillis() - lastActivity >= timeMillisLimit) {
                    clientHandler.send("/you_afk");
                    stop();
                }
                try {
                    Thread.sleep(30000);
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
