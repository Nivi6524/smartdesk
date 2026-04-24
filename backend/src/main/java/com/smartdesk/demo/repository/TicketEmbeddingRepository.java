package com.smartdesk.demo.repository;

import com.smartdesk.demo.entity.TicketEmbedding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.*;

public interface TicketEmbeddingRepository extends JpaRepository<TicketEmbedding, UUID> {

    // Cosine similarity search — finds semantically similar tickets
    @Query(value = """
        SELECT te.ticket_id, te.content, t.resolution
        FROM ticket_embeddings te
        JOIN tickets t ON t.id = te.ticket_id
        WHERE t.status = 'RESOLVED'
        ORDER BY te.embedding <-> CAST(:embedding AS vector)
        LIMIT :limit
        """, nativeQuery = true)
    List<Object[]> findSimilarResolved(
            @Param("embedding") float[] embedding,
            @Param("limit") int limit
    );
}
