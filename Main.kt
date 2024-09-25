import java.util.Scanner

class ContactManager {
    private val phoneRegex = Regex("^\\+\\d{1,15}$")
    private val emailRegex = Regex("^[\\w.]+@[\\w.]+\\.[a-zA-Z]{2,}$")

    fun start() {
        val scanner = Scanner(System.`in`)
        while (true) {
            print("Введите команду: ")
            val input = scanner.nextLine()

            when {
                input.equals("exit", ignoreCase = true) -> {
                    println("Завершение программы.")
                    break
                }
                input.equals("help", ignoreCase = true) -> printHelp()
                input.startsWith("add ") -> processAddCommand(input)
                else -> println("Неверная команда. Введите 'help' для списка команд.")
            }
        }
    }
    private fun processAddCommand(command: String) {
        val parts = command.split(" ")

        if (parts.size < 4) {
            println("Ошибка: недостаточно аргументов.")
            return
        }

        val name = parts[1]
        val type = parts[2].lowercase()
        val value = parts[3]

        when (type) {
            "phone" -> addPhone(name, value)
            "email" -> addEmail(name, value)
            else -> println("Ошибка: неизвестный тип '$type'. Используйте 'phone' или 'email'.")
        }
    }
    private fun addPhone(name: String, phone: String) {
        if (phoneRegex.matches(phone)) {
            println("Имя: $name, Телефон: $phone")
        } else {
            println("Ошибка: неверный формат телефона.")
        }
    }
    private fun addEmail(name: String, email: String) {
        if (emailRegex.matches(email)) {
            println("Имя: $name, Email: $email")
        } else {
            println("Ошибка: неверный формат email.")
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
            """.trimIndent()
        )
    }
}

fun main() {
    val contactManager = ContactManager()
    contactManager.start()
}
