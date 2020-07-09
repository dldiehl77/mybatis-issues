package test;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;

import test.dao.UserDAO;
import test.model.User;
import test.service.UserService;

/**
 * If the MockConfig (TestConfiguration class) is commented out, you get an error on the call to Mockito.verify because while
 * the UserDAO object injected here is a Mockito proxy, the UserDAO object that is injected into the UserService class
 * is a completely different object.  The output from the two println statements in this project appear like this:
 * <p>
 * UserService: com.sun.proxy.$Proxy105 (org.apache.ibatis.binding.MapperProxy@5402612e) : 1686151267
 * SpringBootTest: test.dao.UserDAO$MockitoMock$1604843876 (test.dao.UserDAO#0 bean) : 226230494
 * SpringBootTest: test.dao.UserDAO$MockitoMock$1604843876 (test.dao.UserDAO#0 bean) : 226230494
 * <p>
 * <p>
 * However, if you un-comment the MockConfig class below, this seems to force the Mockito object to be injected into the
 * UserService class as well.  Here is the output from the two println statements in this case:
 * <p>
 * UserService: test.dao.UserDAO$MockitoMock$238072633 (userDAO bean) : 40133542
 * SpringBootTest: test.dao.UserDAO$MockitoMock$238072633 (userDAO bean) : 40133542
 * SpringBootTest: test.dao.UserDAO$MockitoMock$238072633 (userDAO bean) : 40133542
 * <p>
 * While I can do this and make things work, this just started happening when I upgrade from Spring Boot 2.2.6 to 2.2.7.  It also
 * seems like I shouldn't have to do something this hacky for this type of test that I want to do.  I also tried this in Spring
 * Boot 2.2.8 and 2.3.1 and I got the same result as I did using 2.2.7.  So, something changed, but I don't know what.
 * <p>
 * A further indication that what I am doing to make this work is not right, I get this warning message when I apply my hack:
 * <p>
 * o.m.s.mapper.ClassPathMapperScanner : Skipping MapperFactoryBean with name 'userDAO' and 'test.dao.UserDAO'
 * mapperInterface. Bean already defined with the same name!
 * <p>
 * I am not sure if I am doing something wrong, or if the problem is with Mockito, Spring, MyBatis or the Spring MyBatis project.
 */

@org.springframework.boot.test.context.SpringBootTest(webEnvironment = org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({ "unit-test" })
public class SpringBootTest {

    @Autowired
    private UserService userService;

    @MockBean
    private UserDAO userDAO;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    public void printObjectInfo() {
        System.out.println("SpringBootTest: " + userDAO.getClass()
                                                       .getName() + " (" + userDAO + ") : " + System.identityHashCode(userDAO));
    }

    @BeforeEach
    public void evictAllCachesBefore() {
        evictAllCaches();
        when(userDAO.getUser(100)).thenReturn(new User());
    }

    @AfterEach
    public void evictAllCachesAfter() {
        evictAllCaches();
    }

    private void evictAllCaches() {
        cacheManager.getCacheNames()
                    .forEach((name) -> cacheManager.getCache(name)
                                                   .clear());
    }

    /**
     * Test that subsequent calls are fulfilled by the cache/proxy and don't make it through to the service method.
     */
    @Test
    public void testCaching() {
        userService.getUser(100);
        verify(userDAO, times(1)).getUser(100);

        // Should still be only 1 call since 2nd call should hit the proxy/cache.
        userService.getUser(100);
        verify(userDAO, times(1)).getUser(100);
    }

    /**
     * Test that the cache eviction is done properly.
     */
    @Test
    public void testCacheClearing() {
        userService.getUser(100);
        userService.dummyMethodToEvictCaches();
        userService.getUser(100);
        verify(userDAO, times(2)).getUser(100);
    }

    // Uncomment this class to make the test pass!
    /*@TestConfiguration
    static class MockConfig {
        @Bean
        public UserDAO userDAO() {
            return Mockito.mock(UserDAO.class);
        }
    }*/
}
