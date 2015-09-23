package com.avanza.carbon.java.relay.conf;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.InputStream;

import org.junit.Test;

import com.avanza.carbon.java.relay.network.CarbonEndpoint;

public class CarbonConfigReaderTest {

	@Test
	public void test() {
		InputStream inputStream = CarbonConfigReaderTest.class.getResourceAsStream("/test-carbon.conf");
		CarbonConfigReader carbonConfig = new CarbonConfigReader(inputStream);
		RelayConfig config = carbonConfig.readConfig();
		assertThat(config.getUdpListenPort(), equalTo(2003));
		assertThat(config.getDestinations(), contains(new CarbonEndpoint("127.0.0.1", 2014, "a"), new CarbonEndpoint("127.0.0.1", 2106)));
		assertThat(config.getLogDir(), equalTo("/var/log/graphite"));
		assertThat(config.getLineReceiverPort(), equalTo(9999));
	}

}
