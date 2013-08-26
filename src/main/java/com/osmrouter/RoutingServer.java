package com.osmrouter;

import com.graphhopper.GraphHopper;
import com.graphhopper.util.CmdArgs;
import com.graphhopper.util.Helper;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.logging.LoggingHandler;

/**
 * User: Aleksey.Shulga
 * Date: 25.08.13
 * Time: 11:42
 */
public class RoutingServer {

    private static final int PORT = 8888;
    private Router router;

    public RoutingServer(Router router) {
        this.router = router;
    }

    public static void main(String[] cmdArgs) throws Exception {
        CmdArgs args = CmdArgs.read(cmdArgs);
        if (Helper.isEmpty(args.get("config", ""))) {
            args.put("config","graphhopper.properties");
        }

        String algo = args.get("routing.defaultAlgorithm", "dijkstrabi");
        GraphHopper hopper = new GraphHopper().forServer().init(args).importOrLoad();
        Router router = new Router(hopper, algo);
        new RoutingServer(router).run();

    }

    public void run() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup(1);
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.option(ChannelOption.SO_BACKLOG, 1024);
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline p = ch.pipeline();
                            p.addLast("decoder", new HttpRequestDecoder());
                            p.addLast("encoder", new HttpResponseEncoder());
                            p.addLast("handler", new RouteRequestHandler(router));
                            p.addLast("logging", new LoggingHandler());
                        }
                    });

            Channel ch = b.bind(PORT).sync().channel();
            ch.closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

}
