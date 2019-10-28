package org.osgl.http;

/*-
 * #%L
 * OSGL HTTP
 * %%
 * Copyright (C) 2017 - 2019 OSGL (Open Source General Library)
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.List;
import java.util.Map;

public interface HttpClient {

    // ---- GET methods

    <T> T get(Class<? extends T> returnType, String url, Map<String, Object> queryParameter);
    <T> T get(Class<? extends T> returnType, String url, Map<String, Object> queryParameter, Map<String, String> headers);

    <T> List<T> getList(Class<? extends T> returnElementType, String url, Map<String, Object> queryParameter);
    <T> List<T> getList(Class<? extends T> returnElementType, String url, Map<String, Object> queryParameter, Map<String, String> headers);

    JSONObject get(String url, Map<String, Object> queryParameters);
    JSONObject get(String url, Map<String, Object> queryParameters, Map<String, String> headers);
    
    JSONArray getArray(String url, Map<String, Object> queryParameters);
    JSONArray getArray(String url, Map<String, Object> queryParameters, Map<String, String> headers);

    // ---- POST methods

    <T> T post(Class<? extends T> returnType, String url, Object payload);
    <T> T post(Class<? extends T> returnType, String url, Map<String, Object> queryParameter, Map<String, String> headers);

    JSONObject post(String url, Map<String, Object> queryParameters);
    JSONObject post(String url, Map<String, Object> queryParameters, Map<String, String> headers);

    // ---- PUT methods

    <T> T put(Class<? extends T> returnType, String url, Object payload);
    <T> T put(Class<? extends T> returnType, String url, Map<String, Object> queryParameter, Map<String, String> headers);

    JSONObject put(String url, Map<String, Object> queryParameters);
    JSONObject put(String url, Map<String, Object> queryParameters, Map<String, String> headers);

    // ---- DELETE methods
    void delete(String url, Map<String, Object> queryParameters);
    void delete(String url, Map<String, Object> queryParameters, Map<String, String> headers);

}
