package pro.serux.telephony.parsers.memberorempty

import net.dv8tion.jda.api.entities.Member

class Member(
    val parseFailed: Boolean,
    val arg: String,
    val member: Member?
)
