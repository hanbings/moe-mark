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

package io.hanbings.moemark.controller;

import io.hanbings.moemark.Server;
import io.hanbings.moemark.service.*;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.buffer.impl.BufferImpl;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerFileUpload;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;

import java.util.Objects;
import java.util.Set;

public class RouteController {
    private final ThreadService threadService;
    private final VertxService vertxService;
    private final TencentCosService cosService;
    private final ConfigService configService;

    public RouteController(ThreadService threadService,
                           VertxService vertxService,
                           TencentCosService cosService,
                           ConfigService configService) {
        this.threadService = threadService;
        this.vertxService = vertxService;
        this.cosService = cosService;
        this.configService = configService;
    }

    public void loadApi() {
        // GET /* 匹配全目录文件
        vertxService.addSimpleRoute(HttpMethod.GET, "/*", StaticHandler.create("web"));
        // POST /api/v1/embed 添加盲水印
        vertxService.addFileUploadRoute(HttpMethod.POST, "/api/v1/embed", new Handler<RoutingContext>() {
            @Override
            public void handle(RoutingContext event) {
                LoggerService.info("host " + event.request().host() +
                        " request " + event.request().path() +
                        " use " + event.request().params());
                // 限制单文件上传
                if (event.fileUploads().size() > 1) {
                    event.response()
                            .end(MessageService
                                    .buildMessage("413",
                                            "上传得太多啦~ ( º﹃º )"));
                }
                // 小于单个文件肯定不行 和大于单文件分开写是为了以后扩展多图上传时候好理解
                if (event.fileUploads().size() < 1) {
                    event.response()
                            .end(MessageService.buildMessage("501",
                                    "好像没有收到文件欸 「(°ヘ°)"));
                }
                // 请求成功
                event.response()
                        .end(MessageService.buildMessage("200",
                                "收到啦~ Ｏ(≧▽≦)Ｏ", "url", "url"));
            }
        });
        // POST /api/v1/extract 提取盲水印
        vertxService.addFileUploadRoute(HttpMethod.POST, "/api/v1/extract", new Handler<RoutingContext>() {
            @Override
            public void handle(RoutingContext event) {

            }
        });
    }
}
