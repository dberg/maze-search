import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

fun main(args: Array<String>) {
    val instrument = (args.isNotEmpty() && (args[0] == "-i" || args[0] == "--instrument"))
    val maze = Maze.random()

    if (instrument) {
        printResultsInstrumented("dfs", maze, GenericSearchInstrumented.dfs(maze.start, maze::goalTest, maze::successors))
        printResultsInstrumented("bfs", maze, GenericSearchInstrumented.bfs(maze.start, maze::goalTest, maze::successors))
        printResultsInstrumented("astar", maze, GenericSearchInstrumented.astar(maze.start, maze::goalTest, maze::successors, maze::manhattanDistance))
    } else {
        printResults("dfs", maze, GenericSearch.dfs(maze.start, maze::goalTest, maze::successors))
        printResults("bfs", maze, GenericSearch.bfs(maze.start, maze::goalTest, maze::successors))
        printResults("astar", maze, GenericSearch.astar(maze.start, maze::goalTest, maze::successors, maze::manhattanDistance))
    }
}

fun printResults(algo: String, maze: Maze, solution: Node<MazeLoc>?) {
    return if (solution == null) {
        val msg = "No solution found using $algo search! $maze"
        println(msg)
    } else {
        val path = GenericSearch.nodeToPath(solution)
        maze.mark(path)
        val msg = "$algo search:\n$maze"
        maze.clear(path)
        println(msg)

        val log = SearchLogger.build(maze, path)
        val filename = "/tmp/graph-$algo.log"
        Files.write(Paths.get(filename), log.toByteArray(), StandardOpenOption.CREATE)
        Unit
    }
}

fun printResultsInstrumented(algo: String, maze: Maze, solutionAndPath: Pair<Node<MazeLoc>?, SearchPaths<MazeLoc>>) {
    val (solution, searchPaths) = solutionAndPath
    return if (solution == null) {
        val msg = "No solution found using $algo search! $maze"
        println(msg)
    } else {
        val path = GenericSearch.nodeToPath(solution)
        maze.mark(path)
        val msg = "$algo search:\n$maze"
        maze.clear(path)
        println(msg)

        val log = SearchLogger.buildInstrumented(maze, searchPaths)
        val filename = "/tmp/graph-instrumented-$algo.log"
        Files.write(Paths.get(filename), log.toByteArray(), StandardOpenOption.CREATE)
        Unit
    }
}
