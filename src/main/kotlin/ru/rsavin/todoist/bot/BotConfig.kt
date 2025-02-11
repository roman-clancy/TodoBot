package ru.rsavin.todoist.bot

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession

@Configuration
class BotConfig {

    @Value("\${bot.token}")
    private lateinit var botToken: String

    @Bean
    fun telegramBotsApi(bot: TodoistBot): TelegramBotsApi {
        val api = TelegramBotsApi(DefaultBotSession::class.java)
        api.registerBot(bot)
        return api
    }

    @Bean
    fun todoistBot(botService: BotService): TodoistBot {
        return TodoistBot(botToken, botService)
    }
}