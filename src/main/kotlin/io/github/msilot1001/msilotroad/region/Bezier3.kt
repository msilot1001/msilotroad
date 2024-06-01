package io.github.msilot1001.msilotroad.region

import io.github.msilot1001.msilotroad.util.Side
import org.bukkit.Location
import kotlin.math.ceil

class Bezier3 {
    fun points(
        start: Location,
        control: Location,
        end: Location,
    ): List<Location> {
        val locations = mutableListOf<Location>()
        val t = ceil((start.distance(end) + start.distance(control) + control.distance(end)) / 2).toInt() * 5;

        for (i in 0..t step 1) {
            val j = i / t.toDouble()

            val u = 1 - t
            val tt = t * t
            val uu = u * u

            var result = start.toVector().clone().multiply(uu) // start * (1-t)^2
            result = result.add(control.toVector().clone().multiply(2 * u * j)) // 2 * control * (1-t) * t
            result = result.add(end.toVector().clone().multiply(tt)) // end * t^2

            locations.add(result.toLocation(start.world))
        }

        // eliminate weird adjacent points
        // if point is meeting on the side

        return locations
    }

    // creates a parallel curve to this curve that is far away about the designated amount on the specified side
    fun parallelCurve(dist: Double, side: Side) {

    }
}