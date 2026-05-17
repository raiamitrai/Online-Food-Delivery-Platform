@echo off
set "JAVA_HOME=D:\fooddelivery\jdk17\jdk-17.0.10+7"
set "PATH=%JAVA_HOME%\bin;%PATH%"
echo Starting QuickBite Platform with Java 17...
echo ===================================

echo Starting Eureka Server...
start "Eureka Server" cmd /c "..\apache-maven-3.9.6\bin\mvn.cmd spring-boot:run -pl eureka-server"
timeout /t 10

echo Starting API Gateway...
start "API Gateway" cmd /c "..\apache-maven-3.9.6\bin\mvn.cmd spring-boot:run -pl api-gateway"
timeout /t 5

echo Starting Microservices...
start "Auth Service" cmd /c "..\apache-maven-3.9.6\bin\mvn.cmd spring-boot:run -pl auth-service"
start "Restaurant Service" cmd /c "..\apache-maven-3.9.6\bin\mvn.cmd spring-boot:run -pl restaurant-service"
start "Menu Service" cmd /c "..\apache-maven-3.9.6\bin\mvn.cmd spring-boot:run -pl menu-service"
start "Cart Service" cmd /c "..\apache-maven-3.9.6\bin\mvn.cmd spring-boot:run -pl cart-service"
start "Order Service" cmd /c "..\apache-maven-3.9.6\bin\mvn.cmd spring-boot:run -pl order-service"
start "Payment Service" cmd /c "..\apache-maven-3.9.6\bin\mvn.cmd spring-boot:run -pl payment-service"
start "Delivery Service" cmd /c "..\apache-maven-3.9.6\bin\mvn.cmd spring-boot:run -pl delivery-service"
start "Review Service" cmd /c "..\apache-maven-3.9.6\bin\mvn.cmd spring-boot:run -pl review-service"
start "Notification Service" cmd /c "..\apache-maven-3.9.6\bin\mvn.cmd spring-boot:run -pl notification-service"

echo All backend services have been started in separate windows!
echo Please make sure your MySQL server is running and the databases are created.
pause
