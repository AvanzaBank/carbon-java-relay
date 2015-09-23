package com.avanza.carbon.java.relay.network;

import java.util.Objects;
import java.util.Optional;

public class CarbonEndpoint {
	

	public String getAdress() {
		return adress;
	}

	public int getPort() {
		return port;
	}

	public Optional<String> getInstance() {
		return instance;
	}

	private String adress;
	private int port;
	private Optional<String> instance;

	public CarbonEndpoint(String address, int port) {
		this(address, port, Optional.empty());
	}

	public CarbonEndpoint(String adress, int port, String instance) {
		this(adress, port, Optional.of(instance));
	}
	
	@Override
	public String toString() {
		return adress + ":" + port + (instance.isPresent() ? ":" + instance.get() : "");
	}

	private CarbonEndpoint(String adress, int port, Optional<String> instance) {
		this.adress = Objects.requireNonNull(adress);
		if (port < 0) {
			throw new IllegalArgumentException("Port must be > 0");
		}
		this.port = port;
		this.instance = instance;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((adress == null) ? 0 : adress.hashCode());
		result = prime * result + ((instance == null) ? 0 : instance.hashCode());
		result = prime * result + port;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CarbonEndpoint other = (CarbonEndpoint) obj;
		if (adress == null) {
			if (other.adress != null)
				return false;
		} else if (!adress.equals(other.adress))
			return false;
		if (instance == null) {
			if (other.instance != null)
				return false;
		} else if (!instance.equals(other.instance))
			return false;
		if (port != other.port)
			return false;
		return true;
	}
	
	

}
