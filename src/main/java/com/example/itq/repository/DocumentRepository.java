package com.example.itq.repository;

import com.example.itq.model.Document;
import com.example.itq.model.DocumentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, Long>, JpaSpecificationExecutor<Document> {

    Page<Document> findByIdIn(List<Long> ids, Pageable pageable);

    @Query("""
                select d from Document d
                where d.status = :status
                order by d.id
                limit :limit
            """)
    List<Document> findTopNByStatus(@Param("status") DocumentStatus status,
                                    @Param("limit") int limit);
}
