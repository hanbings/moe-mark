/*
 * Copyright (c) 2021 Hanbings / hanbings MoeFurry MoeMark.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.hanbings.moemark.service;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class MessageService {
    static Gson gson = new Gson();
    private MessageService() {}

    public static String buildMessage(String code, String message) {
        Map<String, String> map = new HashMap<>();
        map.put("code", code);
        map.put("message", message);
        return gson.toJson(map);
    }

    public static String buildMessage(String code, String message, Map<String, String> map) {
        map.put("code", code);
        map.put("message", message);
        return gson.toJson(map);
    }

    public static String buildMessage(String code, String message, String... more) {
        Map<String, String> map = new HashMap<>();
        map.put("code", code);
        map.put("message", message);
        for (int count = 0; count < more.length; count = count + 2) {
            map.put(more[count], more[count + 1]);
        }
        return gson.toJson(map);
    }
}
