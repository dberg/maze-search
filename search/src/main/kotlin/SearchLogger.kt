object SearchLogger {
    fun build(maze: Maze, path: List<MazeLoc>): String {
        val b = StringBuilder()
        b.append("rows: ${maze.rows}\n")
        b.append("cols: ${maze.cols}\n")
        b.append("ini: ${maze.start}\n")
        b.append("end: ${maze.goal}\n")

        val blocked = maze.getBlocked().joinToString(" ")
        b.append("blocked: $blocked\n")

        val pathString = path.joinToString(" ")
        b.append("path: $pathString\n")

        return b.toString()
    }

    fun buildInstrumented(maze: Maze, searchPaths: SearchPaths<MazeLoc>): String {
        val b = StringBuilder()
        b.append("rows: ${maze.rows}\n")
        b.append("cols: ${maze.cols}\n")
        b.append("ini: ${maze.start}\n")
        b.append("end: ${maze.goal}\n")

        val blocked = maze.getBlocked().joinToString(" ")
        b.append("blocked: $blocked\n")

        // paths: [(x1,y1)(x2, y2)(...)][(x1, y1)(...)]
        b.append("paths: ")
        searchPaths.forEach {
            val path = it.joinToString(separator = "", prefix = "[", postfix = "]")
            b.append(path)
        }
        b.append("\n")

        return b.toString()
    }

}