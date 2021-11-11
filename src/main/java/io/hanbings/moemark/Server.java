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

package io.hanbings.moemark;

import io.hanbings.moemark.controller.ServerController;
import io.hanbings.moemark.service.LoggerService;

public class Server {
    public static void main(String[] args) {
        LoggerService.info("""
                   
                   _____                   _____                __             \s
                  /     \\   ____   ____   /     \\ _____ _______|  | __       \s
                 /  \\ /  \\ /  _ \\_/ __ \\ /  \\ /  \\\\__  \\\\_  __ \\  |/ /
                /    Y    (  <_> )  ___//    Y    \\/ __ \\|  | \\/    <       \s
                \\____|__  /\\____/ \\___  >____|__  (____  /__|  |__|_ \\     \s
                        \\/            \\/        \\/     \\/           \\/    \s
                         MoeFurry Community MoeMark love VertX.                \s
                            hanbings hanbings@hanbings.io                      \s""");
        ServerController serverController = new ServerController();
        serverController.runServer();
    }
}
