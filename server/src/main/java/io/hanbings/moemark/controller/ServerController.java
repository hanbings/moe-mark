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

import io.hanbings.moemark.service.ConfigService;
import io.hanbings.moemark.service.LoggerService;
import io.hanbings.moemark.service.ThreadService;
import io.hanbings.moemark.service.VertxService;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class ServerController {
    private ThreadService threadService;
    private VertxService vertxService;

    public void runServer() {
        // 读取配置文件
        LoggerService.info("Config Loading...");
        ConfigService configService = new ConfigService("server.properties");

        // 创建线程池
        LoggerService.info("Thread Pool Loading...");
        this.threadService = new ThreadService(3, 30,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>());
        LoggerService.info("Thread Pool Running...");

        // 创建 vertx 服务
        LoggerService.info("VertX Loading...");
        this.vertxService = new VertxService(Integer.parseInt(configService.get("port")));
        threadService.execute(new Thread(vertxService::runServer));
        LoggerService.info("VertX Running...");
    }

    public void stopServer() {
        LoggerService.info("Server stopping...");
        threadService.shutdown();
        vertxService.stopServer();
        LoggerService.info("Bye~ have a good day~");
    }
}
