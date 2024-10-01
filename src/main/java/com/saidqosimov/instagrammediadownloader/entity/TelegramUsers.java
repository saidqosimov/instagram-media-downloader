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
    /*SELECT setval('telegram_users_id_seq', (SELECT MAX(id) FROM telegram_users));

PGPASSWORD=kHHMEOcqeelVDLzGZGEYKaqnycgyBACa psql -h monorail.proxy.rlwy.net -U postgres -p 39850 -d railway

CREATE TABLE telegram_users (
    id SERIAL PRIMARY KEY,
    chat_id BIGINT UNIQUE NOT NULL,
    first_name VARCHAR(255),
    lang VARCHAR(50),
    username VARCHAR(32),
    enabled BOOLEAN DEFAULT TRUE
);
 */