package org.example.userservice;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getUsers() {
        return userRepository.findAll();
    }

    public User getUser(Long id) {
        return userRepository.findById(id)
                .orElse(null);
    }

    public User addUser(User user) {
        if (user != null)
            return userRepository.save(user);

        return null;
    }

    @Transactional
    public User updateUser(Long id, User user) {
        if (id.equals(user.getId()) && userRepository.existsById(id))
            return userRepository.save(user);

        return null;
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}
