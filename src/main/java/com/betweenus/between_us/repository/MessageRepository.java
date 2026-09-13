package com.betweenus.between_us.repository;

import com.betweenus.between_us.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findAllByOrderByCreatedAtAsc();

    List<Message> findByTextContainingIgnoreCaseOrderByCreatedAtAsc(
            String text
    );
}