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

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;

import java.net.http.HttpResponse;
import java.util.HashSet;
import java.util.Set;

public class VertxService implements Runnable{
    Vertx vertx;
    HttpServer server;
    Router router;
    int port;
    Set<String> allowedHeaders;
    Set<HttpMethod> allowedMethods;

    private VertxService() {}

    @SuppressWarnings("SpellCheckingInspection")
    public VertxService(int port) {
        // 初始化 VertX
        this.vertx = Vertx.vertx();
        // 创建 VertX 服务器
        this.server = vertx.createHttpServer();
        // 创建路由
        this.router = Router.router(vertx);
        // 设置处理器
        this.server.requestHandler(router);
        // 设置端口
        this.port = port;
        // 初始化跨域配置
        allowedHeaders = new HashSet<>();
        allowedMethods = new HashSet<>();
        allowedHeaders.add("x-requested-with");
        allowedHeaders.add("Access-Control-Allow-Origin");
        allowedHeaders.add("origin");
        allowedHeaders.add("Content-Type");
        allowedHeaders.add("accept");
        allowedHeaders.add("X-PINGARUNER");
        allowedMethods.add(HttpMethod.GET);
        allowedMethods.add(HttpMethod.POST);
        // 设置默认为允许所有域
        router.route().handler(CorsHandler.create("*").allowedHeaders(allowedHeaders).allowedMethods(allowedMethods));
    }

    @Override
    public void run() {
        // 绑定端口
        server.listen(this.port);
    }

    @Override
    public void stop() {
        server.close();
    }

    public void addSimpleRoute(HttpMethod method, String path, Handler<RoutingContext> handler) {
        this.router.route(method, path).handler(handler);
    }

    public void addFileUploadRoute(HttpMethod method, String path, Handler<RoutingContext> handler) {
        this.router.route(method, path).handler(BodyHandler.create().setUploadsDirectory("temp").setDeleteUploadedFilesOnEnd(true)).handler(handler);
    }

    public void delRoute(String path) {
        this.router.delete(path);
    }
}

