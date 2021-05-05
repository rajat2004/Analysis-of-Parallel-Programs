package utils;

public enum PEGNodeType {
    NORMAL,
    THREAD_BEGIN,
    THREAD_END,
    THREAD_START,
    THREAD_JOIN,
    SYNC_ENTRY,
    SYNC_EXIT,
    WAIT,
    WAITING,
    NOTIFIED_ENTRY,
    NOTIFY_ALL,
    NOTIFY,

    WHILE,
    IF,
    ELSE,
}
