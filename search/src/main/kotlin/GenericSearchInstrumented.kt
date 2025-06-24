import java.util.*

typealias SearchPaths<T> = MutableList<List<T>>

object GenericSearchInstrumented {

    private fun <T> recordPath(path: Node<T>, recordedPath: SearchPaths<T>) {
        val currentPath = nodeToPath(path)
        recordedPath.add(currentPath)
    }

    fun <T> dfs(initial: T, goalTest: (T) -> Boolean, successors: (T) -> List<T>): Pair<Node<T>?, SearchPaths<T>> {
        val frontier: Stack<Node<T>> = Stack()
        frontier.push(Node(initial, null))

        val explored: MutableSet<T> = mutableSetOf()
        explored.add(initial)

        val paths: SearchPaths<T> = mutableListOf()
        while (!frontier.isEmpty()) {
            val currentNode: Node<T> = frontier.pop()
            val currentState = currentNode.state
            recordPath(currentNode, paths)

            if (goalTest(currentState)) {
                return Pair(currentNode, paths)
            }

            for (child in successors(currentState)) {
                if (!explored.contains(child)) {
                    explored.add(child)
                    frontier.push(Node(child, currentNode))
                }
            }
        }

        return Pair(null, paths)
    }

    fun <T> bfs(initial: T, goalTest: (T) -> Boolean, successors: (T) -> List<T>): Pair<Node<T>?, SearchPaths<T>> {
        val frontier: Queue<Node<T>> = LinkedList()
        frontier.offer(Node(initial, null))
        val explored: MutableSet<T> = mutableSetOf()
        explored.add(initial)

        val paths: SearchPaths<T> = mutableListOf()
        while (!frontier.isEmpty()) {
            val currentNode = frontier.poll()
            val currentState = currentNode.state
            recordPath(currentNode, paths)

            if (goalTest(currentState)) {
                return Pair(currentNode, paths)
            }

            for (child in successors(currentState)) {
                if (!explored.contains(child)) {
                    explored.add(child)
                    frontier.offer(Node(child, currentNode))
                }
            }

        }
        return Pair(null, paths)
    }

    fun <T> astar(initial: T, goalTest: (T) -> Boolean, successors: (T) -> List<T>, heuristic: (T) -> Double): Pair<Node<T>?, SearchPaths<T>> {
        val frontier: PriorityQueue<Node<T>> = PriorityQueue()
        frontier.offer(Node(initial, null, 0.0, heuristic(initial)))
        val explored: MutableMap<T, Double> = mutableMapOf()

        val paths: SearchPaths<T> = mutableListOf()
        while (!frontier.isEmpty()) {
            val currentNode: Node<T> = frontier.poll()
            val currentState: T = currentNode.state
            recordPath(currentNode, paths)

            if (goalTest(currentState)) {
                return Pair(currentNode, paths)
            }

            for (child in successors(currentState)) {
                // hardcoded cost function as 1
                val newCost: Double = currentNode.cost + 1
                val exploredCost = explored[child]
                if (exploredCost == null || exploredCost > newCost) {
                    explored[child] = newCost
                    frontier.offer(Node(child, currentNode, newCost, heuristic(child)))
                }
            }
        }

        return Pair(null, paths)
    }

    fun <T> nodeToPath(node: Node<T>): List<T> {
        var curNode = node
        val path: MutableList<T> = mutableListOf(curNode.state)
        // work backwards from end to front
        while (curNode.parent != null) {
            curNode = curNode.parent!!
            path.add(0, curNode.state) // add in front
        }
        return path.toList()
    }
}
