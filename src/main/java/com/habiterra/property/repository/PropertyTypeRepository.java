package com.habiterra.property.repository;
import com.habiterra.property.entity.TypeBien;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PropertyTypeRepository extends JpaRepository<TypeBien, Long> {}

