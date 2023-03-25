package model.data.parse

enum class ParseSource {
    /**
     *
     */
    DOCUMENT,

    /**
     *
     */
    UNITED_FILE;

    /**
     *
     */
    fun isUnitedMode() = this == UNITED_FILE
}
