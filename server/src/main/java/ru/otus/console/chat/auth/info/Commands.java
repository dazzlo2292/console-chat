package ru.otus.console.chat.auth.info;

public class Commands {
    public final static String COMMANDS_BEFORE_AUTH = """
            -auth    — Authentication user. Format: /auth {login} {password}
            -reg     — Registration user.   Format: /reg {login} {password} {userName}""";

    public final static String COMMANDS_AFTER_AUTH = """
            -w          — Send a private message.              Format: /w {userName} {message}
            -exit       — Log out.                             Format: /exit
            -activelist — Get all active users                 Format: /activelist
            -changenick — Change your userName                 Format: /changenick {newUserName}
            
            Only for Admin:
            -ban     — Kick out user of the chat and block.
            ! If days is empty — permanent ban !             Format: /ban {userName} [days]
            -unblock  — Unblock user.                        Format: /unblock {userName}
            -shutdown — Shutdown server                      Format: /shutdown""";
}
