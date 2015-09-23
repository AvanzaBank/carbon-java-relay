/*
 * Copyright 2015 Avanza Bank AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.avanza.carbon.java.relay;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.regex.Pattern;

public class MetricTuple {
	private static final Pattern SPLIT_PATTERN = Pattern.compile("  *");
	private final String name;
	private final long timestamp;
	private final Number value;

	public MetricTuple(String name, Number value, long timestamp) {
		this.name = Objects.requireNonNull(name);
		this.timestamp = Objects.requireNonNull(timestamp);
		this.value = Objects.requireNonNull(value);
	}

	public String getName() {
		return name;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public Number getValue() {
		return value;
	}

	public static MetricTuple fromString(String string) {
		String[] split = SPLIT_PATTERN.split(string.trim());
		return new MetricTuple(split[0], new BigDecimal(split[1]), Long.parseLong(split[2]));
	}

	@Override
	public String toString() {
		return String.format("%s %s %s", name, value, timestamp);
	}
}