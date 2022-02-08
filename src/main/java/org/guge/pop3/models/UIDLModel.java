package org.guge.pop3.models;

/**
 * Information That is returned after UIDL [msg] POP3 request
 * @param number message-number of the message
 * @param id unique-id of the message
 */
public record UIDLModel(int number, String id) {
}
