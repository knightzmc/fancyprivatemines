package me.bristermitten.fancyprivatemines.command

import me.bristermitten.fancyprivatemines.FancyPrivateMines
import me.bristermitten.fancyprivatemines.command.subcommand.*
import me.bristermitten.fancyprivatemines.lang.key.Commands
import me.bristermitten.fancyprivatemines.lang.key.Errors
import org.bukkit.command.CommandSender

class FancyPrivateMinesCommand(val plugin: FancyPrivateMines) : Command(plugin) {

    init {
        addSubCommand("list", ListPrivateMinesSubCommand(plugin))
        addSubCommand("reload", ReloadSubCommand(plugin))
        addSubCommand("create", CreateSubCommand(plugin))
        addSubCommand("menu", MenuSubCommand(plugin))
        addSubCommand("rename", RenameSubCommand(plugin))
    }

    override fun CommandSender.sendUnknownCommand(cmd: String) {
        plugin.langComponent.message(this, Errors.UnknownCommand)
    }

    override fun CommandSender.sendHelp() {
        plugin.langComponent.message(this, Commands.HELP_HEADER)

        subCommandMap.entries
            .filter { hasPermission(it.value.permission ?: "") }
            .forEach {
                plugin.langComponent.message(
                    this, Commands.HELP_COMMAND,
                    "%cmd_name%", it.key,
                    "%cmd_description%", it.value.description ?: ""
                )
            }

        plugin.langComponent.message(this, Commands.HELP_FOOTER)
    }

    override fun CommandSender.sendNoPermission(cmd: String, permission: String) {
        plugin.langComponent.message(
            this, Errors.NoPermission,
            "%permission%", permission,
            "%cmd_name%", cmd
        )
    }
}
