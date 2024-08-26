package com.saidqosimov.instagrammediadownloader.entity;

import com.saidqosimov.instagrammediadownloader.enums.Language;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "telegram_users")
@Builder
public class TelegramUsers implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private Long chatId;
    private String firstName;
    private Language lang;
    @Size(max = 32)
    private String username;
    private boolean enabled = true;
}