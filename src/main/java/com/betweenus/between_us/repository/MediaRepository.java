package com.betweenus.between_us.repository;

import com.betweenus.between_us.model.Media;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MediaRepository extends JpaRepository<Media, Long> {

    List<Media> findAllByOrderByCreatedAtDesc();

    List<Media> findBySavedTrueOrderByCreatedAtDesc();
}