package com.airhacks.headlands.measurement.boundary;

import com.airhacks.headlands.measurement.entity.Measurement;
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
import java.util.logging.Logger;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;

/**
 * @author Riccardo Merolla
 *         Created on 31/01/15.
 */
@Stateless
public class MeasurementService {

    @Inject
    Logger tracer;

    @Inject
    Client client;

    public long count() {
        CountResponse response = client.prepareCount("prototype")
                .setQuery(termQuery("_type", "measurement"))
                .execute()
                .actionGet();
        return response.getCount();
    }

    public Measurement find(String code) {
        Measurement measurement = null;
        GetResponse response = client.prepareGet("prototype", "measurement", code)
                .execute()
                .actionGet();
        if (response.isExists() && !response.isSourceEmpty()) {
            tracer.info("Response: " + response.getSourceAsString());
            Map<String, Object> sourceAsMap = response.getSourceAsMap();
            measurement = buildMeasurement(response.getId(), sourceAsMap);
        }
        return measurement;
    }

    private Measurement buildMeasurement(String id, Map<String, Object> sourceAsMap) {
        Measurement measurement= new Measurement();
        measurement.setId(Long.valueOf(String.valueOf(sourceAsMap.get("id"))));
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        try {
            measurement.setDate(simpleDateFormat.parse((String) sourceAsMap.get("date")));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        measurement.setAsset(String.valueOf(sourceAsMap.get("asset")));
        measurement.setLatitude(String.valueOf(sourceAsMap.get("latitude")));
        measurement.setLongitude(String.valueOf(sourceAsMap.get("longitude")));
        measurement.setIp_address(String.valueOf(sourceAsMap.get("ip_address")));
        measurement.setTemp(Double.valueOf(String.valueOf(sourceAsMap.get("temp"))));
        measurement.setPressure(Double.valueOf(String.valueOf(sourceAsMap.get("pressure"))));

        return measurement;
    }

    public List<Measurement> findAll() {
        List<Measurement> list = new ArrayList<>();
        SearchResponse response = client.prepareSearch("prototype")
                    .setTypes("measurement").setFrom(1).execute().actionGet();
        tracer.info("Response: " + response);
        for(SearchHit searchHit : response.getHits().getHits()) {
            final Measurement measurement = buildMeasurement(searchHit.getId(), searchHit.sourceAsMap());
            list.add(measurement);
        }

        return list;
    }

    public List<Measurement> find(PaginatedListWrapper<Measurement> wrapper, int start, int pageSize, String sortField, String sortDirection, String query) {
        List<Measurement> list = new ArrayList<>();
        SearchRequestBuilder searchRequestBuilder = client.prepareSearch("prototype")
                .setTypes("measurement")
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
            final Measurement measurement = buildMeasurement(searchHit.getId(), searchHit.sourceAsMap());
            list.add(measurement);
        }

        if (null != wrapper) {
            wrapper.setTotalResults((int) response.getHits().totalHits());
            wrapper.setList(list);
        }
        return list;
    }

    public Measurement save(Measurement measurement) {
        try {
            IndexResponse response = client.prepareIndex("prototype", "measurement", String.valueOf(measurement.getId()))
                    .setSource(jsonBuilder()
                                    .startObject()
                                    .field("id", measurement.getId())
                                    .field("date", measurement.getDate())
                                    .field("asset", measurement.getAsset())
                                    .field("latitude", measurement.getLatitude())
                                    .field("longitude", measurement.getLongitude())
                                    .field("temp", measurement.getTemp())
                                    .field("pressure", measurement.getPressure())
                                    .field("ip_address", measurement.getIp_address())
                                    .endObject()
                    )
                    .execute()
                    .actionGet();
            client.admin().indices().prepareRefresh("prototype").execute().actionGet();
            return measurement;
        } catch (IOException e) {
            tracer.severe("Error");
            return null;
        }
    }

    public Measurement update(Measurement measurement) {
        try {
            UpdateRequest updateRequest = new UpdateRequest();
            updateRequest.index("prototype");
            updateRequest.type("measurement");
            updateRequest.id(String.valueOf(measurement.getId()));
            updateRequest.doc(jsonBuilder()
                    .field("id", measurement.getId())
                    .field("date", measurement.getDate())
                    .field("asset", measurement.getAsset())
                    .field("latitude", measurement.getLatitude())
                    .field("longitude", measurement.getLongitude())
                    .field("temp", measurement.getTemp())
                    .field("pressure", measurement.getPressure())
                    .field("ip_address", measurement.getIp_address())
                    .endObject());
            client.update(updateRequest).get();
            return measurement;
        } catch (Exception e) {
            tracer.severe("Error");
            return null;
        }
    }


    public void delete(String id) {
        DeleteResponse response = client.prepareDelete("prototype", "measurement", id)
                .execute()
                .actionGet();
        client.admin().indices().prepareRefresh("prototype").execute().actionGet();
    }
}
