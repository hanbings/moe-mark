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

import io.hanbings.moemark.Server;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

@SuppressWarnings("unused")
public class ConfigService {
    String path;
    public ConfigService() {}

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public ConfigService(String path) {
        this.path = path;
        File file = new File(path);
        // 判断文件是否存在
        if (file.exists()) {
            LoggerService.info("Config found.");
        } else {
            LoggerService.warn("Config not exists, create it ...");
            try {
                file.createNewFile();
                OutputStream outputStream = new FileOutputStream(file);
                outputStream.write(Objects.requireNonNull(Server.class
                                .getClassLoader()
                                .getResourceAsStream("server.properties"))
                        .readAllBytes());
                outputStream.flush();
                outputStream.close();
                LoggerService.error("Created config. The Server will stop. use server.properties config it.");
                // 此时应该先退出应用以填写配置文件
                System.exit(0);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //根据Key读取Value
    public String get(String key) {
        Properties properties = new Properties();
        try {
            InputStream inputStream = new BufferedInputStream(new FileInputStream(path));
            properties.load(inputStream);
            String value = properties.getProperty(key) + "";
            if (("null").equals(value)) {
                LoggerService.warn("Config " + key + "read failed.");
                return null;
            }
            inputStream.close();
            return value;
        } catch (IOException e) {
            LoggerService.warn("Config " + key + "read failed.");
            return null;
        }
    }

    // 读取Properties的全部信息
    public Map<String, String> get() {
        Map<String, String> map = new HashMap<>();
        try {
            Properties properties = new Properties();
            InputStream inputStream = new BufferedInputStream(new FileInputStream(path));
            properties.load(inputStream);
            Enumeration<?> enumeration = properties.propertyNames(); //得到配置文件的名字
            while (enumeration.hasMoreElements()) {
                String strKey = (String) enumeration.nextElement();
                String strValue = properties.getProperty(strKey);
                map.put(strKey, strValue);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
    }

    // 写入Properties信息
    public void write(String pKey, String pValue) throws IOException {
        Properties properties = new Properties();
        InputStream inputStream = new FileInputStream(path);
        // 从输入流中读取属性列表（键和元素对）
        properties.load(inputStream);
        // 调用 Hashtable 的方法 put。使用 getProperty 方法提供并行性。
        // 强制要求为属性的键和值使用字符串。返回值是 Hashtable 调用 put 的结果。
        OutputStream outputStream = new FileOutputStream(path);
        properties.setProperty(pKey, pValue);
        // 以适合使用 load 方法加载到 Properties 表中的格式，
        // 将此 Properties 表中的属性列表（键和元素对）写入输出流
        properties.store(outputStream, "Update " + pKey + " name");
        outputStream.flush();
        inputStream.close();
        outputStream.close();
    }

    // 批量写入Properties信息
    public void write(List<String> list, Map<String, String> map) throws IOException {
        Properties properties = new Properties();
        InputStream inputStream = new FileInputStream(path);
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        // 从输入流中读取属性列表（键和元素对）
        properties.load(inputStreamReader);
        OutputStream outputStream = new FileOutputStream(path);
        for (String strings : list) {
            properties.setProperty(strings, map.get(strings));
        }
        properties.store(outputStream, "Update");
        outputStream.flush();
        inputStream.close();
        outputStream.close();
    }
}

