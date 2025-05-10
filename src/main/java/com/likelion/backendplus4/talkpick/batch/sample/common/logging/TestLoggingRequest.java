package com.likelion.backendplus4.talkpick.batch.sample.common.logging;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Data
public class TestLoggingRequest {
	private final String name;
	private final int age;
}
