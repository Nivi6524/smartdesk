package com.smartdesk.demo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTicketRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    private String priority;   // LOW, MEDIUM, HIGH
    private String category;   // Network, Hardware, Software etc.
}