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

data class ShowCommand(val name: String) : CommandBase() {
    override fun isValid() = name.isNotEmpty()
}

data class FindCommand(val query: String) : CommandBase() {
    private val emailRegex = Regex("^[\\w.]+@[\\w.]+\\.[a-zA-Z]{2,}$")
    private val phoneRegex = Regex("^\\+\\d{1,15}$")
    override fun isValid(): Boolean {
        return emailRegex.matches(query) || phoneRegex.matches(query)
    }
}

data class Person(val name: String) {
    val phones = mutableListOf<String>()
    val emails = mutableListOf<String>()
}

class ContactManager {
    private val contacts = mutableMapOf<String, Person>()

    fun readCommand(input: String): CommandBase {
        val parts = input.split(" ")
        return when {
            input.equals("exit", ignoreCase = true) -> ExitCommand
            input.equals("help", ignoreCase = true) -> HelpCommand
            parts[0].equals("show", ignoreCase = true) && parts.size == 2 -> ShowCommand(parts[1])
            parts[0].equals("find", ignoreCase = true) && parts.size == 2 -> FindCommand(parts[1])
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
                    val person = contacts.getOrPut(command.name) { Person(command.name) }
                    person.phones.add(command.phone)
                    println("Добавлен телефон для ${command.name}: ${command.phone}")
                } else {
                    println("Ошибка: неверный формат телефона.")
                }
            }
            is AddEmailCommand -> {
                if (command.isValid()) {
                    val person = contacts.getOrPut(command.name) { Person(command.name) }
                    person.emails.add(command.email)
                    println("Добавлен email для ${command.name}: ${command.email}")
                } else {
                    println("Ошибка: неверный формат email.")
                }
            }
            is ShowCommand -> {
                val person = contacts[command.name]
                if (person == null) {
                    println("Человек с именем ${command.name} не найден.")
                } else {
                    println("Контакты для ${command.name}:")
                    println("Телефоны: ${person.phones.joinToString(", ")}")
                    println("Emails: ${person.emails.joinToString(", ")}")
                }
            }
            is FindCommand -> {
                val foundPeople = contacts.values.filter { person ->
                    command.query in person.phones || command.query in person.emails
                }
                if (foundPeople.isEmpty()) {
                    println("Никто не найден по запросу ${command.query}")
                } else {
                    println("Люди, найденные по запросу ${command.query}:")
                    foundPeople.forEach { println(it.name) }
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
            show <Имя> - показать контакт с указанным именем
            find <Номер телефона или email> - найти людей по номеру телефона или email
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
