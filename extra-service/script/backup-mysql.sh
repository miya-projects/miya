#!/bin/bash

# 备份文件要保存的目录
basepath='/root/backup-mysql/'

if [ ! -d "$basepath" ]; then
  mkdir -p "$basepath"
fi



backupDatabase(){

        # 要备份的数据库名，多个数据库用空格分开
        databases=(pay)

        # 循环databases数组
        for db in ${databases[*]}
          do
            if [ -d $basepath$db ];then
                echo 2 > /dev/null
            else
                mkdir $basepath$db
            fi;
            cd $basepath$db
            filename=$(date +%Y-%m-%d).sql
            # 备份数据库生成SQL文件
            /bin/nice -n 19 sudo docker exec db /usr/bin/mysqldump --routines --single-transaction -uroot -proot $db >  $filename
            # 将生成的SQL文件压缩
            /bin/nice -n 19 tar -zcvf $basepath$db/$db-$(date +%Y-%m-%d).sql.tar.gz $filename
            # 删除7天之前的备份数据
            find $basepath -mtime +7 -name "*.sql.tar.gz" -exec rm -rf {} \;
          done
        # 删除生成的SQL文件
          rm -rf $basepath$db/*sql

}

# 备份所有数据库
backupAllDatabase(){
        filename=$(date +%Y-%m-%d)-all.sql
        cd $basepath
        /bin/nice -n 19 sudo docker exec db /usr/bin/mysqldump --routines --single-transaction -uroot -proot --all-databases >  $filename
        /bin/nice -n 19 tar -zcvf $basepath$(date +%Y-%m-%d)-all.sql.tar.gz $filename

        find $basepath -mtime +7 -name "*.sql.tar.gz" -exec rm -rf {} \;
        rm -rf $basepath/*sql
}

backupAllDatabase
