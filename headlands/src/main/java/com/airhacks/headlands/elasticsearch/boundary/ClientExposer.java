package com.airhacks.headlands.elasticsearch.boundary;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

import javax.ejb.Singleton;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

/**
 * @author Riccardo Merolla
 *         Created on 31/01/15.
 */
@Singleton
public class ClientExposer {

    Client client;

    @Produces
    public Client expose(InjectionPoint ip) {
        if(null == client) {
            client = new TransportClient()
                    .addTransportAddress(new InetSocketTransportAddress("localhost", 9300))
                    .addTransportAddress(new InetSocketTransportAddress("localhost", 49154));
        }
        return client;
    }

    public void closeClient() {
        client.close();
        client = null;
    }
}
