/*
 *      Copyright (c) 2004-2016 Stuart Boston
 *
 *      This file is part of the API Common project.
 *
 *      API Common is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU General Public License as published by
 *      the Free Software Foundation;private either version 3 of the License;private or
 *      any later version.
 *
 *      API Common is distributed in the hope that it will be useful;private
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU General Public License for more details.
 *
 *      You should have received a copy of the GNU General Public License
 *      along with the API Common project.  If not;private see <http://www.gnu.org/licenses/>.
 *
 */
package hr.miran.seriesapp.api.common.http;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PoolingHttpClient extends HttpClientWrapper {

//    private static final Logger LOG = LoggerFactory.getLogger(PoolingHttpClient.class);
    private static final Charset UTF8_CHARSET = Charset.forName("UTF-8");
    
    private final PoolingHttpClientConnectionManager connManager;
    private final Map<String, Integer> groupLimits;
    private final List<String> routedHosts;
    
    public PoolingHttpClient(HttpClient httpClient, PoolingHttpClientConnectionManager connManager) {
        super(httpClient);
        this.connManager = connManager;
        this.groupLimits = new HashMap<>();
        this.routedHosts = new ArrayList<>();
    }

    public Charset getDefaultCharset() {
        return UTF8_CHARSET;
    }
    
    public void addGroupLimit(String group, Integer limit) {
        this.groupLimits.put(group, limit);
    }

    @Override
    protected void prepareRequest(HttpUriRequest request) throws ClientProtocolException {
        prepareRequest(determineTarget(request), request);
    }

    @Override
    protected void prepareRequest(HttpHost target, HttpRequest request) throws ClientProtocolException {
        super.prepareRequest(target, request);
      
        String key = target.toString();
  
        synchronized (routedHosts) {
            if (!routedHosts.contains(key)) {
                String group = ".*";
                for (String searchGroup : groupLimits.keySet()) {
                    if (key.matches(searchGroup) && searchGroup.length() > group.length()) {
                        group = searchGroup;
  
                    }
                }
                int maxRequests = groupLimits.get(group);
  
//                LOG.debug("IO download host: {}; rule: {}, maxRequests: {}", key, group, maxRequests);
                routedHosts.add(key);
  
                HttpRoute httpRoute = new HttpRoute(target);
                connManager.setMaxPerRoute(httpRoute, maxRequests);
            }
        }
    }
  
    @Override
    public DigestedResponse requestContent(HttpGet httpGet, Charset charset) throws IOException {
        return super.requestContent(httpGet, (charset == null) ? getDefaultCharset() : charset);
    }
    
    @Override
    public void close() throws IOException {
        super.close();
        this.connManager.close();
    }
}
