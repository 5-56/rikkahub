package me.rerere.rikkahub.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import me.rerere.ai.core.TokenUsage

@Entity
data class ConversationEntity(
    @PrimaryKey val id: String,
    @ColumnInfo("title") val title: String,
    @ColumnInfo("messages") val messages: String,
    @ColumnInfo("usage") val tokenUsage: TokenUsage?,
    @ColumnInfo("create_at") val createAt: Long,
    @ColumnInfo("update_at") val updateAt: Long,
)