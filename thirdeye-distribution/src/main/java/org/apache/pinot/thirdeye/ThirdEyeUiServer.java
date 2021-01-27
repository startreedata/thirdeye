package org.apache.pinot.thirdeye;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.eclipse.jetty.proxy.ConnectHandler;
import org.eclipse.jetty.proxy.ProxyServlet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.handler.gzip.GzipHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThirdEyeUiServer {

  private static final Logger log = LoggerFactory.getLogger(ThirdEyeUiServer.class);

  private static final String ARG_PROXY_HOST_PORT = "proxyHostPort";
  private static final String ARG_PORT = "port";
  private static final String ARG_RESOURCE_BASE = "resourceBase";

  private final int port;
  private final String proxyHostPort;
  private final String resourceBase;

  public ThirdEyeUiServer(final int port, final String proxyHostPort, final String resourceBase) {
    this.port = port;
    this.proxyHostPort = proxyHostPort;
    this.resourceBase = resourceBase;
  }

  public static void main(String[] args) throws ParseException {
    final CommandLine cmd = getCommandLine(args);
    new ThirdEyeUiServer(
        Integer.parseInt(cmd.getOptionValue(ARG_PORT, "8081")),
        cmd.getOptionValue(ARG_PROXY_HOST_PORT, "localhost:8080"),
        cmd.getOptionValue(ARG_RESOURCE_BASE, "./thirdeye-ui/dist")
    ).run();
  }

  private static CommandLine getCommandLine(final String[] args) throws ParseException {
    final Options options = new Options();
    options.addOption("p", ARG_PORT, true, "The server port");
    options.addOption("r", ARG_PROXY_HOST_PORT, true, "The proxy to location");
    options.addOption("b", ARG_RESOURCE_BASE, true,
        "Path to directory containing the React build. (has index.html)");

    return ((CommandLineParser) new DefaultParser()).parse(options, args);
  }

  public void run() {
    log.info("Starting server. port: {}, proxyHostPort: {}", port, proxyHostPort);
    final Server server = createServer();

    try {
      server.start();
      server.join();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private Server createServer() {
    final Server server = new Server(port);

    final ResourceHandler resourceHandler = createResourceHandler();
    final ConnectHandler proxy = createProxyHandler();

    final GzipHandler gzip = new GzipHandler();
    gzip.setHandler(new HandlerList(resourceHandler, proxy, new DefaultHandler()));
    server.setHandler(gzip);

    return server;
  }

  private ResourceHandler createResourceHandler() {
    final ResourceHandler resourceHandler = new ResourceHandler();
    resourceHandler.setDirectoriesListed(true);
    resourceHandler.setWelcomeFiles(new String[]{"index.html"});
    resourceHandler.setResourceBase(resourceBase);
    return resourceHandler;
  }

  private ConnectHandler createProxyHandler() {
    // Setup proxy handler to handle CONNECT methods
    final ConnectHandler proxy = new ConnectHandler();

    // Setup proxy servlet
    final ServletContextHandler context = new ServletContextHandler(proxy, "/api/",
        ServletContextHandler.SESSIONS);

    final ServletHolder proxyServlet = new ServletHolder(ProxyServlet.Transparent.class);
    proxyServlet.setInitParameter("proxyTo", String.format("http://%s/api", proxyHostPort));
    context.addServlet(proxyServlet, "/*");
    return proxy;
  }
}
