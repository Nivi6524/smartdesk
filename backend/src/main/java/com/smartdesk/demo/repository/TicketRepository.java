package com.smartdesk.demo.repository;

import com.smartdesk.demo.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.*;

public interface TicketRepository extends JpaRepository<Ticket, UUID> {
    List<Ticket> findByStatus(String status);
    List<Ticket> findByCreatedBy_Id(UUID userId);

    @Query("SELECT t FROM Ticket t WHERE t.status = 'RESOLVED' ORDER BY t.resolvedAt DESC")
    List<Ticket> findResolvedTickets();
}
