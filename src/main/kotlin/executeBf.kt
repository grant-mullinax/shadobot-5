import binding.ApplicationCommand
import binding.ApplicationOption
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import discord4j.core.spec.InteractionApplicationCommandCallbackReplyMono
import reactor.core.publisher.Mono
import java.util.*

@ApplicationCommand("bf", "execute brainfuck program")
fun executeBf(
    event: ChatInputInteractionEvent,
    @ApplicationOption("program", "program to execute")
    program: String,
    @ApplicationOption("input", "input on program")
    input: String = ""
): Mono<Void> {
    val size = 10000
    val executionLimit = 1000000

    var programInputPtr = 0

    var cmdIdx = 0
    var ptr = 0
    val memory = Array(size) { 0 }
    val loopStartIdxs = Stack<Int>()
    var ignore = 0

    var executions = 0

    var out = ""

    while (cmdIdx < program.length) {
        executions++
        if (ignore == 0) {
            when (program[cmdIdx]) {
                '>' -> ptr++
                '<' -> ptr--
                '+' -> memory[ptr] = (memory[ptr] + 1) % 256
                '-' -> memory[ptr] = (memory[ptr] - 1) % 256
                '.' -> out += memory[ptr].toChar()
                ',' -> {
                    memory[ptr] = if (programInputPtr < input.length)
                        input[programInputPtr++].code
                    else
                        0
                }
                '[' -> {
                    if (memory[ptr] == 0) {
                        ignore = 1
                    } else {
                        loopStartIdxs.push(cmdIdx - 1)
                    }
                }
                ']' -> {
                    if (memory[ptr] != 0) {
                        cmdIdx = loopStartIdxs.pop()
                    } else {
                        loopStartIdxs.pop()
                    }
                }
            }
        } else {
            when (program[cmdIdx]) {
                '[' -> {
                    ignore++
                }
                ']' -> {
                    ignore--
                }
            }
        }

        cmdIdx++

        if (ptr < 0 || ptr >= size) {
            event.reply("went out of range, this is what I had:\n$out")
        }

        if (executions > executionLimit) {
            event.reply("looped too hard... this is what I had:\n$out")
        }
    }

    return event.reply(out)
}