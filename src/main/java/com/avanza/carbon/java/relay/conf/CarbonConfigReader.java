package com.avanza.carbon.java.relay.conf;

import static java.util.stream.Collectors.*;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;

import com.avanza.carbon.java.relay.network.CarbonEndpoint;

public class CarbonConfigReader {

	private InputStream inputStream;

	public CarbonConfigReader(InputStream inputStream) {
		this.inputStream = Objects.requireNonNull(inputStream);
	}
	
	public RelayConfig readConfig() {
		String contents = convertStreamToString(inputStream);
		Map<String, String> relayProperties = getSectionProperties("relay", contents);
		int udpListenPort = getUdpListenPort(relayProperties);
		List<CarbonEndpoint> endpoints = getEndpoints(relayProperties);
		Map<String, String> cacheProperties = getSectionProperties("cache", contents);
		String logDir = getWithFallback(relayProperties, cacheProperties, "LOG_DIR");
		if (logDir == null) {
			System.err.println("LOG_DIR not set, will use cwd");
			logDir = ".";
		}
		return new RelayConfig(udpListenPort, endpoints, logDir);
	}


	private String getWithFallback(Map<String, String> tryFirst, Map<String, String> trySecond,
			String key) {
		return tryFirst.getOrDefault(key, trySecond.get(key));
	}

	private List<CarbonEndpoint> getEndpoints(Map<String, String> relayProperties) {
		String destString = getRequiredPropety(relayProperties, "DESTINATIONS");
		String[] endPoints = destString.split(",");
		return Arrays.stream(endPoints).map(this::toEndpoint).collect(toList());
	}
	
	private CarbonEndpoint toEndpoint(String s) {
		String[] split = s.trim().split(":");
		if (split.length == 2) {
			return new CarbonEndpoint(split[0], Integer.parseInt(split[1]));
		} else if (split.length == 3) {
			return new CarbonEndpoint(split[0], Integer.parseInt(split[1]), split[2]);
		}
		throw new RuntimeException("Not a valid endpoint description: " + s);
	}


	private int getUdpListenPort(Map<String, String> relayProperties) {
		String udpListenPortString = getRequiredPropety(relayProperties, "UDP_RECEIVER_PORT");
		int udpListenPort = Integer.parseInt(udpListenPortString);
		return udpListenPort;
	}

	
	private String getRequiredPropety(Map<String, String> relayProperties, String prop) {
		String res = relayProperties.get(prop);
		if (res == null) {
			throw new RuntimeException("Missing property in config: " + prop);
		}
		return res;
	}


	private Map<String, String> getSectionProperties(String section, String contents) {
		String[] lines = contents.split("[\\r\\n]+");
		List<String> sectionLines = getSectionLines(section, lines);
		Map<String, String> m = sectionLines.stream()
				.filter(s -> !s.trim().startsWith("#"))
				.filter(s -> !s.trim().isEmpty())
				.filter(s -> s.contains("="))
				.collect(toMap(s -> s.split("=")[0].trim(), s -> s.split("=")[1].trim()));
		return m;
		
	}


	private List<String> getSectionLines(String section, String[] lines) {
		List<String> sectionLines = new ArrayList<>();
		boolean inSection = false;
		for (String line : lines) {
			if (line.matches("\\[.*\\]")) {
				inSection = false;
			}
			if (inSection) {
				sectionLines.add(line);
			}
			if (line.trim().equals("[" + section + "]")) {
				inSection = true;
			}
		}
		return sectionLines;
	}


	private static String convertStreamToString(java.io.InputStream is) {
		try (Scanner s = new Scanner(is)) {
			s.useDelimiter("\\A");
			return s.hasNext() ? s.next() : "";
		}
	}
}
