package ru.otus.console.chat.auth.info;

public class Commands {
    public final static String COMMANDS_BEFORE_AUTH = """
            -auth    — Authentication user. Format: /auth {login} {password}
            -reg     — Registration user.   Format: /reg {login} {password} {userName}""";

    public final static String COMMANDS_AFTER_AUTH = """
            -w       — Send a private message.              Format: /w {userName} {message}
            -exit    — Log out.                             Format: /exit
            
            Only for Admin:
            -kick    — Kick out user of the chat and block. Format: /kick {userName}
            -unblock — Unblock user.                        Format: /unblock {userName}""";
}
