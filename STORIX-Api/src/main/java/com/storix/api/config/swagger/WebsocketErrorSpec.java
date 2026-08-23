package com.storix.api.config.swagger;

import java.util.List;

public record WebsocketErrorSpec(List<Frame> frames, List<ErrorSpec.Entry> codes) {

    public record Frame(String message, String source) {}
}
