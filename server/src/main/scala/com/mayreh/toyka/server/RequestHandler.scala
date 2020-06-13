package com.mayreh.toyka.server

import java.util.concurrent.atomic.AtomicInteger

import com.mayreh.toyka.server.logging.Loggable
import com.mayreh.toyka.server.network.Request

class RequestHandler(requestResponseQueue: RequestResponseQueue) extends Thread with AutoCloseable {
  import RequestHandler._

  @volatile private[this] var terminated: Boolean = false

  setName(s"request-handler-${sequence.getAndIncrement()}")

  override def run(): Unit = {
    while (!terminated) {
      requestResponseQueue.pollRequest().foreach(handleRequest)
    }
  }

  def close(): Unit = {
    terminated = true
  }

  private[this] def handleRequest(request: Request): Unit = {
  }
}

object RequestHandler extends Loggable {
  private val sequence = new AtomicInteger(0)
}
