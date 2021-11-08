// package com.miya.websocket.spring.bak;
//
// import cn.hutool.json.JSONUtil;
// import com.miya.websocket.spring.WebSocketMessage;
// import lombok.extern.slf4j.Slf4j;
// import org.apache.commons.lang3.exception.ExceptionUtils;
// import org.springframework.security.core.Authentication;
// import org.springframework.stereotype.Service;
// import org.springframework.web.socket.CloseStatus;
// import org.springframework.web.socket.TextMessage;
// import org.springframework.web.socket.WebSocketSession;
// import org.springframework.web.socket.handler.TextWebSocketHandler;
// import javax.annotation.Resource;
// import java.lang.reflect.Field;
// import java.lang.reflect.InvocationTargetException;
// import java.lang.reflect.Method;
// import java.util.List;
// import java.util.Objects;
//
// /**
//  * @author 杨超辉
//  * @date 2018/8/15
//  * @description websocket消息回调
//  */
// @Slf4j
// @Service
// public class WebsocketHandler extends TextWebSocketHandler {
//
//
//     @Resource
//     private WebsocketHandlerRegister handlerRegister;
//
//     @Resource
//     private WebSocketService webSocketService;
//
//     /**
//      * 连接成功回调
//      */
//     @Override
//     public void afterConnectionEstablished(WebSocketSession session) {
//         log.info("websocket连接成功 {}", getSessionUserName(session));
//         WebSocketService.USER_MAP.put(getSessionUserId(session), session);
//     }
//
//     /**
//      * 接收消息回调
//      */
//     @Override
//     protected void handleTextMessage(WebSocketSession session, TextMessage message) {
//         //  log.debug("接收到：" + message.getPayload());
//         String payload = message.getPayload();
//         if("ping".equals(payload)){
//             return;
//         }
//         WebSocketMessage websocketMessage = null;
//         try{
//             websocketMessage = JSONUtil.toBean(payload, WebSocketMessage.class);
//         }catch(Exception e){
//             log.warn("未处理的websocket消息: 无法json化: {}", payload);
//             return;
//         }
//         List<WebsocketCallback> callbacks = handlerRegister.getCallbacks(websocketMessage.getModel());
//         final WebSocketMessage finalWebsocketMessage = websocketMessage;
//         callbacks.forEach((callback) -> callback.callback(finalWebsocketMessage, session));
//     }
//
//     /**
//      * 发生错误回调
//      * @param session
//      * @param exception
//      * @throws Exception
//      */
//     @Override
//     public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
//         if (session.isOpen()) {
//             session.close();
//         }
//         log.debug("用户{}发生连接错误" + ExceptionUtils.getStackTrace(exception), getSessionUserName(session));
//         WebSocketService.USER_MAP.remove(getSessionUserId(session));
//     }
//
//     /**
//      * 连接关闭回调
//      * @param session
//      * @param closeStatus
//      * @throws Exception
//      */
//     @Override
//     public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) {
//         log.debug("用户{}连接关闭", getSessionUserName(session));
//         WebSocketService.USER_MAP.remove(getSessionUserId(session));
//     }
//
//     @Override
//     public boolean supportsPartialMessages() {
//         return false;
//     }
//
//
//     /**
//      * 获取当前session的用户名
//      * @return
//      */
//     private String getSessionUserName(WebSocketSession session){
//         Object details = ((Authentication) Objects.requireNonNull(session.getPrincipal())).getDetails();
//         Class<?> aClass = details.getClass();
//         Field nameField;
//         String name = null;
//         try {
//             nameField = aClass.getField("name");
//             nameField.setAccessible(true);
//             name = (String)nameField.get(details);
//         } catch (NoSuchFieldException | IllegalAccessException e) {
//             e.printStackTrace();
//         }
//         return name;
//     }
//
//     /**
//      * 获取当前session的用户id
//      * @return
//      */
//     private String getSessionUserId(WebSocketSession session){
//         Object details = ((Authentication) Objects.requireNonNull(session.getPrincipal())).getDetails();
//         return getUserId(details);
//     }
//
//     /**
//      * 获取用户对象id
//      * @return
//      */
//     private String getUserId(Object user){
//         Method getId = null;
//         try {
//             getId = user.getClass().getMethod("getId");
//             return getId.invoke(user).toString();
//         } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
//             e.printStackTrace();
//             throw new RuntimeException("获取不到用户id");
//         }
//     }
//
// }
