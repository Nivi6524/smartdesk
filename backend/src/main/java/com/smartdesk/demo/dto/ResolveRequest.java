package com.smartdesk.demo.dto;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResolveRequest {

    @NotBlank(message = "Resolution cannot be empty")
    private String resolution;
}