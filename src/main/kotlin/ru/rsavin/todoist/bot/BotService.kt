package ru.rsavin.todoist.bot
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class BotService {

    @Value("\${todoist.token}")
    private lateinit var todoistToken: String

    private val restTemplate = RestTemplate()

    fun isMarketplaceLink(text: String): Boolean {
        return text.contains("ozon.ru") || text.contains("wildberries.ru")
    }

    fun createTodoistTask(content: String, listId: String): Boolean {
        val url = "https://api.todoist.com/rest/v2/tasks"
        val requestBody = """{"content": "$content", "project_id": "$listId"}"""

        return try {
            val headers = HttpHeaders()
            headers.set("Content-Type", "application/json")
            headers.set("Authorization", "Bearer $todoistToken")
            val entity = HttpEntity<String>(requestBody, headers)
            restTemplate.exchange(url, HttpMethod.POST, entity, String::class.java)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}