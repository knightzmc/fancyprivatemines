package me.bristermitten.fancyprivatemines.mine

import io.papermc.lib.PaperLib
import me.bristermitten.fancyprivatemines.FancyPrivateMines
import me.bristermitten.fancyprivatemines.block.BlockData
import me.bristermitten.fancyprivatemines.block.toBlockData
import me.bristermitten.fancyprivatemines.block.toBlockMask
import me.bristermitten.fancyprivatemines.data.makeRegion
import me.bristermitten.fancyprivatemines.data.toChunkData
import me.bristermitten.fancyprivatemines.pattern.FractionalBlockPattern
import me.bristermitten.fancyprivatemines.schematic.MineSchematic
import me.bristermitten.fancyprivatemines.schematic.attributes.MiningRegionScanner
import me.bristermitten.fancyprivatemines.schematic.attributes.SpawnPointScanner
import me.bristermitten.fancyprivatemines.util.VoidWorldGenerator
import me.bristermitten.fancyprivatemines.util.await
import me.bristermitten.fancyprivatemines.util.center
import org.bukkit.*
import org.bukkit.entity.Player
import java.io.File
import java.util.*

class VoidWorldMineFactory(private val plugin: FancyPrivateMines) : MineFactory() {
    private val random = SplittableRandom()

    private val world: World = Bukkit.createWorld(
        WorldCreator(plugin.pmConfig.mineWorld)
            .generator(VoidWorldGenerator)
            .generateStructures(false))
        .apply {
            difficulty = Difficulty.PEACEFUL
        }

    override suspend fun create(
        schematicFile: File,
        mineSchematic: MineSchematic,
        owner: Player,
    ): PrivateMine {
        val paster = plugin.configuration.schematicPasters.active

        val location = findFreeLocation()
        val region = paster.paste(schematicFile, location)

        val miningRegionScanner = MiningRegionScanner(Material.POWERED_RAIL)
        val spawnPointScanner = SpawnPointScanner(BlockData(Material.CHEST, -1))
        plugin.schematicScanner.scan(region, mineSchematic, listOf(miningRegionScanner, spawnPointScanner))

        val miningRegionPoints = mineSchematic.getAttributeFor(miningRegionScanner)
            .map { it.toLocation(region.origin) }

        val spawnPoint = mineSchematic.getAttributeFor(spawnPointScanner)
            .toLocation(region.origin)

        plugin.configuration.blockSetting.methods.active.setBlock(spawnPoint,
            Material.AIR.toBlockData().toBlockMask())

        val mask = FractionalBlockPattern(
            mapOf(
                Material.STONE.toBlockData() to 1,
                Material.COAL_ORE.toBlockData() to 1,
                Material.COAL_BLOCK.toBlockData() to 1,

            )
        )

        val miningRegion = makeRegion(miningRegionPoints[0], miningRegionPoints[1])
        println(miningRegion)
        plugin.configuration.blockSetting.methods.active.setBlocksBulk(miningRegion, mask)

        val mine = PrivateMine(
            PrivateMine.nextId,
            owner.uniqueId,
            null,
            true,
            mask,
            0.0,
            spawnPoint,
            region,
            miningRegion
        )

        plugin.mineStorage.add(region.chunks, mine)
        return mine
    }

    private suspend fun findFreeLocation(): Location {
        //TODO config instead of magic values
        val x = random.nextInt(0, 5000)
        val z = random.nextInt(0, 5000)

        val chunk = PaperLib.getChunkAtAsync(world, x, z, false).await()

        return if (plugin.mineStorage[chunk.toChunkData()] != null) {
            findFreeLocation()
        } else {
            chunk.center
        }
    }
}
