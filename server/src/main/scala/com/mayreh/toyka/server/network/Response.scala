package com.mayreh.toyka.server.network

import com.mayreh.toyka.network.RequestResponseSend

class Response(val request: Request) {
  def toSend: RequestResponseSend = ???
}
