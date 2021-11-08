# minio
docker volume create minio-config
docker volume create minio-data
docker network create minio

docker-compose down
docker-compose up -d
