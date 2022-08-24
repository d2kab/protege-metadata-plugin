# protege-metadata-plugin
Development of a Protégé plugin to edit metadata

#### Prerequisites
To build and run the examples, you must have the following items installed:
+ Apache's [Maven](http://maven.apache.org/index.html).
+ A Protege distribution (5.0.0 or higher).  The Protege 5.2.0 release is [available](http://protege.stanford.edu/products.php#desktop-protege) from the main Protege website. 
#### Build and install example plug-ins
1. Get a copy of the  code:
        git clone 
2. Change into the  directory.
3. Type mvn clean package.  On build completion, the "target" directory will contain a protege.plugin.examples-${version}.jar file.
4. Copy the JAR file from the target directory to the "plugins" subdirectory of your Protege distribution.