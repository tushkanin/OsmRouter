package com.osmrouter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.util.CharsetUtil;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.netty.handler.codec.http.HttpHeaders.Names.ACCESS_CONTROL_ALLOW_ORIGIN;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * User: Aleksey.Shulga
 * Date: 25.08.13
 * Time: 11:48
 */
public class RouteRequestHandler extends SimpleChannelInboundHandler<HttpRequest> {

    private final Router router;

    public RouteRequestHandler(Router router) {
        this.router = router;
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpRequest request) throws Exception {
        QueryStringDecoder queryStringDecoder = new QueryStringDecoder(request.getUri());
        Map<String, List<String>> params = queryStringDecoder.parameters();
        List<String> pointsParam;
        if (!params.isEmpty() && (pointsParam = params.get("points")) != null) {
            Type type = new TypeToken<List<List<Double>>>() {
            }.getType();
            List<RouteSegment> route = new ArrayList<RouteSegment>();
            try {
                for (String pointsJson : pointsParam) {
                    List<List<Double>> points = new Gson().fromJson(pointsJson, type);
                    route.addAll(router.makeRoute(points));
                }
            } catch (Exception e){
                e.printStackTrace();
                internalServerError(ctx,e.getMessage());
                return;
            }

            FullHttpResponse response = new DefaultFullHttpResponse(
                    HTTP_1_1, OK, Unpooled.copiedBuffer(new Gson().toJson(route), CharsetUtil.UTF_8));


            response.headers().set(CONTENT_TYPE, "application/json; charset=UTF-8");
            response.headers().set(ACCESS_CONTROL_ALLOW_ORIGIN,"*");
            ctx.write(response).addListener(ChannelFutureListener.CLOSE);
        }  else {
            badRequest(ctx);
        }
    }

    private void internalServerError(ChannelHandlerContext ctx,String error) {
        FullHttpResponse response = new DefaultFullHttpResponse(
                HTTP_1_1, INTERNAL_SERVER_ERROR,Unpooled.copiedBuffer(error, CharsetUtil.UTF_8));
        ctx.write(response).addListener(ChannelFutureListener.CLOSE);
    }

    private void badRequest(ChannelHandlerContext ctx) {
        FullHttpResponse response = new DefaultFullHttpResponse(
                HTTP_1_1, BAD_REQUEST);
        ctx.write(response).addListener(ChannelFutureListener.CLOSE);
    }
}
