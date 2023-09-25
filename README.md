# Trecord backend

### Intro

- Gradle mono module project
- Monolithic Structure
- 3-layer(presentation,logic,data) Structure

### Tech

- Backend: Java 17, SpringBoot 3, JPA with QueryDSL, JUnit5, Mockito, MariaDB
- Infra: Github actions, AWS EC2 with DB
    - Use EC2 with EB instead of ECS for cost and management issues

### Build

1. Before build and run application, you should set up database with the information below

    - Jdbc url: jdbc:mariadb://localhost:3306/trecord
    - Username: root
    - Password: 1234

2. After you set database
    - create schema named `trecord`
    - and run `./database/service-domain-create.sql` in `trecord`

3. After you set schema, run command below in your terminal

  ```shell
  ./gradlew clean build && java -jar build/libs/*.jar
  ```

### External APIs in use

The following APIs are being invoked for Google Oauth2 authentication

- https://oauth2.googleapis.com/token
- https://www.googleapis.com/oauth2/v3/userinfo

> Ref: https://developers.google.com/identity/protocols/oauth2/web-server?hl=ko#exchange-authorization-code

### ERD

![](images/erd%20diagram.png)

### AWS Architecture

![](images/aws%20architecture.jpg)

### Git flow

```sh
|-- master
|   |-- hotfix
|   |   |-- #885
|-- develop
|   |-- feat
|   |   |-- #883
|   |   |-- #884
|   |-- refactor
|   |   |-- #887
```

### Issue

1. ECS 배포 실패
2. Spring Security 없이 Cors Filter 적용 시, Filter 작동 X
