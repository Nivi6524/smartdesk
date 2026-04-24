package com.smartdesk.demo.controller;

import com.smartdesk.demo.dto.CreateTicketRequest;
import com.smartdesk.demo.dto.ResolveRequest;
import com.smartdesk.demo.entity.Ticket;
import com.smartdesk.demo.service.TicketService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class TicketController {

    private final TicketService ticketService;

    @PostMapping
    public ResponseEntity<Ticket> create(
            @RequestBody CreateTicketRequest req,
            @AuthenticationPrincipal String email
    ) {
        return ResponseEntity.ok(ticketService.createTicket(req, email));
    }

    @GetMapping
    public ResponseEntity<List<Ticket>> getAll() {
        return ResponseEntity.ok(ticketService.getAllTickets());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Ticket> getOne(@PathVariable UUID id) {
        return ticketService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/resolve")
    public ResponseEntity<Ticket> resolve(
            @PathVariable UUID id,
            @RequestBody ResolveRequest req
    ) {
        return ResponseEntity.ok(ticketService.resolveTicket(id, req.getResolution()));
    }
}
