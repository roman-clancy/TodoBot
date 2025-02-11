package ru.rsavin.todoist

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class TodoistBotIntegrationApplication

fun main(args: Array<String>) {
	runApplication<TodoistBotIntegrationApplication>(*args)
}
