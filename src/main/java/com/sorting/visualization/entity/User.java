package com.sorting.visualization.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("users")
public class User {
    @TableId(type = IdType.AUTO)
    private Long userId;
    private String username;
    private String passwordHash;
    private String role;       // student/teacher/admin
    private String email;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
