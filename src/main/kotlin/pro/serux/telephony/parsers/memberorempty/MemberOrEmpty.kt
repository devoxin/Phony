package pro.serux.telephony.parsers.memberorempty

import me.devoxin.flight.api.Context
import me.devoxin.flight.parsers.MemberParser
import me.devoxin.flight.parsers.Parser
import java.util.*

class MemberOrEmpty : Parser<Member> {
    override fun parse(ctx: Context, param: String): Optional<Member> {
        val entity = memberParser.parse(ctx, param)
        val member = Member(entity.isEmpty, param, entity.orElse(null))
        return Optional.of(member)
    }

    companion object {
        private val memberParser = MemberParser()
    }
}
