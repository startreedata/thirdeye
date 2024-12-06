/*
 * Copyright 2024 StarTree Inc
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

import static ai.startree.thirdeye.spi.util.SpiUtils.optional;

import io.sentry.Breadcrumb;
import io.sentry.Sentry;
import io.sentry.SentryLevel;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.ext.Provider;
import org.glassfish.jersey.message.internal.OutboundJaxrsResponse;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.monitoring.ApplicationEvent;
import org.glassfish.jersey.server.monitoring.ApplicationEventListener;
import org.glassfish.jersey.server.monitoring.RequestEvent;
import org.glassfish.jersey.server.monitoring.RequestEvent.Type;
import org.glassfish.jersey.server.monitoring.RequestEventListener;

@Provider
public class ExceptionSentryLogger implements ApplicationEventListener,
    RequestEventListener {

  private static void onException(final RequestEvent event) {
    if (event.getException() instanceof NotFoundException) {
      /* Suppress 404 errors */
      return;
    }
    final Breadcrumb breadcrumb = new Breadcrumb();
    breadcrumb.setCategory("request.exception");
    breadcrumb.setLevel(SentryLevel.WARNING);
    breadcrumb.setMessage(
        "A request generated an exception. The exception was caught by dropwizard.");
    breadcrumb.setData("exception_message", event.getException().getMessage());
    if (event.getException() instanceof WebApplicationException webException
        && webException.getResponse() instanceof OutboundJaxrsResponse rep
        && rep.getContext() != null) {
      // warning - this returns a big StatusListApi object - it may exceed the max payload size of 1Mb.
      // It's very unlikely though so ignoring for the moment. Worst case the breadcrumb is dropped by sentry, but the exception will still be collected.
      breadcrumb.setData("exception_entity",
          optional(rep.getContext().getEntity()).orElse("null"));
    }
    if (event.getContainerRequest() != null) {
      final ContainerRequest req = event.getContainerRequest();
      breadcrumb.setData("request_path", req.getPath(true));
      breadcrumb.setData("request_method", req.getMethod());
      breadcrumb.setData("request_payload", req.readEntity(String.class));
    }
    Sentry.addBreadcrumb(breadcrumb);

    Sentry.captureException(event.getException());
  }

  @Override
  public void onEvent(final ApplicationEvent event) {
  }

  @Override
  public RequestEventListener onRequest(final RequestEvent requestEvent) {
    requestEvent.getContainerRequest().bufferEntity();
    return this;
  }

  @Override
  public void onEvent(final RequestEvent event) {
    if (event.getType() == Type.ON_EXCEPTION) {
      onException(event);
    }
  }
}
