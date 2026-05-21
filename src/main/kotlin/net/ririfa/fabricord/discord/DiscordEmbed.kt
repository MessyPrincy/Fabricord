package net.ririfa.fabricord.discord

import net.dv8tion.jda.api.EmbedBuilder
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.ririfa.fabricord.Config
import net.ririfa.fabricord.JDA
import net.ririfa.fabricord.LM
import net.ririfa.fabricord.Logger
import net.ririfa.fabricord.translation.FabricordMessageKey
import java.awt.Color
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

object DiscordEmbed {
    private val recentLeaves = ConcurrentHashMap<UUID, Long>()
    private fun sendEmbedToDiscord(color: Color, author: String? = null, imageUrl: String, channelId: String = Config.logChannelID) {
        if (channelId.isBlank()) {
            Logger.error(LM.getSysMessage(FabricordMessageKey.Discord.Config.LogChannelIDIsBlank))
            return
        }

        val embed = EmbedBuilder().apply {
            setColor(color)
            setAuthor(author, null, imageUrl)
        }.build()

        JDA?.getTextChannelById(channelId)?.sendMessageEmbeds(embed)?.queue()
    }

    @JvmStatic
    fun sendPlayerJoinEmbed(player: ServerPlayerEntity) {
        val name = player.name.string
        val uuid = player.uuid.toString()
        val imageUrl = "https://visage.surgeplay.com/face/256/$uuid"
        val message = Config.playerJoinMessage!!.replace("%player%", name)
        sendEmbedToDiscord(Color.GREEN, message, imageUrl)
    }

    @JvmStatic
    fun sendPlayerLeftEmbed(player: ServerPlayerEntity) {
        val now = System.currentTimeMillis()
        val lastLeave = recentLeaves[player.uuid]
        // Discard the second duplicate leave messages
        if (lastLeave != null && now - lastLeave < 3000) {
            return
        }
        recentLeaves[player.uuid] = now
        val name = player.name.string
        val uuid = player.uuid.toString()
        val imageUrl = "https://visage.surgeplay.com/face/256/$uuid"
        val message = Config.playerLeaveMessage!!.replace("%player%", name)
        sendEmbedToDiscord(Color.RED, message, imageUrl)
    }

    @JvmStatic
    fun sendPlayerDeathEmbed(player: ServerPlayerEntity, deathMessage: Text) {
        val uuid = player.uuid.toString()
        val imageUrl = "https://visage.surgeplay.com/face/256/$uuid"
        sendEmbedToDiscord(Color.BLACK, deathMessage.string, imageUrl)
    }

    @JvmStatic
    fun sendPlayerGrantCriterionEmbed(player: ServerPlayerEntity, criterion: String) {
        val name = player.name.string
        val uuid = player.uuid.toString()
        val imageUrl = "https://visage.surgeplay.com/face/256/$uuid"
        sendEmbedToDiscord(Color.YELLOW, "$name has made the advancement $criterion", imageUrl)
    }
}