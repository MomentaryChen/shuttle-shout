package com.shuttleshout.common.model.dto;

import java.time.LocalDateTime;

import javax.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 球员数据传输对象
 * 
 * @author ShuttleShout Team
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerDTO {

    private Long id;

    @NotBlank(message = "球员姓名不能为空")
    private String name;

    private String phoneNumber;

    private String notes;

    private Long teamId;

    private String teamName;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
