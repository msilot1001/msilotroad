package io.github.msilot1001.msilotroad.region

import org.bukkit.Location
import org.bukkit.World
import kotlin.math.abs
import kotlin.math.max

    // https://github.com/EngineHub/WorldEdit/blob/master/worldedit-core/src/main/java/com/sk89q/worldedit/EditSession.java#L2583
    fun line(world: World, pos1: Location, pos2: Location): MutableList<Location> {
        val blockSet = mutableListOf<Location>()

        val x1: Int = pos1.blockX
        val y1: Int = pos1.blockY
        val z1: Int = pos1.blockZ
        val x2: Int = pos2.blockX
        val y2: Int = pos2.blockY
        val z2: Int = pos2.blockZ
        var tipx = x1
        var tipy = y1
        var tipz = z1
        val dx = abs((x2 - x1).toDouble()).toInt()
        val dy = abs((y2 - y1).toDouble()).toInt()
        val dz = abs((z2 - z1).toDouble()).toInt()

        if (dx + dy + dz == 0) {
            blockSet.add(Location(world, tipx.toDouble(), tipy.toDouble(), tipz.toDouble()))
            return blockSet
        }

        val dMax = max(max(dx.toDouble(), dy.toDouble()), dz.toDouble()).toInt()
        when (dMax) {
            dx -> {
                for (domstep in 0..dx) {
                    tipx = x1 + domstep * (if (x2 - x1 > 0) 1 else -1)
                    tipy =
                        Math.round(y1 + domstep * (dy.toDouble()) / (dx.toDouble()) * (if (y2 - y1 > 0) 1 else -1)).toInt()
                    tipz =
                        Math.round(z1 + domstep * (dz.toDouble()) / (dx.toDouble()) * (if (z2 - z1 > 0) 1 else -1)).toInt()

                    blockSet.add(Location(world, tipx.toDouble(), tipy.toDouble(), tipz.toDouble()))
                }
            }
            dy -> {
                for (domstep in 0..dy) {
                    tipy = y1 + domstep * (if (y2 - y1 > 0) 1 else -1)
                    tipx =
                        Math.round(x1 + domstep * (dx.toDouble()) / (dy.toDouble()) * (if (x2 - x1 > 0) 1 else -1)).toInt()
                    tipz =
                        Math.round(z1 + domstep * (dz.toDouble()) / (dy.toDouble()) * (if (z2 - z1 > 0) 1 else -1)).toInt()

                    blockSet.add(Location(world, tipx.toDouble(), tipy.toDouble(), tipz.toDouble()))
                }
            }
            else -> /* if (dMax == dz) */ {
                for (domstep in 0..dz) {
                    tipz = z1 + domstep * (if (z2 - z1 > 0) 1 else -1)
                    tipy =
                        Math.round(y1 + domstep * (dy.toDouble()) / (dz.toDouble()) * (if (y2 - y1 > 0) 1 else -1)).toInt()
                    tipx =
                        Math.round(x1 + domstep * (dx.toDouble()) / (dz.toDouble()) * (if (x2 - x1 > 0) 1 else -1)).toInt()

                    blockSet.add(Location(world, tipx.toDouble(), tipy.toDouble(), tipz.toDouble()))
                }
            }
        }

        return blockSet
    }
