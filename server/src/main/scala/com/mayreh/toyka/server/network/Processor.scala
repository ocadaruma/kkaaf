package com.mayreh.toyka.server.network

import java.nio.channels.{SelectionKey, Selector, SocketChannel}
import java.util.concurrent.{BlockingQueue, LinkedBlockingQueue}
import java.util.concurrent.atomic.AtomicInteger

import com.mayreh.toyka.network.{Connection, ConnectionId}
import com.mayreh.toyka.server.RequestResponseQueue
import com.mayreh.toyka.server.logging.Loggable

import scala.collection.mutable

case class ProcessorId(value: Int) extends AnyVal

class Processor(processorId: ProcessorId,
                requestResponseQueue: RequestResponseQueue) extends Thread with AutoCloseable {
  import Processor._

  setName(s"network-processor-${processorId.value}")

  private[this] val selector = Selector.open()
  private[this] val numConnection = new AtomicInteger(0)
  private[this] val connectionQueue: BlockingQueue[SocketChannel] = new LinkedBlockingQueue
  private[this] val connections: mutable.Map[ConnectionId, ConnectionSelect] = mutable.Map.empty

  @volatile private[this] var terminated: Boolean = false
  private[this] var connectionSequence = 0

  override def run(): Unit = {
    while (!terminated) {
      Iterator
        .continually(connectionQueue.poll())
        .takeWhile(_ != null)
        .foreach(channel => {
          val connectionId = new ConnectionId(channel.socket(), connectionSequence)
          val key = channel
            .configureBlocking(false)
            .register(selector, SelectionKey.OP_READ)
          key.attach(connectionId)

          connectionSequence += 1
          connections(connectionId) = ConnectionSelect(key, new Connection)
        })

      Iterator
        .continually(requestResponseQueue.pollResponse(processorId))
        .takeWhile(_.isDefined)
        .flatten
        .foreach(response => {
          val connSelect = connections(response.request.connectionId)

          connSelect.selectionKey.interestOpsOr(SelectionKey.OP_WRITE)
          connSelect.connection.setSend(response.toSend)
        })

      val timeout = if (connectionQueue.isEmpty) selectTimeoutMillis else 0
      if (selector.select(timeout) > 0) {
        val iter = selector.selectedKeys().iterator()

        while (iter.hasNext) {
          val key = iter.next()
          iter.remove()

          val channel = key.channel().asInstanceOf[SocketChannel]
          val connectionId = key.attachment().asInstanceOf[ConnectionId]
          val connSelect = connections(connectionId)
          if (key.isReadable) {
            val result = connSelect.connection.receiveFrom(channel)
            if (result.completed()) {
              requestResponseQueue.enqueueRequest(new Request(processorId, connectionId, result.receive()))
            }
          }
          if (key.isWritable) {
            val result = connSelect.connection.transferTo(channel)
            if (result.completed()) {
              connSelect.selectionKey.interestOpsAnd(~SelectionKey.OP_WRITE)
            }
          }
        }
      }
    }

    selector.close()
  }

  def numConnection: Int = this.numConnection.get()

  def offerConnection(socketChannel: SocketChannel): Boolean = {
    connectionQueue.offer(socketChannel)
  }

  def close(): Unit = {
    terminated = true
  }
}

object Processor extends Loggable {

  private case class ConnectionSelect(
    selectionKey: SelectionKey, connection: Connection)

  private val selectTimeoutMillis: Long = 500L
}
