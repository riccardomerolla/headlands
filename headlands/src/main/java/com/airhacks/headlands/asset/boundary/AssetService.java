package com.airhacks.headlands.asset.boundary;

import com.airhacks.headlands.asset.entity.Asset;
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
public class AssetService {

    @Inject
    Logger tracer;

    @Inject
    Client client;

    public long count() {
        CountResponse response = client.prepareCount("prototype")
                .setQuery(termQuery("_type", "asset"))
                .execute()
                .actionGet();
        return response.getCount();
    }

    public Asset find(String code) {
        Asset asset = null;
        GetResponse response = client.prepareGet("prototype", "asset", code)
                .execute()
                .actionGet();
        if (response.isExists() && !response.isSourceEmpty()) {
            tracer.info("Response: " + response.getSourceAsString());
            Map<String, Object> sourceAsMap = response.getSourceAsMap();
            asset = buildAssest(response.getId(), sourceAsMap);
        }
        return asset;
    }

    private Asset buildAssest(String id, Map<String, Object> sourceAsMap) {
        Asset asset= new Asset();
        asset.setId(id);
        asset.setCode(String.valueOf(sourceAsMap.get("code")));
        asset.setDescription(String.valueOf(sourceAsMap.get("description")));
        return asset;
    }

    public List<Asset> findAll() {
        List<Asset> list = new ArrayList<>();
        SearchResponse response = client.prepareSearch("prototype")
                    .setTypes("asset").setFrom(1).execute().actionGet();
        tracer.info("Response: " + response);
        for(SearchHit searchHit : response.getHits().getHits()) {
            final Asset asset = buildAssest(searchHit.getId(), searchHit.sourceAsMap());
            list.add(asset);
        }

        return list;
    }

    public List<Asset> find(PaginatedListWrapper<Asset> wrapper, int start, int pageSize, String sortField, String sortDirection, String query) {
        List<Asset> list = new ArrayList<>();
        client.admin().indices().prepareRefresh("prototype").execute().actionGet();
        SearchRequestBuilder searchRequestBuilder = client.prepareSearch("prototype")
                .setTypes("asset").setFrom(start).setSize(pageSize)
                .addSort(sortField, SortOrder.valueOf(sortDirection.toUpperCase()));
        if (null != query && !("".equalsIgnoreCase(query))) {
            searchRequestBuilder.setSearchType(SearchType.QUERY_THEN_FETCH)
                    .setQuery(QueryBuilders.simpleQueryString(query));
        }
        SearchResponse response = searchRequestBuilder
                .execute().actionGet();
        tracer.info("Response: " + response);
        for(SearchHit searchHit : response.getHits().getHits()) {
            final Asset asset = buildAssest(searchHit.getId(), searchHit.sourceAsMap());
            list.add(asset);
        }

        if (null != wrapper) {
            wrapper.setTotalResults((int) response.getHits().totalHits());
            wrapper.setList(list);
        }
        return list;
    }

    public Asset save(Asset asset) {
        try {
            IndexResponse response = client.prepareIndex("prototype", "asset", asset.getCode())
                    .setSource(jsonBuilder()
                                .startObject()
                                .field("code", asset.getCode())
                                .field("description", asset.getDescription())
                                .endObject()
                    )
                    .execute()
                    .actionGet();
            client.admin().indices().prepareRefresh("prototype").execute().actionGet();
            return asset;
        } catch (IOException e) {
            tracer.severe("Error");
            return null;
        }
    }

    public Asset update(Asset asset) {
        try {
            UpdateRequest updateRequest = new UpdateRequest();
            updateRequest.index("prototype");
            updateRequest.type("asset");
            updateRequest.id(asset.getId());
            updateRequest.doc(jsonBuilder()
                    .startObject()
                    .field("code", asset.getCode())
                    .field("description", asset.getDescription())
                    .endObject());
            client.update(updateRequest).get();
            return asset;
        } catch (Exception e) {
            tracer.severe("Error");
            return null;
        }
    }


    public void delete(String id) {
        DeleteResponse response = client.prepareDelete("prototype", "asset", id)
                .execute()
                .actionGet();
        client.admin().indices().prepareRefresh("prototype").execute().actionGet();
    }
}
