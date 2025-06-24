import java.lang.StringBuilder
import kotlin.math.abs
import kotlin.math.sqrt

data class Maze(
    val rows: Int,
    val cols: Int,
    val start: MazeLoc,
    val goal: MazeLoc,
    val sparseness: Double
) {
    private val grid: Array<Array<Cell>> = Array(rows) {
        Array(cols) { if (Math.random() < sparseness) Cell.BLOCKED else Cell.EMPTY }
    }

    init {
        grid[start.row][start.col] = Cell.START
        grid[goal.row][goal.col] = Cell.GOAL
    }

    override fun toString(): String {
        val sb = StringBuilder()
        for (row in grid) {
            for (cell in row) {
                sb.append(cell.toString())
            }
            sb.append(System.lineSeparator())
        }
        return sb.toString()
    }

    fun goalTest(ml: MazeLoc): Boolean = goal == ml

    fun successors(ml: MazeLoc): List<MazeLoc> {
        val locs: MutableList<MazeLoc> = mutableListOf()
        // bottom
        if (ml.row + 1 < rows && grid[ml.row + 1][ml.col] != Cell.BLOCKED) {
            locs.add(MazeLoc(ml.row + 1, ml.col))
        }
        // top
        if (ml.row - 1 >= 0 && grid[ml.row - 1][ml.col] != Cell.BLOCKED) {
            locs.add(MazeLoc(ml.row - 1, ml.col))
        }
        // right
        if (ml.col + 1 < cols && grid[ml.row][ml.col + 1] != Cell.BLOCKED) {
            locs.add(MazeLoc(ml.row, ml.col + 1))
        }
        // left
        if (ml.col - 1 >= 0 && grid[ml.row][ml.col - 1] != Cell.BLOCKED) {
            locs.add(MazeLoc(ml.row, ml.col - 1))
        }

        return locs.toList()
    }

    companion object {
        fun random(): Maze =
            Maze(32, 64, MazeLoc(0, 0), MazeLoc(31, 63), 0.1)
    }

    fun mark(path: List<MazeLoc>)  {
        for (ml in path) {
            grid[ml.row][ml.col] = Cell.PATH
        }
        grid[start.row][start.col] = Cell.START
        grid[goal.row][goal.col] = Cell.GOAL
    }

    fun clear(path: List<MazeLoc>) {
        for (ml in path) {
            grid[ml.row][ml.col] = Cell.EMPTY
        }
        grid[start.row][start.col] = Cell.START
        grid[goal.row][goal.col] = Cell.GOAL
    }

    fun euclideanDistance(ml: MazeLoc): Double {
        val xDist = ml.col - goal.col
        val yDist = ml.row - goal.row
        return sqrt(((xDist * xDist) + (yDist * yDist)).toDouble())
    }

    fun manhattanDistance(ml: MazeLoc): Double {
        val xDist = abs(ml.col - goal.col)
        val yDist = abs(ml.row - goal.row)
        return (xDist + yDist).toDouble()
    }

    fun getBlocked(): List<MazeLoc> {
        val blocked: MutableList<MazeLoc> = mutableListOf()
        grid.forEachIndexed { row, cols ->
            cols.forEachIndexed { col, cell ->
                if (cell == Cell.BLOCKED) {
                    blocked.addLast(MazeLoc(row, col))
                }
            }
        }
        return blocked
    }
}

enum class Cell(private val c: String) {
    EMPTY("."),
    BLOCKED("X"),
    START("S"),
    GOAL("G"),
    PATH("*");

    override fun toString(): String = c
}

data class MazeLoc(val row: Int, val col: Int) {
    override fun toString(): String = "($row,$col)"
}
