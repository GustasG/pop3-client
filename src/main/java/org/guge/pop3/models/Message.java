package org.guge.pop3.models;

import java.util.Map;

public record Message(Map<String, String> headers, String body) {
}
