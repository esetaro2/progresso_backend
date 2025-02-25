package com.progresso.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentDto {

  private Long id;

  @NotBlank(message = "Content cannot be empty. Please provide some text for your comment.")
  @Size(max = 500, message = "Content is too long. It must be between 1 and 500 characters.")
  @Pattern(regexp = "^\\S.*", message = "Content cannot start with a space. "
      + "Please remove the leading space.")
  private String content;

  private LocalDateTime creationDate;

  @NotNull(message = "User ID is required. Please specify the user ID.")
  private Long userId;

  private String userFirstName;

  private String userLastName;

  private String userUsername;

  @NotNull(message = "Project ID is required. Please specify the project ID.")
  private Long projectId;

  private Boolean modified;

  private LocalDateTime modifiedDate;

  private Boolean deleted;

  private Long parentId;

  private String parentContent;

  private LocalDateTime parentCreationDate;

  private Long parentUserId;

  private String parentUserFirstName;

  private String parentUserLastName;

  private String parentUserUsername;

  private Boolean parentModified;

  private LocalDateTime parentModifiedDate;

  private Boolean parentDeleted;
}
