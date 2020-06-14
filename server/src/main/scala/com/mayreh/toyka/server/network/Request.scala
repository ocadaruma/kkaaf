package com.mayreh.toyka.server.network

import com.mayreh.toyka.network.{ConnectionId, RequestResponseReceive}

class Request(
  val processorId: ProcessorId,
  val connectionId: ConnectionId,
  val receive: RequestResponseReceive) {

}
