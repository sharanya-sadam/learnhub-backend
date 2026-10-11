package com.learnhub.dto.submission;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateSubmissionRequest {
    private String textResponse;
    private String filePath;
    private String originalFileName;
}
