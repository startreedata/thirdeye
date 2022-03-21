package ai.startree.thirdeye.resources;

import static ai.startree.thirdeye.util.ResourceUtils.respondOk;

import ai.startree.thirdeye.config.UiConfiguration;
import ai.startree.thirdeye.spi.api.UiConfigurationApi;
import com.codahale.metrics.annotation.Timed;
import io.swagger.annotations.Api;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Api(tags = "UI Configuration")
@Singleton
@Produces(MediaType.APPLICATION_JSON)
public class UiResource {

    private UiConfiguration configuration;

    @Inject
    public UiResource(final UiConfiguration uiConfiguration) {
        this.configuration = uiConfiguration;
    }

    @GET
    @Path("config")
    @Timed
    @Produces(MediaType.APPLICATION_JSON)
    public Response get() {
        return respondOk(new UiConfigurationApi().setClientId(configuration.getClientId()));
    }
}
