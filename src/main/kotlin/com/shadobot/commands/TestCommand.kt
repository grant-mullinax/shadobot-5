package com.shadobot.commands

import com.shadobot.binding.ApplicationCommand
import com.shadobot.binding.ApplicationOption
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