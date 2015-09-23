package com.avanza.carbon.java.relay.conf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.avanza.carbon.java.relay.network.CarbonEndpoint;

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
