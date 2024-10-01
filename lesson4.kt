import java.io.File
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
    override fun isValid() = name.isNotBlank()
}

data class FindCommand(val query: String) : CommandBase() {
    override fun isValid() = query.isNotBlank()
}

data class ExportCommand(val filePath: String) : CommandBase() {
    override fun isValid() = filePath.isNotBlank()
}

data class Person(val name: String, val phones: MutableList<String> = mutableListOf(), val emails: MutableList<String> = mutableListOf())

class ContactManager {
    private val people = mutableMapOf<String, Person>()

    fun readCommand(input: String): CommandBase {
        val parts = input.split(" ")
        return when {
            input.equals("exit", ignoreCase = true) -> ExitCommand
            input.equals("help", ignoreCase = true) -> HelpCommand
            input.startsWith("show ") && parts.size == 2 -> ShowCommand(parts[1])
            input.startsWith("find ") && parts.size == 2 -> FindCommand(parts[1])
            input.startsWith("export ") && parts.size == 2 -> ExportCommand(parts[1])
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

    // Метод для обработки команд
    fun processCommand(command: CommandBase) {
        when (command) {
            is AddPhoneCommand -> {
                if (command.isValid()) {
                    val person = people.getOrPut(command.name) { Person(command.name) }
                    person.phones.add(command.phone)
                    println("Телефон добавлен: ${command.phone} к ${command.name}")
                } else {
                    println("Ошибка: неверный формат телефона.")
                }
            }
            is AddEmailCommand -> {
                if (command.isValid()) {
                    val person = people.getOrPut(command.name) { Person(command.name) }
                    person.emails.add(command.email)
                    println("Email добавлен: ${command.email} к ${command.name}")
                } else {
                    println("Ошибка: неверный формат email.")
                }
            }
            is ShowCommand -> {
                val person = people[command.name]
                if (person == null) {
                    println("Контакт не найден.")
                } else {
                    println("Контакт ${person.name}: телефоны - ${person.phones}, email - ${person.emails}")
                }
            }
            is FindCommand -> {
                val found = people.values.filter { it.phones.contains(command.query) || it.emails.contains(command.query) }
                if (found.isEmpty()) {
                    println("Записи с таким телефоном или email не найдены.")
                } else {
                    println("Найдены контакты: ${found.joinToString { it.name }}")
                }
            }
            is ExportCommand -> {
                if (command.isValid()) {
                    val json = buildJson {
                        array {
                            people.values.forEach { person ->
                                obj {
                                    "name" to person.name
                                    "phones" to person.phones
                                    "emails" to person.emails
                                }
                            }
                        }
                    }
                    File(command.filePath).writeText(json)
                    println("Данные экспортированы в ${command.filePath}")
                }
            }
            is ExitCommand -> println("Завершение программы.")
            is HelpCommand -> printHelp()
        }
    }

    // Метод для вывода справки
    private fun printHelp() {
        println(
            """
            Доступные команды:
            exit - выйти из программы
            help - показать это сообщение
            add <Имя> phone <Номер телефона> - добавить телефон к контакту
            add <Имя> email <Адрес электронной почты> - добавить email к контакту
            show <Имя> - показать контакт по имени
            find <Телефон или Email> - найти контакт по телефону или email
            export <Путь к файлу> - экспортировать данные в JSON файл
            """.trimIndent()
        )
    }
}

class JsonBuilder {
    private val result = StringBuilder()

    fun obj(init: JsonObjectBuilder.() -> Unit) {
        result.append("{")
        JsonObjectBuilder().apply(init).build().let { result.append(it) }
        result.append("}")
    }

    fun array(init: JsonArrayBuilder.() -> Unit) {
        result.append("[")
        JsonArrayBuilder().apply(init).build().let { result.append(it) }
        result.append("]")
    }

    fun build(): String = result.toString()
}

class JsonObjectBuilder {
    private val result = StringBuilder()

    infix fun String.to(value: Any) {
        if (result.isNotEmpty()) result.append(",")
        result.append("\"$this\":\"$value\"")
    }

    fun build(): String = result.toString()
}

class JsonArrayBuilder {
    private val result = StringBuilder()

    fun obj(init: JsonObjectBuilder.() -> Unit) {
        if (result.isNotEmpty()) result.append(",")
        result.append("{")
        JsonObjectBuilder().apply(init).build().let { result.append(it) }
        result.append("}")
    }

    fun build(): String = result.toString()
}

fun buildJson(init: JsonBuilder.() -> Unit): String {
    return JsonBuilder().apply(init).build()
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