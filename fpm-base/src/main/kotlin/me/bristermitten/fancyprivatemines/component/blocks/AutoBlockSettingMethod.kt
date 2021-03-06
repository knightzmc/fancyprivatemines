package me.bristermitten.fancyprivatemines.component.blocks

import me.bristermitten.fancyprivatemines.FancyPrivateMines
import me.bristermitten.fancyprivatemines.pattern.BlockPattern
import me.bristermitten.fancyprivatemines.data.ImmutableLocation
import me.bristermitten.fancyprivatemines.data.Region
import me.bristermitten.fancyprivatemines.logging.debug
import me.bristermitten.fancyprivatemines.logging.fpmLogger
import org.bukkit.Location

class AutoBlockSettingMethod(private val plugin: FancyPrivateMines) : BlockSettingMethod() {
    override val id: String = "Auto"

    override val priority: Int = Int.MIN_VALUE

    override fun init() {
        val maxByOrNull = plugin.configuration.blockSetting.methods.all.maxByOrNull {
            it.priority //Get the highest priority method
        }!! //This should never ever be empty
        if (maxByOrNull == this) {
            plugin.logger.severe {
                """
                    === COULD NOT REGISTER A BLOCK SETTING METHOD! ===
                    > Usually, this means that you do not have a world editing plugin installed
                    > Please install WorldEdit, FastAsyncWorldEdit, or AsyncWorldEdit

                    > If the error persists, enable debugging mode in the config, and send any errors to our Discord Server.
                    > https://discord.gg/D2HBcFtU9v
                """.trimIndent()
            }

            if (plugin.pmConfig.debug) {
                val exception = IllegalStateException("Max priority BlockSettingMethod was AutoBlockSettingMethod.")
                fpmLogger.debug {
                    "BlockSettingMethods: ${plugin.configuration.blockSetting.methods}"
                }
                throw exception
            }

            return
        }

        plugin.configuration.blockSetting.methods.active = maxByOrNull.also { it.init() }
    }

    override fun setBlock(location: ImmutableLocation, data: BlockPattern) = notAllowed()

    override fun setBlocksBulk(region: Region, pattern: BlockPattern) = notAllowed()

    override fun setBlocksBulk(locations: List<Location>, pattern: BlockPattern) = notAllowed()

    private fun notAllowed(): Nothing = throw UnsupportedOperationException("AutoBlockSettingMethod must delegate")
}
