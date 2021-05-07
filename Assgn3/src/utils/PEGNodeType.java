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

    // Custom-defined nodes
    WHILE,
    WHILE_JUMP,
    WHILE_END,
    IF,
    IF_END,
    ELSE,
    IF_ELSE_END,
}
