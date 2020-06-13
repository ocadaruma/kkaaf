package com.mayreh.toyka.server.network

import java.net.InetSocketAddress
import java.nio.channels.{SelectionKey, Selector, ServerSocketChannel}

import com.mayreh.toyka.server.RequestResponseQueue
import com.mayreh.toyka.server.logging.Loggable

/**
 * Accept new connections from client
 * @param config server config
 */
class SocketServer(config: SocketServer.Config,
                   requestResponseQueue: RequestResponseQueue) extends Thread with AutoCloseable {
  import SocketServer._

  setName(s"network-socket-server")

  private[this] val processors = (0 until config.numProcessors)
    .map(i => new Processor(ProcessorId(i), requestResponseQueue))

  @volatile private[this] var terminated: Boolean = false
  private[this] var selector: Selector = _
  private[this] var channel: ServerSocketChannel = _

  override def run(): Unit = {
    selector = Selector.open()

    channel = ServerSocketChannel.open()
    channel.bind(config.bindAddress)
      .configureBlocking(false)
      .register(selector, SelectionKey.OP_ACCEPT)

    while (!terminated) {
      if (selector.select(selectTimeoutMillis) > 0) {
        val iter = selector.selectedKeys().iterator()

        while (iter.hasNext) {
          val key = iter.next()
          iter.remove()

          if (key.isAcceptable) {
            val socketChannel = key.channel().asInstanceOf[ServerSocketChannel].accept()
            processors.minBy(_.numConnection).offerConnection(socketChannel)
          }
        }
      }
    }

    safeClose(channel)
    safeClose(selector)
    processors.foreach(safeClose(_))
  }

  def close(): Unit = {
    terminated = true
    join()
  }

  private[this] def safeClose(resource: AutoCloseable): Unit = {
    if (resource != null) {
      try {
        resource.close()
      } catch {
        case t: Throwable => logger.warn("failed to close resource", t)
      }
    }
  }
}

object SocketServer extends Loggable {
  private val selectTimeoutMillis: Long = 500L

  case class Config(bindAddress: InetSocketAddress,
                    numProcessors: Int)
}
