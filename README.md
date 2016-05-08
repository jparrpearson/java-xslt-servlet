# java-xslt-servlet

This is a Java Servlet that executes an XSL Transformation (XSLT).

## Build

This project uses [Gradle](http://gradle.org/), and the following tasks assume that it is already installed.

### Resolve Dependencies

If editing in Eclipse, first resolve dependencies and place them in the `lib` folder (ensure that all needed dependencies are also listed in the `.classpath` file, and run this task before opening the project in Eclipse):
```
gradle prepare
```

### Build the WAR

If deploying to a web container other than Jetty, generate the WAR file (can then be found under the `build/libs` folder):
```
gradle war
```

Note that the WAR `manifest` properties can be set under the `build.gradle` file.

### Deploy with Jetty

In order to build the application and deploy to a Jetty container:
```
gradle jettyRun
```

Note that the `httpPort` can be set under the `build.gradle` file.

## Usage

After building and deploying to a web container, access the XSLT Tester page from the root of the application, for example:
```
http://localhost:8080/java-xslt-servlet
```

An example `source document` and `xslt stylesheet` are provided.  Modify or provide your own by either pasting the files in the text boxes, or by selecting the `file` option and choosing the files from your computer.

Optional `parameters` may also be provided (name/value pairs), which will be passed to the XSLT.

The resulting document can be viewed in the result text box, or in the browser itself (replaces the current page).

Note that all values in the form, with the exception of file references, will be preserved using the HTML5 `localStorage` object.  This means that even after navigating to other pages, or closing the browser, the values will remain in memory for next use.

## License

Copyright (c) 2016 Jeremy Parr-Pearson

The MIT License (MIT)
