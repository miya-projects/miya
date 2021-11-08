#!/bin/bash

if [ -z "$1" -o -z "$2" ];then
        echo "dbg 自动创建用户和数据库程序 v1.0"
        echo "用法 ./dbg [database] [user]"
        exit
fi
read -p "输入y确认继续操作:"  i
if [ "$i" != y ];then
        exit
fi

read -p "输入密码(直接回车将自动生成密码):"  p
if [ -z "$p" ];then
        echo "gen pass"
        UUID=$(cat /proc/sys/kernel/random/uuid)
        p="${UUID//-/}"
fi

DATABASE=$1
USERNAME=$2
PASSWORD=$p

#登录
docker exec db mysql -uroot -proot -e \
"create database $DATABASE; \
CREATE USER '$USERNAME'@'%' IDENTIFIED BY '$PASSWORD'; \
GRANT all privileges ON $DATABASE.* TO '$USERNAME'; \
flush privileges;
commit;"
if [ $? -eq 0 ];then
        echo "已经生成数据库:$DATABASE,用户:$USERNAME,密码:$PASSWORD"
else
        echo "生成失败，请自行删除错误创建的数据库和用户名"
fi
