package binding

import reactor.core.publisher.Mono
import kotlin.reflect.KFunction

interface CommandDelegate {
    fun commandMapping(): Map<String, KFunction<Mono<Void>>>
}