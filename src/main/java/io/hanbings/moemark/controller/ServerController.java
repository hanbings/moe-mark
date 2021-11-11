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

import io.hanbings.moemark.service.*;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class ServerController {
    private ThreadService threadService;
    private VertxService vertxService;
    private TencentCosService cosService;

    public void runServer() {
        // 读取配置文件
        LoggerService.info("Config Loading...");
        ConfigService configService = new ConfigService("server.properties");
        LoggerService.info("Config Ready...");

        // 创建线程池
        LoggerService.info("Thread Pool Loading...");
        this.threadService = new ThreadService(3, 30,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>());
        this.threadService.run();
        LoggerService.info("Thread Pool Running...");

        // 创建 vertx 服务
        LoggerService.info("VertX Loading...");
        this.vertxService = new VertxService(Integer.parseInt(configService.get("port")));
        this.threadService.execute(new Thread(this.vertxService::run));
        LoggerService.info("VertX Running...");

        // 获取腾讯云 COS 实例
        LoggerService.info("Tencent COS Client Loading...");
        this.cosService = new TencentCosService(configService.get("app_id"),
                configService.get("secret_id"),
                configService.get("secret_key"),
                configService.get("region"));
        this.threadService.execute(new Thread(this.cosService::run));
        LoggerService.info("Tencent COS Client Running...");

        // 初始化服务完成 转交实例给 Route Controller
        RouteController routeController = new RouteController(threadService, vertxService, cosService, configService);
        routeController.loadApi();
    }

    public void stopServer() {
        LoggerService.info("Server stopping...");
        vertxService.stop();
        cosService.stop();
        threadService.stop();
        LoggerService.info("Bye~ have a good day~");
    }
}
