## NB: Currently under development, stay tuned for the first release

# carbon-java-relay
carbon-java-relay is a fast and simple relay for carbon data. It provides a subset of the functionality of carbon-relay from the [Graphite project](https://github.com/graphite-project/).

carbon-java-relay uses the same configuration file as carbon-relay. If you are already using carbon-relay you can use carbon-java-relay as a drop-in replacement.

It has two main advantages over the standard carbon-relay:

* It is a lot faster
* It is multithreaded to allow utilization of several cores

### Installation

Download one of the releases and start the application using `java -jar`:

    java -jar <jarFile> <pathToCarbonConf>
    
### Building

Standard build using maven, run `mvn package`. An executable jar will be created with classifier `executable`.

### Functionality

carbon-java-relay listens for metrics in line format on both UDP and TCP. The data is sent using the pickle format to a number of carbon-cache instances.

### Limitations

* carbon-java-relay does currently not implement the consistent hashing method for routing metrics. Which cache instance that received a metric is random. Hence carbon-java-relay is basically only useful in setups where all carbon-cache instances write to the same storage. This also means that if you use graphite-web, it will not always find the metrics in the carbon-cache in-memory caches but will have to read from disk.

* Currently one thread per carbon-cache instance is used for sending metrics. This might limit your performance, but from our experience you are probably better off increasing the number of carbon-cache instances if you need more concurrency.

### Reason for development

We ran a single-host graphite setup where a carbon-relay distributes the load to five carbon-cache instances. We had about 3 million metrics per minute but the graphite-relay dropped about two thirds of the metrics.

There are some other replacement for carbon-relay but we could either not get them to work or they seemed bloated with functionality we did not need. Hence we decided to roll our own solution in the programming language we are most confident with. This way we could make the implementation as simple as possible as well as support drop-in replacement.

