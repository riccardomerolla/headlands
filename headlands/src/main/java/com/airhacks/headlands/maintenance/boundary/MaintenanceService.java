package com.airhacks.headlands.maintenance.boundary;

import com.airhacks.headlands.maintenance.entity.Maintenance;
import com.airhacks.headlands.util.entity.PaginatedListWrapper;
import org.elasticsearch.action.count.CountResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortOrder;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;

/**
 * @author Riccardo Merolla
 *         Created on 31/01/15.
 */
@Stateless
public class MaintenanceService {

    @Inject
    Logger tracer;

    @Inject
    Client client;

    public long count() {
        CountResponse response = client.prepareCount("prototype")
                .setQuery(termQuery("_type", "maintenance"))
                .execute()
                .actionGet();
        return response.getCount();
    }

    public Maintenance find(String code) {
        Maintenance maintenance = null;
        GetResponse response = client.prepareGet("prototype", "maintenance", code)
                .execute()
                .actionGet();
        if (response.isExists() && !response.isSourceEmpty()) {
            tracer.info("Response: " + response.getSourceAsString());
            Map<String, Object> sourceAsMap = response.getSourceAsMap();
            maintenance = buildMaintenance(response.getId(), sourceAsMap);
        }
        return maintenance;
    }

    private Maintenance buildMaintenance(String id, Map<String, Object> sourceAsMap) {
        Maintenance maintenance= new Maintenance();
        maintenance.setUUID(String.valueOf(sourceAsMap.get("uuid")));
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        try {
            maintenance.setDate(simpleDateFormat.parse((String) sourceAsMap.get("date")));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        maintenance.setAsset(String.valueOf(sourceAsMap.get("asset")));
        maintenance.setEventType(String.valueOf(sourceAsMap.get("eventType")));
        maintenance.setDescription(String.valueOf(sourceAsMap.get("description")));

        return maintenance;
    }

    public List<Maintenance> findAll() {
        List<Maintenance> list = new ArrayList<>();
        SearchResponse response = client.prepareSearch("prototype")
                    .setTypes("measurement").setFrom(1).execute().actionGet();
        tracer.info("Response: " + response);
        for(SearchHit searchHit : response.getHits().getHits()) {
            final Maintenance maintenance = buildMaintenance(searchHit.getId(), searchHit.sourceAsMap());
            list.add(maintenance);
        }

        return list;
    }

    public List<Maintenance> find(PaginatedListWrapper<Maintenance> wrapper, int start, int pageSize, String sortField, String sortDirection, String query) {
        List<Maintenance> list = new ArrayList<>();
        SearchRequestBuilder searchRequestBuilder = client.prepareSearch("prototype")
                .setTypes("maintenance")
                .setFrom(start).setSize(pageSize)
                .addSort(sortField, SortOrder.valueOf(sortDirection.toUpperCase()));
        if (null != query && !("".equalsIgnoreCase(query))) {
            searchRequestBuilder.setSearchType(SearchType.QUERY_THEN_FETCH)
                    .setQuery(QueryBuilders.simpleQueryString(query));
        }
        SearchResponse response = searchRequestBuilder
                .execute().actionGet();
        client.admin().indices().prepareRefresh("prototype").execute().actionGet();
        tracer.info("Response: " + response);
        for(SearchHit searchHit : response.getHits().getHits()) {
            final Maintenance maintenance = buildMaintenance(searchHit.getId(), searchHit.sourceAsMap());
            list.add(maintenance);
        }

        if (null != wrapper) {
            wrapper.setTotalResults((int) response.getHits().totalHits());
            wrapper.setList(list);
        }
        return list;
    }

    public Maintenance save(Maintenance maintenance) {
        try {
            UUID uuid = UUID.randomUUID();
            IndexResponse response = client.prepareIndex("prototype", "maintenance", uuid.toString())
                    .setSource(jsonBuilder()
                                    .startObject()
                                    .field("uuid", uuid.toString())
                                    .field("date", maintenance.getDate())
                                    .field("asset", maintenance.getAsset())
                                    .field("eventType", maintenance.getEventType())
                                    .field("description", maintenance.getDescription())
                                    .endObject()
                    )
                    .execute()
                    .actionGet();
            client.admin().indices().prepareRefresh("prototype").execute().actionGet();
            return maintenance;
        } catch (IOException e) {
            tracer.severe("Error");
            return null;
        }
    }

    public Maintenance update(Maintenance maintenance) {
        try {
            UpdateRequest updateRequest = new UpdateRequest();
            updateRequest.index("prototype");
            updateRequest.type("maintenance");
            updateRequest.id(maintenance.getUUID());
            updateRequest.doc(jsonBuilder()
                    .field("uuid", maintenance.getUUID())
                    .field("date", maintenance.getDate())
                    .field("asset", maintenance.getAsset())
                    .field("eventType", maintenance.getEventType())
                    .field("description", maintenance.getDescription())
                    .endObject());
            client.update(updateRequest).get();
            return maintenance;
        } catch (Exception e) {
            tracer.severe("Error");
            return null;
        }
    }


    public void delete(String id) {
        DeleteResponse response = client.prepareDelete("prototype", "maintenance", id)
                .execute()
                .actionGet();
        client.admin().indices().prepareRefresh("prototype").execute().actionGet();
    }
}
