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

import io.hanbings.cynops.security.ShaUtils;
import io.hanbings.moemark.Server;
import io.hanbings.moemark.service.*;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.buffer.impl.BufferImpl;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerFileUpload;
import io.vertx.core.impl.VertxThread;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

public class RouteController {
    private final ThreadService threadService;
    private final VertxService vertxService;
    private final TencentCosService cosService;
    private final ConfigService configService;
    // 过滤文件后缀名正则
    static final Pattern pattern = Pattern.compile(".*(.png|jpg|jpeg|webp)$");

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
                LoggerService.info("[POST] host " + event.request().host() +
                        " request " + event.request().uri());
                // 判断参数
                if ((event.request().params().get("text") == null)) {
                    LoggerService.info("host " + event.request().host() +
                            " error param error.");
                    event.response().end(MessageService.buildMessage("412", "参数缺失 ∑ (´△｀)？！"));
                    return;
                }
                // 限制单文件上传
                if (event.fileUploads().size() > 1) {
                    LoggerService.info("host " + event.request().host() +
                            " error too much file upload.");
                    event.response()
                            .end(MessageService
                                    .buildMessage("412",
                                            "上传得太多啦~ ( º﹃º )"));
                    return;
                }
                // 小于单个文件肯定不行 和大于单文件分开写是为了以后扩展多图上传时候好理解
                if (event.fileUploads().size() < 1) {
                    LoggerService.info("host " + event.request().host() +
                            " error no such file upload.");
                    event.response()
                            .end(MessageService.buildMessage("501",
                                    "好像没有收到文件欸 「(°ヘ°)"));
                    return;
                }
                // 处理文件 判断大小
                Set<FileUpload> files = event.fileUploads();
                for (FileUpload file : files) {
                    if (file.size() > 33554432) {
                        LoggerService.info("host " + event.request().host() +
                                " error image too big.");
                        event.response()
                                .end(MessageService
                                        .buildMessage("413",
                                                "上传的图片太大啦~ ( º﹃º )"));
                        return;
                    }
                    // 判断后缀 仅仅是名字 不根据字节判断了 以后看情况加
                    if (file.fileName().lastIndexOf(".") == -1
                            || !(pattern.matcher(file.fileName().toLowerCase(Locale.ROOT)).matches())) {
                        LoggerService.info("host " + event.request().host() +
                                " error image format error");
                        event.response()
                                .end(MessageService
                                        .buildMessage("412",
                                                "格式错误 (*｀へ´*) "));
                        return;
                    }
                    // 获取文件对象 文件名
                    File result = new File(file.uploadedFileName());
                    String name = file.fileName()
                            .replace(file.fileName().substring(0, file.fileName().lastIndexOf("."))
                                    , ShaUtils.sha256(result));
                    // 上传
                    threadService.execute(new Thread(() -> {
                        cosService.embed(configService.get("bucket"), "picture/" + name, name, event.request().params().get("text"), result);
                        if (result.delete()) {
                            // 产生日志
                            LoggerService.info("[UPLOAD COS] host " + event.request().host() +
                                    " request " + event.request().path() +
                                    " use " + event.request().params() +
                                    " upload " + file.fileName() +
                                    " save " + name);
                        }
                    }));

                    // 请求成功返回
                    event.response()
                            .end(MessageService.buildMessage("200",
                                    "收到啦~ Ｏ(≧▽≦)Ｏ", "file", name));
                }
            }
        });
        // POST /api/v1/extract 提取盲水印
        vertxService.addFileUploadRoute(HttpMethod.POST, "/api/v1/extract", new Handler<RoutingContext>() {
            @Override
            public void handle(RoutingContext event) {
                LoggerService.info("[POST] host " + event.request().host() +
                        " request " + event.request().uri());
                // 限制单文件上传
                if (event.fileUploads().size() > 1) {
                    LoggerService.info("host " + event.request().host() +
                            " error too much file upload.");
                    event.response()
                            .end(MessageService
                                    .buildMessage("412",
                                            "上传得太多啦~ ( º﹃º )"));
                    return;
                }
                // 小于单个文件肯定不行 和大于单文件分开写是为了以后扩展多图上传时候好理解
                if (event.fileUploads().size() < 1) {
                    LoggerService.info("host " + event.request().host() +
                            " error no such file upload.");
                    event.response()
                            .end(MessageService.buildMessage("501",
                                    "好像没有收到文件欸 「(°ヘ°)"));
                    return;
                }
                // 处理文件 判断大小
                Set<FileUpload> files = event.fileUploads();
                for (FileUpload file : files) {
                    if (file.size() > 33554432) {
                        LoggerService.info("host " + event.request().host() +
                                " error image too big.");
                        event.response()
                                .end(MessageService
                                        .buildMessage("413",
                                                "上传的图片太大啦~ ( º﹃º )"));
                        return;
                    }
                    // 判断后缀 仅仅是名字 不根据字节判断了 以后看情况加
                    if (file.fileName().lastIndexOf(".") == -1
                            || !(pattern.matcher(file.fileName().toLowerCase(Locale.ROOT)).matches())) {
                        LoggerService.info("host " + event.request().host() +
                                " error image format error");
                        event.response()
                                .end(MessageService
                                        .buildMessage("412",
                                                "格式错误 (*｀へ´*) "));
                        return;
                    }
                    // 获取文件对象 文件名
                    File result = new File(file.uploadedFileName());
                    String name = file.fileName()
                            .replace(file.fileName().substring(0, file.fileName().lastIndexOf("."))
                                    , ShaUtils.sha256(result));
                    // 上传
                    threadService.execute(new Thread(() -> {
                        cosService.extract(configService.get("bucket"), "watermark/" + name, name, result);
                        if (result.delete()) {
                            // 产生日志
                            LoggerService.info("[UPLOAD COS] host " + event.request().host() +
                                    " request " + event.request().path() +
                                    " use " + event.request().params() +
                                    " upload " + file.fileName() +
                                    " save " + name);
                        }
                    }));

                    // 请求成功返回
                    event.response()
                            .end(MessageService.buildMessage("200",
                                    "收到啦~ Ｏ(≧▽≦)Ｏ", "file", name));
                }
            }
        });
        // GET /api/v1/picture 获取添加水印后的图片
        vertxService.addSimpleRoute(HttpMethod.GET, "/api/v1/picture", new Handler<RoutingContext>() {
            @Override
            public void handle(RoutingContext event) {
                if ((event.request().params().get("file") == null)) {
                    LoggerService.info("host " + event.request().host() +
                            " error param error.");
                    event.response().end(MessageService.buildMessage("412", "参数缺失 ∑ (´△｀)？！"));
                    return;
                }
                // 下载前需要请求一次是否存在
                if (cosService.nonexistent(configService.get("bucket"),
                        "picture/" + event.request().params().get("file"))) {
                    LoggerService.info("host " + event.request().host() +
                            " error resource not found.");
                    event.response().end(MessageService.buildMessage("410", "没有找到欸 ∑ (´△｀)？！"));
                    return;
                }
                // 需要先从腾讯云下载
                threadService.execute(new Thread(() -> {
                    cosService.download(configService.get("bucket"),
                            "picture/" + event.request().params().get("file"),
                            "./picture/" + event.request().params().get("file"));
                    event.response().sendFile("./picture/" + event.request().params().get("file"));
                    if (new File("./picture/" + event.request().params().get("file")).delete()) {
                        LoggerService.info("host " + event.request().host() +
                                " get file " + "./picture/" + event.request().params().get("file"));
                    }
                }));
            }
        });
        // GET /api/v1/watermark 获取水印
        vertxService.addSimpleRoute(HttpMethod.GET, "/api/v1/watermark", new Handler<RoutingContext>() {
            @Override
            public void handle(RoutingContext event) {
                if ((event.request().params().get("file") == null)) {
                    LoggerService.info("host " + event.request().host() +
                            " error param error.");
                    event.response().end(MessageService.buildMessage("412", "参数缺失 ∑ (´△｀)？！"));
                    return;
                }
                // 下载前需要请求一次是否存在
                if (cosService.nonexistent(configService.get("bucket"),
                        "watermark/" + event.request().params().get("file"))) {
                    LoggerService.info("host " + event.request().host() +
                            " error resource not found.");
                    event.response().end(MessageService.buildMessage("410", "没有找到欸 ∑ (´△｀)？！"));
                    return;
                }
                // 需要先从腾讯云下载
                threadService.execute(new Thread() {
                    @Override
                    public void run() {
                        cosService.download(configService.get("bucket"),
                                "watermark/" + event.request().params().get("file"),
                                "./watermark/" + event.request().params().get("file"));
                        event.response().sendFile("./watermark/" + event.request().params().get("file"));
                        if (new File("./watermark/" + event.request().params().get("file")).delete()) {
                            LoggerService.info("host " + event.request().host() +
                                    " get file " + "./watermark/" + event.request().params().get("file"));
                        }
                    }
                });
            }
        });
    }
}
