package test.service;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import test.dao.UserDAO;
import test.model.User;

@Service
public class UserService {

    private final UserDAO userDAO;

    public UserService(UserDAO userDAO) {
        System.out.println("UserService: " + userDAO.getClass()
                                                    .getName() + " (" + userDAO + ") : " + System.identityHashCode(userDAO));
        this.userDAO = userDAO;
    }

    @Cacheable("userCache")
    public User getUser(int id) {
        return userDAO.getUser(id);
    }

    @Caching(evict = { @CacheEvict(cacheNames = { "userCache" }, allEntries = true) })
    public void dummyMethodToEvictCaches() {
    }
}
