import discord4j.core.`object`.entity.User
import discord4j.discordjson.json.UserData

@ApplicationCommand("test", "its the stest")
fun testCommand(
    @ApplicationOption("a", "make it anything")
    a: String,
    @ApplicationOption("user", "user info")
    user: User
): String {
    return "Hi $a,  ${user.username}"
}