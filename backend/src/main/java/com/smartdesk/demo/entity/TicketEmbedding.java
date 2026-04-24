package com.smartdesk.demo.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "ticket_embeddings", indexes = {
        @Index(name = "ticket_embedding_idx", columnList = "embedding")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketEmbedding {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false, foreignKey = @ForeignKey(name = "fk_ticket_embeddings_ticket"))
    private Ticket ticket;

    @Column(name = "embedding", nullable = false, columnDefinition = "vector(768)")
    private float[] embedding;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public void setTicketAndContent(Ticket ticket) {
        this.ticket = ticket;
        this.content = ticket.getTitle() + " " + ticket.getDescription();
    }

    /**
     * Pre-persist lifecycle hook to ensure createdAt is set
     */
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

}

