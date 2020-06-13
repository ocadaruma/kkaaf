package com.mayreh.toyka.server

import java.net.InetSocketAddress

import com.mayreh.toyka.server.network.SocketServer

/**
 * Entry point of Toyka server application
 */
object Application {
  def main(args: Array[String]): Unit = {
    val port = args(0).toInt

    new SocketServer(SocketServer.Config(
      new InetSocketAddress("localhost", port), 2)).start()
  }
}
