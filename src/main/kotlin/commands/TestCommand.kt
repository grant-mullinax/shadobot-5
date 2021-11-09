import binding.ApplicationCommand
import binding.ApplicationOption
import discord4j.core.`object`.entity.User

@ApplicationCommand("test", "its the stest")
fun testCommand(
    @ApplicationOption("a", "make it anything")
    a: String,
    @ApplicationOption("user", "user info")
    user: User
): String {
    return "Hi $a,  ${user.username}"
}