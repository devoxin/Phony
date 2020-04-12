package pro.serux.telephony.entities

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class CallManager {
    private val timer = Executors.newSingleThreadScheduledExecutor()
    private val calls = ConcurrentHashMap<Long, Call>()

    fun isLineEngaged(guildId: Long): Boolean {
        return hasOutgoingCall(guildId) || hasIncomingCall(guildId)
    }

    fun hasOutgoingCall(guildId: Long): Boolean {
        return calls.containsKey(guildId)
    }

    fun hasIncomingCall(guildId: Long): Boolean {
        return calls.values.any { it.receiverGuildId == guildId }
    }

    fun getIncomingCall(receiver: Long): Call? {
        return calls.values.firstOrNull { it.receiverGuildId == receiver }
    }

    fun getOutgoingCall(caller: Long): Call? {
        return calls[caller]
    }

    fun getCallFor(guildId: Long): Call? {
        return getOutgoingCall(guildId)
            ?: getIncomingCall(guildId)
    }

    fun remove(call: Call) {
        calls.remove(call.callerGuildId)
    }

    fun setup(callerId: Long, callerGuild: Long, receiverGuild: Long): Call {
        val call = Call(callerId, callerGuild, receiverGuild)
        calls[callerGuild] = call

        timer.schedule({
            if (call.status == CallStatus.CALLING) {
                call.notifyCaller("Call ended; nobody answered.")
                call.notifyReceiver("Call ended; nobody answered.")
                call.end()

                remove(call)
            }
        }, 30, TimeUnit.SECONDS)

        return call
    }

    fun hangup(guildId: Long) {
        calls.remove(guildId)
    }
}
