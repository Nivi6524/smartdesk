package com.smartdesk.demo.service;


import com.smartdesk.demo.ai.AIResolutionService;
import com.smartdesk.demo.dto.CreateTicketRequest;
import com.smartdesk.demo.entity.Ticket;
import com.smartdesk.demo.entity.User;
import com.smartdesk.demo.repository.TicketRepository;
import com.smartdesk.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepo;
    private final UserRepository userRepo;
    private final AIResolutionService aiService;

    // Create ticket + auto-get AI suggestion
    @Transactional
    public Ticket createTicket(CreateTicketRequest req, String userEmail) {
        User user = userRepo.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Ticket ticket = Ticket.builder()
                .title(req.getTitle())
                .description(req.getDescription())
                .priority(req.getPriority())
                .category(req.getCategory())
                .createdBy(user)
                .build();

        // Get AI suggestion immediately
        String suggestion = aiService.suggestResolution(
                req.getTitle(), req.getDescription()
        );
        ticket.setAiSuggestion(suggestion);

        return ticketRepo.save(ticket);
    }

    // When ticket is resolved, save embedding for future RAG
    @Transactional
    public Ticket resolveTicket(UUID id, String resolution) {
        Ticket ticket = ticketRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        ticket.setStatus("RESOLVED");
        ticket.setResolution(resolution);
        ticket.setResolvedAt(LocalDateTime.now());
        Ticket saved = ticketRepo.save(ticket);

        // Save to knowledge base for future RAG searches
        aiService.saveEmbedding(saved);
        return saved;
    }

    public List<Ticket> getAllTickets() { return ticketRepo.findAll(); }
    public Optional<Ticket> getById(UUID id) { return ticketRepo.findById(id); }
}
