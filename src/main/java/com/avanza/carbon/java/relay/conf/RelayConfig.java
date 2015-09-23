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
 */package com.avanza.carbon.java.relay.conf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.avanza.carbon.java.relay.network.CarbonEndpoint;

/**
 * @author Kristoffer Erlandsson
 */
public class RelayConfig {

	private final int udpListenPort;
	private final List<CarbonEndpoint> endpoints;
	private final String logDir;


	public RelayConfig(int udpListenPort, List<CarbonEndpoint> endpoints, String logDir) {
		this.udpListenPort = udpListenPort;
		this.logDir = Objects.requireNonNull(logDir);
		this.endpoints = Collections.unmodifiableList(new ArrayList<>(Objects.requireNonNull(endpoints)));
	}
	
	public int getUdpListenPort() {
		return udpListenPort;
	}

	public List<CarbonEndpoint> getDestinations() {
		return endpoints;
	}

	public String getLogDir() {
		return logDir;
	}

}
