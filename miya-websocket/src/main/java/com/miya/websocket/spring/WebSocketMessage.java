package com.miya.websocket.spring;

import cn.hutool.json.JSONUtil;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * @author 杨超辉
 * @date 2018/9/3
 * @description websocket传输实体类
 */
@Data
@RequiredArgsConstructor(staticName = "of")
@NoArgsConstructor
public class WebSocketMessage {

    /**
     * 提示信息
     * HtmlUtils.htmlEscape(message.getName())
     */
    @NonNull
    private String msg;
    /**
     * 传输数据
     */
    @NonNull
    private Object data;

    /**
     * 使用data创建一个websocketMessage对象
     * @param data
     * @return
     */
    public static WebSocketMessage of(Object data) {
        WebSocketMessage websocketMessage = new WebSocketMessage();
        websocketMessage.setData(data);
        return websocketMessage;
    }

    public String toJSONString() {
        return JSONUtil.toJsonStr(this);
    }
}
