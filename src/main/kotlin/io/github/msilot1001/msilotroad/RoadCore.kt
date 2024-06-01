package io.github.msilot1001.msilotroad

import io.github.monun.tap.fake.FakeEntity
import io.github.monun.tap.fake.FakeEntityServer
import io.github.msilot1001.msilotroad.plugin.MsilotroadPlugin
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.concurrent.schedule
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.entity.FallingBlock
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.util.Vector

enum class RoadShapeType {
  LINE,
  CurveTHREE,
  CurveFOUR,
}

public class RoadSession(public val player: Player, public val world: World) {
  private val roadShapeType = RoadShapeType.CurveTHREE
  private val points = HashMap<Int, Location>()

  // fake entities
  private lateinit var fakeServer: FakeEntityServer
  private lateinit var cursorFakeBlock: FakeEntity<FallingBlock>
  private val pointFakeBlocks = HashMap<Int, FakeEntity<FallingBlock>>()

  // messages
  private var actionBar = ActionBarManager()
  private val defaultMessage = Messages.ROADSESSION_DEFAULT_MSG
  private lateinit var messageToDisplay: Component

  lateinit var location: Location
    private set

  fun initialize(fakeServer: FakeEntityServer) {
    this.fakeServer = fakeServer

    // raytrace block that player is heading
    this.player.rayTraceBlocks(100.0)?.let { rayTraceResult ->
      if (rayTraceResult.hitBlock != null) {
        rayTraceResult.hitBlock?.let { block ->
          // spawn fake block
          val fakeblock =
            fakeServer.spawnFallingBlock(
              block.location.add(0.5, 0.01, 0.5),
              Material.LIGHT_BLUE_STAINED_GLASS.createBlockData()
            )
          fakeblock.updateMetadata { setGravity(false) }
          this.cursorFakeBlock = fakeblock
        }
      }
    }

    // initialize message to Display
    messageToDisplay = defaultMessage

    // send info message

    actionBar.sendActionBar(player, messageToDisplay)
  }

  private fun moveFakeBlock(loc: Location): Boolean {
    cursorFakeBlock.moveTo(loc)
    return true
  }

  fun equals(player: Player, world: World): Boolean {
    return this.player == player && this.world == world
  }

  // don't call this function inside this class / use RoadCore.terminateSession() instead
  fun terminate() {
    // remove fake blocks
    for (n in 1..pointFakeBlocks.size step 1) {
      val block = pointFakeBlocks[n]
      block?.remove()
      pointFakeBlocks.remove(n)
    }

    cursorFakeBlock.remove()
  }

  // change it to opening road building menu
  fun toolLeftClick(e: PlayerInteractEvent) {
    if (points.size == roadShapeType.ordinal + 2) {
      val p1 = points[1]
      val p2 = points[2]
      val p3 = points[3]

      if (p1 != null && p2 != null && p3 != null) {
        val locations =
          RoadCore.calculateBezier3Curve(
            p1,
            p2,
            p3,
            Math.round(
              (RoadCore.calculateDistance(p1, p2) + RoadCore.calculateDistance(p2, p3)) * 100
            )
          )

        for (loc in locations) {
          loc.block.type = Material.BLACK_CONCRETE
        }
      }

      // display building success message
      messageToDisplay = Messages.ROADSESSION_BUILD_SUCCESS

      // terminate session
      Timer().schedule(100) { RoadCore.terminateSession(player, world) }
    }
  }

  // set point / cancel point if the point heading is the point already clicked
  fun toolRightClick(e: PlayerInteractEvent) {
    // find point player heading
    player.rayTraceBlocks(100.0)?.let { rayTraceResult ->
      if (rayTraceResult.hitBlock != null) {
        rayTraceResult.hitBlock?.let { block ->
          val location = block.location

          // check if player is canceling the point
          if (points.containsValue(block.location)) {
            // find points to remove
            val point = points.filter { it.value == block.location }

            val pointsToDelete = ArrayList<Int>()

            // if it has more points after, than delete those too
            for (n in point) {
              // remove other points if it has more points
              if (points.size > n.key) {
                for (i in 1..(points.size - n.key) step 1) {
                  pointsToDelete.add(n.key + i)
                }
              }

              // add to deleting list
              pointsToDelete.add(n.key)

              // delete points and all fake blocks
              for (k in pointsToDelete) {
                points.remove(k)
                pointFakeBlocks.get(k).let { it?.remove() }
                pointFakeBlocks.remove(k)
              }
            }

            return
          } else {
            // get max amount of points
            val maxPoints = roadShapeType.ordinal + 2 // line: 2, curve three: 3, curve four: 4

            // check what point is now setting
            val n = points.size + 1 // 1st, 2nd, (3rd, 4th)

            // if n = max points
            if (n > maxPoints) {
              // Display that it reached max points
              messageToDisplay = Messages.ROADSESSION_MAXPOINTS
              Timer().schedule(3000) { messageToDisplay = defaultMessage }
            } else {
              // put into points list
              points.put(n, location)

              // create new fake block to display the point that is selected
              // if it is the first and last point, summon red glass, if not, summon lime glass
              val fakeblock =
                fakeServer.spawnFallingBlock(
                  block.location.add(0.5, 0.01, 0.5),
                  (if (n == 1 || n == maxPoints) {
                      Material.RED_STAINED_GLASS
                    } else {
                      Material.LIME_STAINED_GLASS
                    })
                    .createBlockData()
                )
              fakeblock.updateMetadata { setGravity(false) }

              // put fake block summoned into fake block list
              pointFakeBlocks.put(n, fakeblock)


            }
          }
        }
      }
    }
  }

  fun update() {
    // keep sending action bar
    actionBar.sendActionBar(player, messageToDisplay)
  }

  fun updateCursor(block: Block) {
    moveFakeBlock(block.location.add(0.5, 0.01, 0.5))
  }
}

object RoadCore {
  private val sessions = ArrayList<RoadSession>()
  private val playersInSession = ArrayList<Player>()
  private lateinit var fakeServer: FakeEntityServer
  private val userPalette = HashMap<UUID, Material>()

  // constants
  public val tool = Material.BLAZE_ROD

  fun initCore(plugin: MsilotroadPlugin) {
    this.fakeServer = plugin.fakeServer
  }

  fun update() {
    for (session in sessions) {
      session.update()
    }
  }

  fun registerSession(player: Player, world: World) {
    val session = RoadSession(player, world)
    session.initialize(fakeServer)

    sessions.add(session)
    playersInSession.add(player)
    userPalette[player.uniqueId] = Material.BLACK_CONCRETE
  }

  fun findSession(player: Player, world: World): RoadSession? {
    return sessions.find { it.equals(player, world) }
  }

  fun terminateSession(player: Player, world: World): Boolean {
    val result = this.findSession(player, world)

    if (result != null) {
      playersInSession.remove(player)
      result.terminate()
      sessions.remove(result)
      userPalette.remove(player.uniqueId)
      return true
    }

    return false
  }

  fun terminateAll(player: Player) {
    val session = sessions.find { it.player == player }
    if (session != null) {
      terminateSession(player, session.world)
    }
  }

  fun activePlayers(): ArrayList<Player> {
    return playersInSession
  }

  fun calculateBezier3Curve(
    start: Location,
    control: Location,
    end: Location,
    t: Long
  ): List<Location> {
    val locations = mutableListOf<Location>()

    for (i in 0..t step 1) {
      val j = i / t.toDouble()
      val point = calculateBezier3Point(start.toVector(), control.toVector(), end.toVector(), j)
      locations.add(point.toLocation(start.world))
    }

    return locations
  }

  private fun calculateBezier3Point(
    start: Vector,
    control: Vector,
    end: Vector,
    t: Double
  ): Vector {
    val u = 1 - t
    val tt = t * t
    val uu = u * u

    var result = start.clone().multiply(uu) // start * (1-t)^2
    result = result.add(control.clone().multiply(2 * u * t)) // 2 * control * (1-t) * t
    result = result.add(end.clone().multiply(tt)) // end * t^2

    return result
  }

  fun getParallelCurve3(
    start: Location,
    control: Location,
    end: Location,
    d: Double
  ): List<Location> {
    val locations = mutableListOf<Location>()

    return locations
  }

  fun calculateDistance(location1: Location, location2: Location): Double {
    return location1.distance(location2)
  }
}
