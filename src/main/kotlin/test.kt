import reactor.core.publisher.Mono

fun testf(a: Mono<String>) {
    print("a")
}


fun main() {
    for (parameter in ::testf.parameters) {
        print(parameter.type.arguments.first())
    }
}