/*
 * Copyright, 2013, SALESFORCE.com 
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.jersey;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.junit.Before;
import org.junit.Test;

import com.force.spa.AuthorizationConnector;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.ClientFilter;
import com.sun.jersey.client.apache4.ApacheHttpClient4;
import com.sun.jersey.client.apache4.config.ApacheHttpClient4Config;
import com.sun.jersey.client.apache4.config.DefaultApacheHttpClient4Config;

public class ClientFactoryTest {

    private static final String AUTHORIZATION = "3MVG9lKcPoNINVBLYaNDxgCfI4tzPYNyBMLcvL";
    private static final URI INSTANCE_URL = URI.create("https://instance.com");

    private AuthorizationConnector mockConnector;

    @Before
    public void setUp() {
        mockConnector = mock(AuthorizationConnector.class);
        when(mockConnector.getAuthorization()).thenReturn(AUTHORIZATION);
        when(mockConnector.getInstanceUrl()).thenReturn(INSTANCE_URL);
    }

    @Test
    public void testNewFactoryWithDefaultConfig() {

        ClientFactory factory = new ClientFactory();
        Client client = factory.newInstance(mockConnector);

        assertThat(client, instanceOf(ApacheHttpClient4.class));
        assertThat(getConnectionManager(client), instanceOf(PoolingClientConnectionManager.class));
        assertThat(getPoolingConnectionManager(client).getDefaultMaxPerRoute(), is(equalTo(ClientFactory.DEFAULT_MAX_CONNECTIONS_PER_ROUTE)));
        assertThat(getPoolingConnectionManager(client).getMaxTotal(), is(equalTo(ClientFactory.DEFAULT_MAX_CONNECTIONS_TOTAL)));
    }

    @Test
    public void testNewFactoryWithSpecifiedPoolLimits() {

        ApacheHttpClient4Config clientConfig = new DefaultApacheHttpClient4Config();
        clientConfig.getProperties().put(ClientFactory.PROPERTY_MAX_CONNECTIONS_PER_ROUTE, 101);
        clientConfig.getProperties().put(ClientFactory.PROPERTY_MAX_CONNECTIONS_TOTAL, 1001);

        ClientFactory factory = new ClientFactory(clientConfig);
        Client client = factory.newInstance(mockConnector);

        assertThat(client, instanceOf(ApacheHttpClient4.class));
        assertThat(getConnectionManager(client), instanceOf(PoolingClientConnectionManager.class));
        assertThat(getPoolingConnectionManager(client).getDefaultMaxPerRoute(), is(equalTo(101)));
        assertThat(getPoolingConnectionManager(client).getMaxTotal(), is(equalTo(1001)));
    }

    @Test
    public void testNewFactoryWithSpecifiedConnectionManager() {

        PoolingClientConnectionManager myConnectionManager = new PoolingClientConnectionManager();
        myConnectionManager.setDefaultMaxPerRoute(102);
        myConnectionManager.setMaxTotal(1002);

        ApacheHttpClient4Config clientConfig = new DefaultApacheHttpClient4Config();
        clientConfig.getProperties().put(ApacheHttpClient4Config.PROPERTY_CONNECTION_MANAGER, myConnectionManager);

        ClientFactory factory = new ClientFactory(clientConfig);
        Client client = factory.newInstance(mockConnector);

        assertThat(client, instanceOf(ApacheHttpClient4.class));
        assertThat(getConnectionManager(client), instanceOf(PoolingClientConnectionManager.class));
        assertThat(getPoolingConnectionManager(client).getDefaultMaxPerRoute(), is(equalTo(102)));
        assertThat(getPoolingConnectionManager(client).getMaxTotal(), is(equalTo(1002)));
    }

    @Test
    public void testThatAuthorizationFilterIsEstablished() {

        ClientFactory factory = new ClientFactory();
        Client client = factory.newInstance(mockConnector);

        issueDummyPutRequest(client, new ClientRequestChecker() {
            @Override
            public void check(ClientRequest request) {
                String authorization = (String) request.getHeaders().get("AUTHORIZATION").get(0);
                assertThat(authorization, is(equalTo(AUTHORIZATION)));
            }
        });
    }

    private static HttpClient getHttpClient(Client client) {
        return ((ApacheHttpClient4) client).getClientHandler().getHttpClient();
    }

    private static ClientConnectionManager getConnectionManager(Client client) {
        return getHttpClient(client).getConnectionManager();
    }

    private static PoolingClientConnectionManager getPoolingConnectionManager(Client client) {
        return (PoolingClientConnectionManager) getConnectionManager(client);
    }

    private void issueDummyPutRequest(Client client, ClientRequestChecker checker) {
        WebResource resource = client.resource("null://null");
        resource.addFilter(checker);
        try {
            resource.put("Hello");
        } catch (ExpectedDummyRequestException e) {
            // Expected
        }
    }

    private static abstract class ClientRequestChecker extends ClientFilter {
        public abstract void check(ClientRequest request);

        @Override
        public ClientResponse handle(ClientRequest request) throws ClientHandlerException {

            delegateToNextAndIgnoreExpectedException(request);
            check(request);
            throw new ExpectedDummyRequestException();
        }

        private void delegateToNextAndIgnoreExpectedException(ClientRequest request) {
            try {
                getNext().handle(request);
            } catch (ClientHandlerException e) {
                if (!(e.getCause() instanceof ClientProtocolException)) {
                    throw e;
                }
            }
        }
    }

    private static class ExpectedDummyRequestException extends RuntimeException {
    }
}
