/**
 * Copyright 2016 Telefonica Investigación y Desarrollo, S.A.U
 *
 * This file is part of fiware-cygnus (FI-WARE project).
 *
 * fiware-cygnus is free software: you can redistribute it and/or modify it under the terms of the GNU Affero
 * General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * fiware-cygnus is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with fiware-cygnus. If not, see
 * http://www.gnu.org/licenses/.
 *
 * For those usages not covered by the GNU Affero General Public License please contact with iot_support at tid dot es
 */
package com.telefonica.iot.cygnus.backends.cartodb;

import com.telefonica.iot.cygnus.backends.http.HttpBackend;
import com.telefonica.iot.cygnus.backends.http.JsonResponse;
import com.telefonica.iot.cygnus.errors.CygnusPersistenceError;
import java.net.URLEncoder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 *
 * @author frb
 */
public class CartoDBBackendImpl extends HttpBackend implements CartoDBBackend {
    
    private final String apiKey;
    private static final String BASE_URL = "/api/v2/sql?q=";
    
    /**
     * Constructor.
     * @param host
     * @param port
     * @param ssl
     * @param apiKey
     */
    public CartoDBBackendImpl(String host, String port, boolean ssl, String apiKey) {
        super(new String[]{host}, port, ssl, false, null, null, null, null);
        this.apiKey = apiKey;
    } // CartoDBBackendImpl
    
    @Override
    public boolean isEmpty(String schema, String tableName) throws Exception {
        String query = "SELECT COUNT(*) FROM " + schema + "." + tableName;
        String encodedQuery = URLEncoder.encode(query, "UTF-8");
        String relativeURL = BASE_URL + encodedQuery + "&api_key=" + apiKey;
        JsonResponse response = doRequest("GET", relativeURL, true, null, null);

        // check the status
        if (response.getStatusCode() != 200) {
            throw new CygnusPersistenceError("The query '" + query + "' could not be executed. CartoDB response: "
                    + response.getStatusCode() + " " + response.getReasonPhrase());
        } // if
        
        JSONArray rows = (JSONArray) response.getJsonObject().get("rows");
        JSONObject countRow = (JSONObject) rows.get(0);
        Long count = (Long) countRow.get("count");
        return (count == 0);
    } // isEmpty

    @Override
    public void createTable(String schema, String tableName, String fields) throws Exception {
        // Create the table
        String query = "CREATE TABLE " + schema + "." + tableName + " " + fields;
        String encodedQuery = URLEncoder.encode(query, "UTF-8");
        String relativeURL = BASE_URL + encodedQuery + "&api_key=" + apiKey;
        JsonResponse response = doRequest("GET", relativeURL, true, null, null);

        if (response.getStatusCode() != 200) {
            throw new CygnusPersistenceError("The query '" + query + "' could not be executed. CartoDB response: "
                    + response.getStatusCode() + " " + response.getReasonPhrase());
        } // if
        
        // CartoDBfy the table
        query = "SELECT cdb_cartodbfytable('" + schema + "', '" + tableName + "')";
        encodedQuery = URLEncoder.encode(query, "UTF-8");
        relativeURL = BASE_URL + encodedQuery + "&api_key=" + apiKey;
        response = doRequest("GET", relativeURL, true, null, null);

        if (response.getStatusCode() != 200) {
            throw new CygnusPersistenceError("The query '" + query + "' could not be executed. CartoDB response: "
                    + response.getStatusCode() + " " + response.getReasonPhrase());
        } // if
    } // createTable
    
    @Override
    public void insert(String schema, String tableName, String withs, String fields, String rows) throws Exception {
        String query = withs + "INSERT INTO " + schema + "." + tableName + " " + fields + " VALUES " + rows;
        String encodedQuery = URLEncoder.encode(query, "UTF-8");
        String relativeURL = BASE_URL + encodedQuery + "&api_key=" + apiKey;
        JsonResponse response = doRequest("GET", relativeURL, true, null, null);

        // check the status
        if (response.getStatusCode() != 200) {
            throw new CygnusPersistenceError("The query '" + query + "' could not be executed. CartoDB response: "
                    + response.getStatusCode() + " " + response.getReasonPhrase());
        } // if
    } // insert
    
} // CartoDBBackendImpl
