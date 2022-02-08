package org.guge.pop3.models;

/**
 * Information That is returned after STAT POP3 request
 * @param messageCount number of messages in the maildrop
 * @param totalSize size of mail drop in octets
 */
public record StatsModel(int messageCount, int totalSize) {
}