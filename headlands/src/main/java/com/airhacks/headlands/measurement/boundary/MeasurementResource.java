package com.airhacks.headlands.measurement.boundary;

import com.airhacks.headlands.measurement.entity.Measurement;
import com.airhacks.headlands.util.entity.PaginatedListWrapper;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * @author Riccardo Merolla
 *         Created on 31/01/15.
 */
@Path("measurement")
@Stateless
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class MeasurementResource {

    @Inject
    Logger tracer;

    @Inject
    MeasurementService service;

    @GET
    public PaginatedListWrapper<Measurement> findAll(@DefaultValue("1")
                                               @QueryParam("page")
                                               Integer page,
                                               @DefaultValue("10")
                                               @QueryParam("pageSize")
                                               Integer pageSize,
                                               @DefaultValue("id")
                                               @QueryParam("sortFields")
                                               String sortFields,
                                               @DefaultValue("asc")
                                               @QueryParam("sortDirections")
                                               String sortDirections,
                                               @QueryParam("query")
                                               String query) {
        PaginatedListWrapper<Measurement> paginatedListWrapper = new PaginatedListWrapper<>();
        paginatedListWrapper.setCurrentPage(page);
        paginatedListWrapper.setPageSize(pageSize);
        paginatedListWrapper.setSortFields(sortFields);
        paginatedListWrapper.setSortDirections(sortDirections);
        Map<String, Object> params = new HashMap<>();
        params.put("query", query);
        paginatedListWrapper.setParams(params);
        return find(paginatedListWrapper);
    }

    private PaginatedListWrapper<Measurement> find(PaginatedListWrapper<Measurement> wrapper) {
        wrapper.setTotalResults((int) service.count());
        int start = (wrapper.getCurrentPage() - 1) * wrapper.getPageSize();
        service.find(wrapper, start,
                wrapper.getPageSize(),
                wrapper.getSortFields(),
                wrapper.getSortDirections(),
                (String) wrapper.getParams().get("query"));
        return wrapper;
    }

    @GET
    @Path("{id}")
    public Measurement find(@PathParam("id") String id) {
        return service.find(id);
    }

    @POST
    public Measurement save(Measurement measurement) {
        if (null == measurement.getId()) {
            return service.save(measurement);
        }
        return service.update(measurement);
    }

    @PUT
    @Path("{id}")
    public Measurement update(Measurement measurement) {
        return service.update(measurement);
    }

    @DELETE
    @Path("{id}")
    public void remove(@PathParam("id") String id) {
        service.delete(id);
    }

}
