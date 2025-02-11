package ru.rsavin.todoist.bot

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.exceptions.TelegramApiException

@Component
class TodoistBot(
    @Value("\${bot.token}") botToken: String,
    private val botService: BotService
) : TelegramLongPollingBot(botToken) {

    @Value("\${bot.allowed-user}")
    private lateinit var allowedUser: String

    @Value("\${todoist.orders-list-id}")
    private lateinit var ordersListId: String

    @Value("\${todoist.tasks-list-id}")
    private lateinit var tasksListId: String

    override fun onUpdateReceived(update: Update) {
        if (update.hasMessage()) {
            val message = update.message
            val username = message.from.userName

            // Проверка пользователя
            if (allowedUser != username) {
                sendMessage(message.chatId, "🚫 Доступ запрещен.")
                return
            }

            // Анализ сообщения
            val text = message.text
            val listId = if (botService.isMarketplaceLink(text)) ordersListId else tasksListId

            // Создание задачи в Todoist
            val isTaskCreated = botService.createTodoistTask(text, listId)

            // Ответ пользователю
            if (isTaskCreated) {
                sendMessage(message.chatId, "✅ Задача создана!")
            } else {
                sendMessage(message.chatId, "❌ Ошибка при создании задачи.")
            }
        }
    }

    private fun sendMessage(chatId: Long, text: String) {
        val message = SendMessage(chatId.toString(), text)
        try {
            execute(message)
        } catch (e: TelegramApiException) {
            e.printStackTrace()
        }
    }

    override fun getBotUsername(): String {
        return "TodoistBot"
    }
}