package ai.startree.thirdeye.resources;

import ai.startree.thirdeye.config.UiConfiguration;
import ai.startree.thirdeye.spi.api.UIConfigurationApi;
import com.codahale.metrics.annotation.Timed;
import io.swagger.annotations.Api;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static ai.startree.thirdeye.util.ResourceUtils.respondOk;

@Api(tags = "UI Configuration")
@Singleton
@Produces(MediaType.APPLICATION_JSON)
public class UIResource {

    private UiConfiguration configuration;

    @Inject
    public UIResource(final UiConfiguration uiConfiguration) {
        this.configuration = uiConfiguration;
    }

    @GET
    @Path("config")
    @Timed
    @Produces(MediaType.APPLICATION_JSON)
    public Response get() {
        return respondOk(new UIConfigurationApi().setClientId(configuration.getClientId()));
    }
}
