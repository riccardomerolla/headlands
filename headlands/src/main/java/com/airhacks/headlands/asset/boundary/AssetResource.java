package com.airhacks.headlands.asset.boundary;

import com.airhacks.headlands.asset.entity.Asset;
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
@Path("asset")
@Stateless
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AssetResource {

    @Inject
    Logger tracer;

    @Inject
    AssetService service;

    @GET
    public PaginatedListWrapper<Asset> findAll(@DefaultValue("1")
                                               @QueryParam("page")
                                               Integer page,
                                               @DefaultValue("5")
                                               @QueryParam("pageSize")
                                               Integer pageSize,
                                               @DefaultValue("code")
                                               @QueryParam("sortFields")
                                               String sortFields,
                                               @DefaultValue("asc")
                                               @QueryParam("sortDirections")
                                               String sortDirections,
                                               @QueryParam("query")
                                               String query) {
        PaginatedListWrapper<Asset> paginatedListWrapper = new PaginatedListWrapper<>();
        paginatedListWrapper.setCurrentPage(page);
        paginatedListWrapper.setPageSize(pageSize);
        paginatedListWrapper.setSortFields(sortFields);
        paginatedListWrapper.setSortDirections(sortDirections);
        Map<String, Object> params = new HashMap<>();
        params.put("query", query);
        paginatedListWrapper.setParams(params);
        return find(paginatedListWrapper);
    }

    private PaginatedListWrapper<Asset> find(PaginatedListWrapper<Asset> wrapper) {
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
    public Asset find(@PathParam("id") String id) {
        return service.find(id);
    }

    @POST
    public Asset save(Asset asset) {
        if (null == asset.getId()) {
            return service.save(asset);
        }
        return service.update(asset);
    }

    @PUT
    @Path("{id}")
    public Asset update(Asset asset) {
        return service.update(asset);
    }

    @DELETE
    @Path("{id}")
    public void remove(@PathParam("id") String id) {
        service.delete(id);
    }

}
