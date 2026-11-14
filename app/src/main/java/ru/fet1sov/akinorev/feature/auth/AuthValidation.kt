package ru.fet1sov.akinorev.feature.auth

internal object AuthValidation {
    const val UsernameMaxLength = 24
    const val UsernameMinLength = 3
    const val PasswordMinLength = 6

    private val usernamePattern = Regex("^[A-Za-z0-9]{$UsernameMinLength,$UsernameMaxLength}$")
    private val forbiddenUsernameChars = Regex("[^A-Za-z0-9]")
    private val emailPattern = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]{2,}$")

    fun sanitizeUsername(raw: String): String =
        forbiddenUsernameChars.replace(raw, "").take(UsernameMaxLength)

    fun isUsernameValid(value: String): Boolean = usernamePattern.matches(value)

    fun isPasswordValid(value: String): Boolean = value.length >= PasswordMinLength

    fun isEmailValid(value: String): Boolean = emailPattern.matches(value)
}
