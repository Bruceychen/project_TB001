package com.bruceychen.tb001.repository;

import com.bruceychen.tb001.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
