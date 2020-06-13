package com.mayreh.toyka.server.network

import java.nio.channels.{SelectionKey, Selector, SocketChannel}
import java.util.concurrent.{BlockingQueue, LinkedBlockingQueue}
import java.util.concurrent.atomic.AtomicInteger

import com.mayreh.toyka.network.{ConnectionId, ConnectionStateMachine}
import com.mayreh.toyka.server.RequestResponseQueue

case class ProcessorId(value: Int) extends AnyVal

class Processor(processorId: ProcessorId,
                requestResponseQueue: RequestResponseQueue) extends Thread with AutoCloseable {
  import Processor._

  setName(s"network-processor-${processorId.value}")

  private[this] val selector = Selector.open()
  private[this] val numConnection = new AtomicInteger(0)
  private[this] val connectionQueue: BlockingQueue[SocketChannel] = new LinkedBlockingQueue

  @volatile private[this] var terminated: Boolean = false
  private[this] var connectionSequence = 0

  override def run(): Unit = {
    while (!terminated) {
      var channel: SocketChannel = null
      while ({channel = connectionQueue.poll(); channel != null}) {
        channel
          .configureBlocking(false)
          .register(selector, SelectionKey.OP_READ)
          .attach(new ConnectionId(channel.socket(), connectionSequence))
        connectionSequence += 1
      }

      Iterator
        .continually(requestResponseQueue.pollResponse(processorId))
        .takeWhile(_.isDefined)
        .flatten
        .foreach(response => )

      val timeout = if (connectionQueue.isEmpty) selectTimeoutMillis else 0
      if (selector.select(timeout) > 0) {
        val iter = selector.selectedKeys().iterator()

        while (iter.hasNext) {
          val key = iter.next()
          iter.remove()

          val channel = key.channel().asInstanceOf[SocketChannel]
          val channelStateMachine = key.attachment()
          if (key.isReadable) {
            channelStateMachine.receive(channel)
          }
          if (key.isWritable) {
            channelStateMachine.send(channel)
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

  private[this] def handleNewConnection(): Unit = {

  }
}

object Processor {
  private val selectTimeoutMillis: Long = 100L
}
