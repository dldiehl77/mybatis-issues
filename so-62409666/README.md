See my explanation in the class Javadoc of the SpringBootTest class (where the problem can be seen).

Basically my problem is two different beans are being injected -- the bean in my test class and the bean in my
service class are not the same object.  For the test to function correctly, both objects need to be Mockito
objects.

Mixed in here along with Spring Boot, MyBatis and Spring-MyBatis is Mockito, EhCache and Flyway.

```sh
$ svn export https://github.com/harawata/mybatis-issues/trunk/so-62409666
$ cd so-62409666
$ mvn test
```
