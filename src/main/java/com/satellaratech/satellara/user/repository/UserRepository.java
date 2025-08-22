package com.satellaratech.satellara.user.repository;

import com.satellaratech.satellara.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {
}
