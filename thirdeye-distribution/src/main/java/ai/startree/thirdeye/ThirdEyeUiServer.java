/*
 * Copyright 2023 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
package ai.startree.thirdeye;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.eclipse.jetty.proxy.ConnectHandler;
import org.eclipse.jetty.proxy.ProxyServlet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ErrorPageErrorHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.util.thread.ThreadPool;
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
    log.info("Starting server. port: {}, proxyHostPort: {}, resourceBase: {}",
        port,
        proxyHostPort,
        resourceBase);
    final Server server = createServer();

    try {
      server.start();
      server.join();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private Server createServer() {
    final ThreadPool threadPool = new QueuedThreadPool();
    final Server server = new Server(threadPool);

    final ServerConnector connector = new ServerConnector(server);
    connector.setPort(port);
    server.addConnector(connector);

    final ServletContextHandler context = new ServletContextHandler();
    context.setContextPath("/");
    context.setWelcomeFiles(new String[]{"index.html"});
    context.addAliasCheck(new ContextHandler.ApproveAliases());


    // Lastly, the default servlet for resource base content (serves static files)
    // It is important that this is last.
    final ServletHolder defHolder = createDefaultServlet();
    context.addServlet(defHolder, "/");

    final ErrorPageErrorHandler errorHandler = new ErrorPageErrorHandler();
    errorHandler.addErrorPage(404, "/");
    context.setErrorHandler(errorHandler);

    final ConnectHandler proxyHandler = createProxyHandler();
    server.setHandler(new HandlerList(proxyHandler, context));

    return server;
  }

  private ServletHolder createDefaultServlet() {
    final ServletHolder defHolder = new ServletHolder("default", DefaultServlet.class);
    // Cannot be null or empty, must be declared, must be a directory, can be a URL to some jar content
    defHolder.setInitParameter("resourceBase", resourceBase);
    defHolder.setInitParameter("dirAllowed", "true");
    defHolder.setInitParameter("gzip", "true");
    defHolder.setInitParameter("otherGzipFileExtensions", ".svgz");
    defHolder.setInitParameter("cacheControl", "private, max-age=0, no-cache");
    return defHolder;
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
