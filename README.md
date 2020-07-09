# 人脸识别ARC3.1算法
##### sqlite>.help 这个命令让我们看到许多命令
##### sqlite>.exit 命令退出sqlite，返回到#提示符。
##### sqlite>.tables 查看所有表
##### 显示字段
##### .mode column
##### .header on

root@rk3288:/data/data/com.runvision.faceagm_1v1vn
chmod 777 FaceTemplate.db

--查询验证记录表
select * from tRecord;

--表示选择降序排列的十条数据
select * from tRecord order by id desc LIMIT 10;

--表示选择1,2,3,4行记录
select * from tRecord limit 0,4;

--只选择第一条记录,从第一条开始
select * from tRecord limit 1;

--表示只选择最后一条记录
select * from tRecord limit (select count(*) from tRecord)-1,1;

--表示查询表中数据总数
select count(*) from tRecord;

--表示删除id=1的数据
DELETE FROM tRecord WHERE id = 1;

--表示模糊查询
select * from tRecord where snapImageID like '%1545983101377673%';

<img src="https://github.com/lichao3140/ArcFaceGo/blob/master/screenshot/device-001.png" width = "230" height = "400" alt="人脸识别" />
<img src="https://github.com/lichao3140/ArcFaceGo/blob/master/screenshot/device-002.png" width = "230" height = "400" alt="人脸识别" />
<img src="https://github.com/lichao3140/ArcFaceGo/blob/master/screenshot/device-003.png" width = "230" height = "400" alt="人脸识别" />
<img src="https://github.com/lichao3140/ArcFaceGo/blob/master/screenshot/device-004.png" width = "230" height = "400" alt="人脸识别" />