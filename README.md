 # !!!!!!!! SHADOBOT 5 !!!!!!!!
 ###to run:
 put your bot key as the only line in a file named "key" (no extension) in your root directory
 
### to add commands:
1) write a function using only the following parameter types:
```
Implicit parameter types:
ChatInputInteractionEvent

Explicit parameter types:
String
Int
Boolean
Double
Mono<User>
Mono<Channel>
Mono<Role>
```
2) annotate the function with the ApplicationCommand annotation, and supply the name and description of the slash command:

```kotlin
@ApplicationCommand("queue", "check the queue")
fun YOURCOOLCOMMAND(...
```

4) annotate your EXPLICIT parameters with ApplicationOption and supply their name and description
```kotlin
fun YOURCOOLCOMMAND(
    eventWeeWooAwesome: ChatInputInteractionEvent,
    @ApplicationOption("a", "make it anything")
    coolString: String,
    @ApplicationOption("user", "user info")
    user: User
)
```

4) wrap your function with a CommandBinding, and get that put into the commandMap.
```kotlin
    val commands = listOf(
        CommandBinding(::executeBf),
        CommandBinding(musicCommandManager::join),
        CommandBinding(musicCommandManager::play),
        CommandBinding(musicCommandManager::skip),
        CommandBinding(musicCommandManager::queue),
        CommandBinding(musicCommandManager::playing),
        CommandBinding(musicCommandManager::remove),
        CommandBinding(::YOURCOOLCOMMAND),
    )
...
    val commandMap = commands.associateBy { it.applicationCommand.name() }
```

5) run the bot boom. you might have to wait for the commands to propagate through discord, so even if its running your commands may not show up instantly

## bonus ducks: wtf is a mono? what is going on
read this link: https://jstobigdata.com/java/getting-started-with-project-reactor/
it might mislead you but probably not

###extra bonus: 
if you give up trying to figure out monos you can use .block() to retrieve the actual value from a mono. but this will block the thread until the value completes but who cares right