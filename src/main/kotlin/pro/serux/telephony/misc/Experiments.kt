package pro.serux.telephony.misc

enum class Experiments(val rawValue: Int, val description: String) {
    MULTI_USER_CALLS(1, "Enables receiving audio of all users in a voice channel.");

    fun isEnabled(raw: Int): Boolean {
        return (raw and this.rawValue) == this.rawValue
    }
}
