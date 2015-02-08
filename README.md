#分支（branch）说明
* master - 正式发布版本，暂时不要提交到该分支
* dev - 开发分支，请提交到该分支
#提交说明
* 每位开发者在本地建立local分支
* 将github的dev分支更新（pull）到本地的local分支
* 在local分支提交修改后，将最新的github上的dev分支pull到本地的dev分支
* 切换到本地dev分支，将local分支合并（merge）到dev分支
* 提交本地dev分支到github
```
git pull origin dev:local #将github上的dev分支更新并合并到本地的local分支
git checkout local #切换到本地local分支
#代码修改完成后，执行以下：
git add . #添加文件到git仓库
git commit -m "提交备注，简要描述修改内容，用英文"
git pull origin dev:dev #将github上的dev分支更新到本地的dev分支
git checkout dev #切换到本地dev分支
git merge local #将local分支合并到dev分支
git push origin dev #将本地dev分支推送到github上的dev分支

```
