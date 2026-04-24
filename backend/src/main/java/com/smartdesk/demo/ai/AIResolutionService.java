package com.smartdesk.demo.ai;
import com.smartdesk.demo.entity.Ticket;
import com.smartdesk.demo.entity.TicketEmbedding;
import com.smartdesk.demo.repository.TicketEmbeddingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AIResolutionService {

    private final ChatClient chatClient;
    private final EmbeddingModel embeddingModel;
    private final TicketEmbeddingRepository embeddingRepo;

    // Step 1: When a ticket is resolved, save its embedding for future RAG
    public void saveEmbedding(Ticket ticket) {
        String content = ticket.getTitle() + " " + ticket.getDescription();
        float[] vector = embeddingModel.embed(content);

        TicketEmbedding embedding = new TicketEmbedding();
        embedding.setTicket(ticket);
        embedding.setContent(content);
        embedding.setEmbedding(vector);
        embeddingRepo.save(embedding);
    }

    // Step 2: When a new ticket comes in, find similar past tickets and ask AI
    public String suggestResolution(String title, String description) {
        // Convert ticket to vector
        float[] queryVector = embeddingModel.embed(title + " " + description);

        // Search for top 3 similar resolved tickets
        List<Object[]> similar = embeddingRepo.findSimilarResolved(queryVector, 3);

        if (similar.isEmpty()) {
            return "No similar past tickets found. Please investigate manually.";
        }

        // Build context from past resolutions
        String context = similar.stream()
                .map(row -> "Issue: " + row[1] + "\nResolution: " + row[2])
                .collect(Collectors.joining("\n\n"));

        // Ask Llama 3 with context (RAG!)
        String prompt = """
            You are a helpful IT support assistant.
            Based on these similar resolved tickets from our system:
            %s
            
            Now suggest a resolution for this new ticket:
            Title: %s
            Description: %s
            
            Give a concise, step-by-step resolution in 3-5 bullet points.
            """.formatted(context, title, description);

        return chatClient.prompt()
                .user(prompt)
                .call()
                .content();
    }
}
