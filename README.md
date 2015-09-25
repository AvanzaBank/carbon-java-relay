# carbon-java-relay
carbon-java-relay is a fast and simple relay for carbon data. It provides a subset of the functionality of carbon-relay from the [Graphite project](https://github.com/graphite-project/).

carbon-java-relay uses the same configuration file as carbon-relay. If you are already using carbon-relay you can use carbon-java-relay as a drop-in replacement.

It has two main advantages over the standard carbon-relay:

* It is a lot faster
* It is multithreaded to allow utilization of several cores

### Installation

Download one of the releases and start the application using `java -jar`:

    java -jar <jarFile> <pathToCarbonConf>
    
### Configuration

carbon-java-relay reads the following properties from the configuration file specified on the command line. Properties are read from the `[relay]` section unless otherwise mentioned.

* `UDP_RECEIVER_PORT` - UDP listen port
* `LINE_RECEIVER_PORT` - TCP listen port
* `DESTINATIONS` - carbon-cache endpoints to send metrics to. Accepts a comma-separated list with `host:port:instanceId`. `instnaceId` is optional since consistent hashing is not supported.
* `LOG_DIR` - directory for logging. A file named `carbon-java-relay.log` will created and logged to. Will be read from the `[cache]` section if it does not exist in the `[relay]` section.

### Requirements

Requires Java 8.

### Functionality

carbon-java-relay listens for metrics in line format on both UDP and TCP. The data is sent using the pickle format to a number of carbon-cache instances.

### Building

Standard build using maven, run `mvn package`. An executable jar will be created with classifier `executable`.

### Limitations

* carbon-java-relay does currently not implement the consistent hashing method for routing metrics. Which cache instance that received a metric is random. Hence carbon-java-relay is basically only useful in setups where all carbon-cache instances write to the same storage. This also means that if you use graphite-web, it will not always find the metrics in the carbon-cache in-memory caches but will have to read from disk.

* Currently one thread per carbon-cache instance is used for sending metrics. This might limit your performance, but from our experience you are probably better off increasing the number of carbon-cache instances if you need more concurrency.

* relay-rules or aggregation is not supported.

* Does not support receiving metrics in the pickle format.

* Probably more, we have only tested it in our simple setup.

### Reason for development

We ran a single-host graphite setup where a carbon-relay distributed the load to five carbon-cache instances. We had about 3 million metrics per minute but the carbon-relay dropped about two thirds of the metrics since it was running with 100% CPU usage.

There are some other replacement for carbon-relay but we could either not get them to work or they seemed bloated with functionality we did not need. Hence we decided to roll our own solution in the programming language we are most confident with. This way we could make the implementation as simple as possible as well as support drop-in replacement.

