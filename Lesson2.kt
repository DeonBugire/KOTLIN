import java.util.Scanner

interface Command {
    fun isValid(): Boolean
}

sealed class CommandBase : Command

object ExitCommand : CommandBase() {
    override fun isValid() = true
}

object HelpCommand : CommandBase() {
    override fun isValid() = true
}

data class AddPhoneCommand(val name: String, val phone: String) : CommandBase() {
    private val phoneRegex = Regex("^\\+\\d{1,15}$")
    override fun isValid(): Boolean {
        return phoneRegex.matches(phone)
    }
}

data class AddEmailCommand(val name: String, val email: String) : CommandBase() {
    private val emailRegex = Regex("^[\\w.]+@[\\w.]+\\.[a-zA-Z]{2,}$")
    override fun isValid(): Boolean {
        return emailRegex.matches(email)
    }
}

object ShowCommand : CommandBase() {
    override fun isValid() = true
}

data class Person(var name: String = "", var phone: String = "", var email: String = "")

class ContactManager {
    private var lastPerson: Person? = null
    fun readCommand(input: String): CommandBase {
        val parts = input.split(" ")
        return when {
            input.equals("exit", ignoreCase = true) -> ExitCommand
            input.equals("help", ignoreCase = true) -> HelpCommand
            input.equals("show", ignoreCase = true) -> ShowCommand
            input.startsWith("add ") && parts.size >= 4 -> {
                val name = parts[1]
                val type = parts[2].lowercase()
                val value = parts[3]

                when (type) {
                    "phone" -> AddPhoneCommand(name, value)
                    "email" -> AddEmailCommand(name, value)
                    else -> HelpCommand
                }
            }
            else -> HelpCommand
        }
    }

    fun processCommand(command: CommandBase) {
        when (command) {
            is AddPhoneCommand -> {
                if (command.isValid()) {
                    lastPerson = Person(command.name, command.phone, "")
                    println("Имя: ${command.name}, Телефон: ${command.phone}")
                } else {
                    println("Ошибка: неверный формат телефона.")
                }
            }
            is AddEmailCommand -> {
                if (command.isValid()) {
                    lastPerson = Person(command.name, "", command.email)
                    println("Имя: ${command.name}, Email: ${command.email}")
                } else {
                    println("Ошибка: неверный формат email.")
                }
            }
            is ShowCommand -> {
                if (lastPerson == null) {
                    println("Not initialized")
                } else {
                    println("Последний контакт: $lastPerson")
                }
            }
            is ExitCommand -> println("Завершение программы.")
            is HelpCommand -> printHelp()
        }
    }

    private fun printHelp() {
        println(
            """
            Доступные команды:
            exit - выйти из программы
            help - показать это сообщение
            add <Имя> phone <Номер телефона> - добавить контакт с номером телефона (пример: +123456789)
            add <Имя> email <Адрес электронной почты> - добавить контакт с email (пример: name@example.com)
            show - показать последний добавленный контакт
            """.trimIndent()
        )
    }
}

fun main() {
    val contactManager = ContactManager()
    val scanner = Scanner(System.`in`)

    while (true) {
        print("Введите команду: ")
        val input = scanner.nextLine()
        val command = contactManager.readCommand(input)

        if (!command.isValid()) {
            contactManager.processCommand(HelpCommand)
        } else {
            contactManager.processCommand(command)
            if (command is ExitCommand) break
        }
    }
}