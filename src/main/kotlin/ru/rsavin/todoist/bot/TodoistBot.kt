import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import ru.rsavin.todoist.bot.BotService
import java.util.Timer
import java.util.TimerTask

class TodoistBot(
    botToken: String,
    private val botService: BotService
) : TelegramLongPollingBot(botToken) {

    @Value("\${bot.allowed-user}")
    private lateinit var allowedUser: String

    @Value("\${todoist.orders-list-id}")
    private lateinit var ordersListId: String

    @Value("\${todoist.tasks-list-id}")
    private lateinit var tasksListId: String

    // Хранение временных данных для каждого пользователя
    private val userTasks = mutableMapOf<Long, UserTaskData>()

    override fun onUpdateReceived(update: Update) {
        if (update.hasMessage()) {
            val message = update.message
            val chatId = message.chatId
            val username = message.from.userName

            // Проверка пользователя
            if (allowedUser != username) {
                sendMessage(chatId, "🚫 Доступ запрещен.")
                return
            }

            // Обработка сообщения
            handleUserMessage(chatId, message.text)
        }
    }

    private fun handleUserMessage(chatId: Long, text: String?) {
        val taskData = userTasks.getOrPut(chatId) { UserTaskData() }

        // Сброс таймера
        taskData.timer?.cancel()
        taskData.timer = Timer()

        // Установка таймера на 5 секунд
        taskData.timer?.schedule(object : TimerTask() {
            override fun run() {
                createTaskFromUserData(chatId, taskData)
                userTasks.remove(chatId) // Очищаем данные
            }
        }, 5000) // 5 секунд

        // Сбор данных для задачи
        if (taskData.title == null) {
            taskData.title = text
        } else {
            taskData.description.appendLine(text)
        }
    }

    private fun createTaskFromUserData(chatId: Long, taskData: UserTaskData) {
        if (taskData.title != null) {
            // Создание задачи в Todoist
            val listId = if (botService.isMarketplaceLink(taskData.title!!)) ordersListId else tasksListId
            val isTaskCreated = botService.createTodoistTask(
                taskData.title!!,
                taskData.description.toString(),
                listId
            )

            if (isTaskCreated) {
                sendMessage(chatId, "✅ Задача создана автоматически!")
            } else {
                sendMessage(chatId, "❌ Ошибка при создании задачи.")
            }
        } else {
            sendMessage(chatId, "⚠️ Нет данных для создания задачи.")
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

data class UserTaskData(
    var title: String? = null,
    var description: StringBuilder = StringBuilder(),
    var timer: Timer? = null
)