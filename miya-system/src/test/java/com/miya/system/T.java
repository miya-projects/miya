package com.miya.system;

import com.miya.system.module.user.model.SysUser;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class T {
    public static void main(String[] args) throws NoSuchMethodException {

        @Data
        @AllArgsConstructor
        class User{
            private String name;
            private String sex;
        }

        List<User> list = new ArrayList<>();
        list.add(new User("zs", "male"));
        list.add(new User("lisi", "female"));
        list.add(new User("zhaoliu", "male"));
        list.add(new User("wangwu", "male"));
        list.add(new User("liming", "female"));

        Map<String, List<String>> collect = list.stream().collect(Collectors.groupingBy(User::getSex, Collectors.mapping(User::getName, Collectors.toList())));


        System.out.println(123);


//        EMailService eMailService = new EMailService();
//        eMailService.sendText(Collections.singleton("2901119227@234q.com"), "haha", "212333");
    }

    public void say(){
        System.out.println(123);
    }
}
