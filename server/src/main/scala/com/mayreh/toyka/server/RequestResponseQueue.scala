package com.mayreh.toyka.server

import java.util.concurrent.{BlockingQueue, LinkedBlockingQueue, TimeUnit}

import com.mayreh.toyka.server.network.{ProcessorId, Request, Response}

import scala.collection.concurrent.TrieMap

/**
 * Queue between network thread and I/O thread
 */
class RequestResponseQueue {
  import RequestResponseQueue._

  private[this] val requestQueue: BlockingQueue[Request] = new LinkedBlockingQueue
  private[this] val responseQueues: collection.mutable.Map[ProcessorId, BlockingQueue[Response]] =
    TrieMap.empty.withDefault(_ => new LinkedBlockingQueue)

  def enqueueRequest(request: Request): Unit = requestQueue.put(request)

  def pollRequest(): Option[Request] =
    Option(requestQueue.poll(requestPollTimeoutMillis, TimeUnit.MILLISECONDS))

  def enqueueResponse(response: Response): Unit =
    responseQueues(response.request.processorId).put(response)

  def pollResponse(processorId: ProcessorId): Option[Response] =
    Option(responseQueues(processorId).poll())
}

object RequestResponseQueue {
  private val requestPollTimeoutMillis = 300L
}
