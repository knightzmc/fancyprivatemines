package me.bristermitten.fancyprivatemines.command

import me.bristermitten.fancyprivatemines.FancyPrivateMines
import me.bristermitten.fancyprivatemines.command.subcommand.SubCommand
import net.kyori.adventure.text.minimessage.fancy.Fancy
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor

abstract class Command(private val plugin: FancyPrivateMines) : TabExecutor {

    protected val subCommandMap = mutableMapOf<String, SubCommand>()

    protected fun addSubCommand(alias: String, subCommand: SubCommand) {
        subCommandMap[alias.toLowerCase()] = subCommand
    }

    final override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (args.isEmpty()) {
            sender.sendHelp()
            return true
        }

        val subCommand = subCommandMap[args[0].toLowerCase()]
        if (subCommand == null) {
            sender.sendUnknownCommand(args[0])
            return true
        }

        if (subCommand.permission != null && sender.hasPermission(subCommand.permission).not()) {
            sender.sendNoPermission(args[0], subCommand.permission)
            return true
        }

        try {
            subCommand.exec(sender, args.drop(1).toTypedArray())
        } catch (reqFailed: CommandRequirementNotSatisfiedException) {
            plugin.langComponent.message(sender, reqFailed.langKey, *reqFailed.placeholders)
            return true
        }
        return true
    }

    final override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<String>): List<String> {
        if (args.isEmpty()) {
            return subCommandMap.entries
                    .asSequence()
                    .filter { sender.hasPermission(it.value.permission ?: "") }
                    .map { it.key }
                    .toList()
        }
        if (args.size == 1) {
            return subCommandMap.entries
                    .asSequence()
                    .filter {
                        sender.hasPermission(it.value.permission ?: "")
                    }
                    .map { it.key }
                    .filter { it.startsWith(args[0]) }
                    .toList()
        }

        val subCommand = subCommandMap[args[0].toLowerCase()] ?: return emptyList()
        return subCommand.tabComplete(sender, args.drop(1))
    }

    protected abstract fun CommandSender.sendUnknownCommand(cmd: String)
    protected abstract fun CommandSender.sendHelp()
    protected abstract fun CommandSender.sendNoPermission(cmd: String, permission: String)
}
