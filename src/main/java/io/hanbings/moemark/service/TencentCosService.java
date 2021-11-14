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
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.model.GetObjectRequest;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.ciModel.persistence.PicOperations;
import com.qcloud.cos.region.Region;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class TencentCosService implements Runnable {
    COSClient client;
    COSCredentials credentials;
    Region region;
    ClientConfig config;

    public TencentCosService(String secretId, String secretKey, String region) {
        this.credentials = new BasicCOSCredentials(secretId, secretKey);
        this.region = new Region(region);
        this.config = new ClientConfig(this.region);
    }

    public void embed(String bucket, String path, String name, String text, File file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucket, path, file);
        PicOperations picOperations = new PicOperations();
        picOperations.setIsPicInfo(0);
        List<PicOperations.Rule> ruleList = new LinkedList<>();
        PicOperations.Rule rule = new PicOperations.Rule();
        rule.setBucket(bucket);
        rule.setFileId(name);
        rule.setRule("watermark/3/type/3/text/" + text);
        ruleList.add(rule);
        picOperations.setRules(ruleList);
        putObjectRequest.setPicOperations(picOperations);
        try {
            client.putObject(putObjectRequest);
        } catch (CosClientException e) {
            e.printStackTrace();
        }
    }

    public void extract(String bucket, String path, String name, File file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucket, path, file);
        PicOperations picOperations = new PicOperations();
        picOperations.setIsPicInfo(0);
        List<PicOperations.Rule> ruleList = new LinkedList<>();
        PicOperations.Rule rule = new PicOperations.Rule();
        rule.setBucket(bucket);
        rule.setFileId(name);
        rule.setRule("watermark/4/type/3");
        ruleList.add(rule);
        picOperations.setRules(ruleList);
        putObjectRequest.setPicOperations(picOperations);
        try {
            client.putObject(putObjectRequest);
        } catch (CosClientException e) {
            e.printStackTrace();
        }
    }

    public void download(String bucket, String path, String save) {
        File file = new File(save);
        GetObjectRequest getObjectRequest = new GetObjectRequest(bucket, path);
        client.getObject(getObjectRequest, file);
    }

    public boolean nonexistent(String bucket, String path) {
        return (!client.doesBucketExist(bucket)) && (client.getObjectMetadata(bucket, path) == null);
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
