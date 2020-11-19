package me.bristermitten.fancyprivatemines.hook.fawe

import com.boydti.fawe.util.EditSessionBuilder
import com.sk89q.worldedit.EditSession
import com.sk89q.worldedit.regions.CuboidRegion
import me.bristermitten.fancyprivatemines.FancyPrivateMines
import me.bristermitten.fancyprivatemines.component.blocks.BlockSettingMethod
import me.bristermitten.fancyprivatemines.block.BlockMask
import org.bukkit.Location
import org.bukkit.World
import java.util.*

class FAWEBlockSettingMethod(val plugin: FancyPrivateMines) : BlockSettingMethod() {
    override val id: String = "FAWE"
    override val priority: Int = 5 //FAWE is the highest speed, so will be prioritised



    override fun setBlock(location: Location, data: BlockMask) {

        val session = location.world.editSession

        val block = data.generate()

        val baseBlock = block.toBaseBlock()
        session.setBlock(location.toWorldEditVector(), baseBlock)
    }

    override fun setBlocksBulk(pos1: Location, pos2: Location, mask: BlockMask) {
        val session = pos1.world.editSession

        val region = CuboidRegion(pos1.toWorldEditVector(), pos2.toWorldEditVector())
        session.setBlocks(region, mask.toWEPattern())
        session.flushQueue()
    }

    override fun setBlocksBulk(locations: List<Location>, mask: BlockMask) {
        if(locations.isEmpty()) {
            return
        }
        val session = locations[0].world.editSession

        session.setBlocks(locations.map {it.toWorldEditVector()}.toSet(), mask.toWEPattern())
    }
}
