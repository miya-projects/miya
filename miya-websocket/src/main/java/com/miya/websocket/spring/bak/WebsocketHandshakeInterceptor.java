// package com.miya.websocket.spring.bak;
//
// import lombok.extern.slf4j.Slf4j;
// import org.apache.commons.lang3.exception.ExceptionUtils;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.server.ServerHttpRequest;
// import org.springframework.http.server.ServerHttpResponse;
// import org.springframework.http.server.ServletServerHttpResponse;
// import org.springframework.stereotype.Component;
// import org.springframework.web.socket.WebSocketHandler;
// import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;
//
// import java.io.IOException;
// import java.io.PrintWriter;
// import java.util.Iterator;
// import java.util.Map;
// import java.util.Set;
//
// /**
//  * @author 杨超辉 握手拦截器
//  * @date 2018/8/15
//  * @description
//  */
// // @Component
// @Slf4j
// public class WebsocketHandshakeInterceptor extends HttpSessionHandshakeInterceptor {
//
//     /**
//      * 握手前解析权限
//      * @param request
//      * @param response
//      * @param wsHandler
//      * @param attributes
//      * @return
//      * @throws Exception
//      */
//     @Override
//     public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
//                                    Map<String, Object> attributes) throws IOException {
//         Set<Map.Entry<String, Object>> entries = attributes.entrySet();
//         Iterator<Map.Entry<String, Object>> iterator = entries.iterator();
//         while (iterator.hasNext()) {
//             Map.Entry<String, Object> next = iterator.next();
//             log.info("attributes: {}:  {}",next.getKey(), next.getValue());
//         }
//
//         boolean flag = request.getPrincipal() == null;
//         if (flag){
//             log.debug("用户未登录，不允许连接ws");
//             response.setStatusCode(HttpStatus.UNAUTHORIZED);
//             PrintWriter writer = ((ServletServerHttpResponse) response).getServletResponse().getWriter();
//             writer.write("未登录");
//             writer.flush();
//             return false;
//         }
//         return true;
//     }
//
//     @Override
//     public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
//                                Exception ex) {
//         log.debug("{}握手成功", request.getPrincipal());
//         if (ex != null){
//             log.error(ExceptionUtils.getStackTrace(ex));
//         }
//     }
//
// }
