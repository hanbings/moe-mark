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

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicSessionCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.region.Region;

public class TencentCosService implements Runnable{
    COSClient client;
    COSCredentials credentials;
    Region region;
    ClientConfig config;

    private TencentCosService() {}

    public TencentCosService(String appId, String secretId, String secretKey, String region) {
        this.credentials = new BasicSessionCredentials(appId, secretId, secretKey);
        this.region = new Region(region);
        this.config = new ClientConfig(this.region);
    }

    @Override
    public void run() {
        this.client = new COSClient(this.credentials, this.config);
    }

    @Override
    public void stop() {
        this.client.shutdown();
    }
}
