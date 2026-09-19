package com.storix.api.config.swagger;

import java.util.List;

public record WebsocketErrorSpec(List<String> reasons, List<ErrorSpec.Entry> codes) {}
