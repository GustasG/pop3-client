package org.guge.pop3.models;

/**
 * Information That is returned after LIST [msg] POP3 request
 * @param number message-number of the message
 * @param size exact size of message in octets
 */
public record MessageInfoModel(int number, int size) {
}
