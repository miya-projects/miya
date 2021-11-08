# minio
docker volume create minio-config
docker volume create minio-data
docker network create minio

docker run -p 9000:9000 -d --name minio --restart=always \
  --network minio \
  -e "MINIO_ACCESS_KEY=AKIAIOSFOKLDSFMPLE" \
  -e "MINIO_SECRET_KEY=wJalrXHJjKJI/K7MDENG/bPxRdiCYEXAMPLEKEY" \
  -v minio-data:/data \
  -v minio-config:/root/.minio \
  minio/minio server /data
