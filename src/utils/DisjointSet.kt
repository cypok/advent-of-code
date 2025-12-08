package utils

class DisjointSet<E> {
    private val nodes = HashMap<E, Node<E>>()

    private fun node(e: E) = nodes.getOrPut(e) { Node(e) }

    private fun findNode(n: Node<E>): Node<E> {
        val p = n.parent
        if (p != n) {
            return findNode(p)
                .also { n.parent = it }
        }
        return n
    }

    private fun findNode(a: E): Node<E> =
        findNode(node(a))

    fun size(e: E): Int =
        if (e in nodes) findNode(e).size
        else 1

    fun find(e: E): E =
        if (e in nodes) findNode(e).data
        else e

    fun union(a: E, b: E) {
        val pa = findNode(a)
        val pb = findNode(b)
        if (pa != pb) {
            val size = pa.size + pb.size
            if (pa.size < pb.size) {
                pa.parent = pb
                pb.size = size
            } else {
                pb.parent = pa
                pa.size = size
            }
        }
    }
}

private class Node<E>(val data: E) {
    var parent: Node<E> = this
    var size = 1 // valid only for the top-most parent
}
