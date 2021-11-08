// package com.miya.websocket.spring.bak;
//
// import com.miya.websocket.spring.WebSocketMessage;
// import lombok.extern.slf4j.Slf4j;
// import org.springframework.stereotype.Component;
// import org.springframework.web.socket.WebSocketSession;
// import java.util.ArrayList;
// import java.util.HashMap;
// import java.util.List;
// import java.util.Map;
//
// /**
//  * @author 杨超辉
//  * @date 2018/9/3
//  * @description websocket回调注册器
//  */
// @Slf4j
// @Component
// public class WebsocketHandlerRegister {
//     /**
//      * 消息处理回调
//      */
//     private final Map<WSModule, List<WebsocketCallback>> callbacks = new HashMap<>();
//
//     /**
//      * 添加消息回调
//      *
//      * @param model             消息属于的模块
//      * @param websocketCallback 回调
//      */
//     public void addHandler(WSModule model, WebsocketCallback websocketCallback) {
//         if (!this.callbacks.containsKey(model)) {
//             this.callbacks.put(model, new ArrayList<>());
//         }
//         this.callbacks.get(model).add(websocketCallback);
//     }
//
//     public List<WebsocketCallback> getCallbacks(WSModule model) {
//         return callbacks.get(model);
//     }
// }
//
//
// @FunctionalInterface
// interface WebsocketCallback {
//     /**
//      * 接收消息后的回调
//      */
//     void callback(WebSocketMessage message, WebSocketSession session);
//
// }
//
