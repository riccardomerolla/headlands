package com.airhacks.headlands.maintenance.boundary;

import com.airhacks.headlands.maintenance.entity.Maintenance;
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
@Path("maintenance")
@Stateless
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class MaintenanceResource {

    @Inject
    Logger tracer;

    @Inject
    MaintenanceService service;

    @GET
    public PaginatedListWrapper<Maintenance> findAll(@DefaultValue("1")
                                               @QueryParam("page")
                                               Integer page,
                                               @DefaultValue("10")
                                               @QueryParam("pageSize")
                                               Integer pageSize,
                                               @DefaultValue("uuid")
                                               @QueryParam("sortFields")
                                               String sortFields,
                                               @DefaultValue("asc")
                                               @QueryParam("sortDirections")
                                               String sortDirections,
                                               @QueryParam("query")
                                               String query) {
        PaginatedListWrapper<Maintenance> paginatedListWrapper = new PaginatedListWrapper<>();
        paginatedListWrapper.setCurrentPage(page);
        paginatedListWrapper.setPageSize(pageSize);
        paginatedListWrapper.setSortFields(sortFields);
        paginatedListWrapper.setSortDirections(sortDirections);
        Map<String, Object> params = new HashMap<>();
        params.put("query", query);
        paginatedListWrapper.setParams(params);
        return find(paginatedListWrapper);
    }

    private PaginatedListWrapper<Maintenance> find(PaginatedListWrapper<Maintenance> wrapper) {
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
    @Path("{uuid}")
    public Maintenance find(@PathParam("uuid") String uuid) {
        return service.find(uuid);
    }

    @POST
    public Maintenance save(Maintenance maintenance) {
        if (null == maintenance.getUUID()) {
            return service.save(maintenance);
        }
        return service.update(maintenance);
    }

    @PUT
    @Path("{uuid}")
    public Maintenance update(Maintenance maintenance) {
        return service.update(maintenance);
    }

    @DELETE
    @Path("{uuid}")
    public void remove(@PathParam("uuid") String uuid) {
        service.delete(uuid);
    }

}
