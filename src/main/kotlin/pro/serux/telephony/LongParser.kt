package pro.serux.telephony

import me.devoxin.flight.api.Context
import me.devoxin.flight.parsers.Parser
import java.util.*

class LongParser : Parser<Long> {

    override fun parse(ctx: Context, param: String): Optional<Long> {
        return Optional.ofNullable(param.toLongOrNull())
    }

}
