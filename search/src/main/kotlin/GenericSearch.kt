import java.util.*

object GenericSearch {

    fun <T> dfs(initial: T, goalTest: (T) -> Boolean, successors: (T) -> List<T>): Node<T>? {
        val frontier: Stack<Node<T>> = Stack()
        frontier.push(Node(initial, null))
        val explored: MutableSet<T> = mutableSetOf()
        explored.add(initial)

        while (!frontier.isEmpty()) {
            val currentNode: Node<T> = frontier.pop()
            val currentState = currentNode.state

            if (goalTest(currentState)) {
                return currentNode
            }

            for (child in successors(currentState)) {
                if (!explored.contains(child)) {
                    explored.add(child)
                    frontier.push(Node(child, currentNode))
                }
            }
        }

        return null
    }

    fun <T> bfs(initial: T, goalTest: (T) -> Boolean, successors: (T) -> List<T>): Node<T>? {
        val frontier: Queue<Node<T>> = LinkedList()
        frontier.offer(Node(initial, null))
        val explored: MutableSet<T> = mutableSetOf()
        explored.add(initial)

        while (!frontier.isEmpty()) {
            val currentNode = frontier.poll()
            val currentState = currentNode.state

            if (goalTest(currentState)) {
                return currentNode
            }

            for (child in successors(currentState)) {
                if (!explored.contains(child)) {
                    explored.add(child)
                    frontier.offer(Node(child, currentNode))
                }
            }

        }
        return null
    }

    fun <T> astar(initial: T, goalTest: (T) -> Boolean, successors: (T) -> List<T>, heuristic: (T) -> Double): Node<T>? {
        val frontier: PriorityQueue<Node<T>> = PriorityQueue()
        frontier.offer(Node(initial, null, 0.0, heuristic(initial)))
        val explored: MutableMap<T, Double> = mutableMapOf()
        while (!frontier.isEmpty()) {
            val currentNode: Node<T> = frontier.poll()
            val currentState: T = currentNode.state
            if (goalTest(currentState)) {
                return currentNode
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
        return null
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

data class Node<T>(
    val state: T,
    val parent: Node<T>?,
    val cost: Double = 0.0,
    val heuristic: Double = 0.0
) : Comparable<Node<T>> {

    override fun compareTo(other: Node<T>): Int {
        val mine = cost + heuristic
        val theirs = other.cost + other.heuristic
        return mine.compareTo(theirs)
    }

}