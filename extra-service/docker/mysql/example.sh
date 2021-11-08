docker network create db
docker volume create db-conf
docker volume create db-data

docker run --name db \
    --network=db \
    --restart=always \
    -p 3306:3306 -v db-data:/var/lib/mysql -v db-conf:/etc/mysql/conf.d \
    -e MYSQL_ROOT_PASSWORD=root -e TZ=Asia/Shanghai -d mysql:5.7
