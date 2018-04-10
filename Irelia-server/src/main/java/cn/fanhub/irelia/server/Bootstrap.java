/**
 *    Copyright 2018 chengfan(fanhub.cn)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.fanhub.irelia.server;

import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 *
 * @author chengfan
 * @version $Id: Bootstrap.java, v 0.1 2018年04月07日 下午4:05 chengfan Exp $
 */
@Slf4j
public class Bootstrap {

    @Setter
    private int port;

    public void start() throws InterruptedException {
        log.info("start netty server");
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            io.netty.bootstrap.ServerBootstrap bootstrap = new io.netty.bootstrap.ServerBootstrap();
            bootstrap.group(group)
                    .channel(NioServerSocketChannel.class)
                    .localAddress(new InetSocketAddress(port))
                    .childHandler(new HttpSupportInitializer());
            ChannelFuture future = bootstrap.bind().sync();
            future.channel().closeFuture().sync();

        } catch (InterruptedException e) {
            log.error("start netty server error :", e);
        } finally {
            log.info("shutdown netty server");
            group.shutdownGracefully().sync();
        }
    }
}