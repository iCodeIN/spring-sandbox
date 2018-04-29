package at.favre.server.springsandbox.web;

import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

@Component
@Path("/v1/test")
public interface HelloWorldEndpoint {
    @GET
    @Path("/saymy")
    String test(@NotNull @QueryParam("name") String name);

    @GET
    @Path("/add")
    String add(@NotNull @QueryParam("name") String name);

    @GET
    @Path("/all")
    String getAll();
}