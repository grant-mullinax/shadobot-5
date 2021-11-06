package binding

import discord4j.core.`object`.entity.Role
import discord4j.core.`object`.entity.User
import discord4j.core.`object`.entity.channel.Channel
import reactor.core.CoreSubscriber
import reactor.core.publisher.Mono
import kotlin.reflect.KParameter

annotation class ApplicationCommand(val name: String, val description: String)
annotation class ApplicationOption(val name: String, val description: String)

interface ParameterData {
    val kParameter: KParameter
}

class ExplicitParameter(val name: String, override val kParameter: KParameter) : ParameterData
class ImplicitParameter(override val kParameter: KParameter) : ParameterData

// class AuthorMono: Mono<User>()
typealias UserMono = Mono<User>
typealias ChannelMono = Mono<Channel>
typealias RoleMono = Mono<Role>