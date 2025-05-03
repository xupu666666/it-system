@echo off
REM 启动MongoDB和Redis（如果用docker-compose）
cd /d %~dp0
docker-compose up -d

REM 启动后端
cd backend
start cmd /k "mvn spring-boot:run -Dspring.profiles.active=default"

REM 启动前端
cd ..\frontend
start cmd /k "npm install && npm run serve"

echo 所有服务已启动，请稍等片刻后访问前端页面。
pause